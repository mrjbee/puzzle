function initialize_browser_module(){

   $(window).resize(function() {
        var cellSize = thumbnailCellSize()
        var thumbnailsPanelWidth = Math.floor(pageWidth()/cellSize.width) * cellSize.width
        $('.panel-thumbnails').each(function() {
                $(this).width(thumbnailsPanelWidth);
        });
   });

   $("#drop-selection-btn").click(function (event){
         vibrate(50)
         ui_updateByIdOrIds(selectedMediaIds, ui_thumbnail_deSelectById)
         selectedMediaIds = []
         onSelectedMediaChange()
   })


   loadMoreMediaItems()
}

var hasMoreMediaItems = true
var _mediaItemsOffset = 0;
function loadMoreMediaItems(){

    $.mobile.loading( "show", {
                text: "Loading. PLease wait",
                textVisible: false,
    });

    $.get("/api/media-stream?offset="+_mediaItemsOffset+"&limit=10")
        .success(function(data) {
            hasMoreMediaItems = data.mediaResourceIds.length == data.paging.limit
            _mediaItemsOffset += data.mediaResourceIds.length
            console.log("Has more elements:"+hasMoreMediaItems)
            for (i = 0; i < data.mediaResourceIds.length; i++) {
                onNewMediaItem(data.mediaResourceIds[i])
            }
            onNewMediaItem(null)
        })
        .error(function() { alert("Error during loading media-stream"); })
        .complete(function() {
            $.mobile.loading( "hide" );
        });
}

function onNewMediaItem(mediaResource){
    if (mediaResource == null){
        onMedia(null)
        return
    }
    var creationDate = new Date(mediaResource.creationDate);

    var sortingDate  = new Date(
        creationDate.getFullYear(),
        creationDate.getMonth(),
        creationDate.getDate(), 0,0,0,0)

    var media = {
        orig:mediaResource,
        sortByDate:sortingDate
    }

    onMedia(media)
}

var firstMediaInACell = null
var selectedPattern = null;

function randomInteger(min, max) {
    var rand = min - 0.5 + Math.random() * (max - min + 1)
    rand = Math.round(rand);
    return rand;
}


var content
function onMedia(media){

    if (media == null) {
        if (hasMoreMediaItems){
            var panel = content
            if (panel == null){
                panel = $("#panel_image")
            }
            panel.append(
                $('<div>')
                    .attr("id", "element_waypoint")
            )
            var waypoint = new Waypoint({
              element: document.getElementById('element_waypoint'),
              handler: function(direction) {
                //remove
                waypoint.destroy()
                $("#element_waypoint").remove();
                loadMoreMediaItems()
              },
              offset: '100%'
            })
        }
        return
    }

    if (selectedPattern == null){
        selectedPattern = randomInteger(0,2)
    }
    var cellSize = thumbnailCellSize()
    var thumbnailsPanelWidth = Math.floor(pageWidth()/cellSize.width) * cellSize.width

    var panel_image = $("#panel_image")
    var groupId = "panel_thumbnails_" + media.sortByDate.getTime();
    var panel_thumbnails;
    if ( $( "#"+groupId ).length ) {
        panel_thumbnails =  $( "#"+groupId )
    } else {
        panel_thumbnails = $('<div>').attr("id",groupId)
            .append(
                $('<h4>')
                    .addClass("ui-bar")
                    .addClass("ui-bar-a")
                    .text(media.sortByDate.toDateString())
            )
            .append(
                 $('<div>')
                    .addClass("ui-body")
                    .addClass('panel-thumbnails')
                    .width(thumbnailsPanelWidth)
            )

        panel_image.append(panel_thumbnails)
    }

    content = panel_thumbnails.children("div");
    content.append(
        $('<div>')
            .attr("id","thumbnail_"+media.orig.id)
            .addClass("panel-thumbnail")
            .width(cellSize.width - 8)
            .height(cellSize.height - 8)
            .append(
                $('<img>')
                    .attr("src","api/thumbnail/"+media.orig.id+"?width="+MAX_THUMBNAIL_CELL_SIZE+"&height="+MAX_THUMBNAIL_CELL_SIZE)

            )
            .append (
                $('<div>')
                    .on( "taphold", function( event ) {
                       vibrate(100)
                       onThumbnailLongPress(media.orig.id)
                    } )
                   .on( "tap", function( event ) {
                          onThumbnailPress(media.orig.id)
                    } )

            )
    )
}

function vibrate(ms){
   if ('vibrate' in navigator) {
      navigator.vibrate(ms);
   }
}

var selectedMediaIds = []
function onThumbnailPress(mediaId){
    if (selectedMediaIds.length == 0){
        console.log("Single click is not implemented")
    } else {
        vibrate(50)
        ui_updateByIdOrIds(selectedMediaIds, ui_thumbnail_deSelectById)
        selectedMediaIds = []
        onSelectedMediaChange()
    }
}

function onThumbnailLongPress(mediaId){
    var mediaIdIndex = $.inArray(mediaId, selectedMediaIds)
    if (mediaIdIndex == -1){
        selectedMediaIds.push(mediaId)
        ui_thumbnail_selectById(mediaId)
    } else {
        selectedMediaIds.splice(mediaIdIndex,1)
        ui_thumbnail_deSelectById(mediaId)
    }
    onSelectedMediaChange()
}

function ui_updateByIdOrIds(idOrIds, updateFunction){
    if ($.isArray(idOrIds)){
        $.each(idOrIds, function( index, value ) {
            updateFunction(value);
          })
    } else {
        updateFunction(idOrIds);
    }
}

function onSelectedMediaChange(){
    if (selectedMediaIds.length != 0){
        showHeader()
        showFooter()
        $('#drop-selection-btn').show()
    } else {
        hideHeader()
        hideFooter()
        $('#drop-selection-btn').hide()
    }
    $('#selected-counter-text').text(selectedMediaIds.length)
}

function ui_thumbnail_deSelectById(id){
    $("#thumbnail_"+id+" div").removeClass("selection")
}

function ui_thumbnail_selectById(id){
    $("#thumbnail_"+id+" div").addClass("selection")
}

var lastScrollPosition;

$(document).scroll( function() {
  var scrollPosition = $(this).scrollTop();

  // Scrolling down
  if (scrollPosition > lastScrollPosition){
    hideHeader()
    hideFooter()
  }

  // Scrolling up
  else {
    showHeader()
    showFooter()
  }

  lastScrollPosition = scrollPosition;
});

function hideHeader(){
    // If the header is currently showing
    if (!$('[data-role=header].ui-fixed-hidden').length) {
      $('[data-role=header]').toolbar('hide');
    }
}

function showHeader(){
// If the header is currently hidden
    if ($('[data-role=header].ui-fixed-hidden').length) {
      $('[data-role=header]').toolbar('show');
    }
}

function hideFooter(){
    // If the header is currently showing
    if (!$('[data-role=footer].ui-fixed-hidden').length) {
      $('[data-role=footer]').toolbar('hide');
    }
}

function showFooter(){
// If the header is currently hidden
    if ($('[data-role=footer].ui-fixed-hidden').length) {
      $('[data-role=footer]').toolbar('show');
    }
}