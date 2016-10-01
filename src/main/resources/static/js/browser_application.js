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

   $( "#pageDelete" ).on( "pagebeforeshow", function( event ) {
        onDeletePopupShow(selectedMediaIds)
   } )

    $('#delete-selected-approved-btn').click(function(){
        onDeleteResourcesAccepted()
        return false;
    })

    $('#open-selection-btn').click(function(){
        for (i = 0; i < selectedMediaIds.length; i++) {
            openMediaInTab(selectedMediaIds[i])
        }
        parent.history.back();
        return false;
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
            $("#total-counter-text").text(data.paging.actualCount)
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
                       onThumbnailLongPress(media.orig.id)
                    } )
                   .on( "tap", function( event ) {
                          onThumbnailPress(media.orig.id)
                    } )
            ).append (
                $('<div>')
                    .addClass("ui-page-theme-b")
                    .addClass("ui-btn-b")
                    .addClass("ui-corner-all")
                    .addClass("thumbnail_tooltip_"+media.orig.type)
                    .text(media.orig.type.toLowerCase())
            )
    )
}

function vibrate(ms){
   if ('vibrate' in navigator) {
      navigator.vibrate(ms);
   }
}

function openMediaInTab(mediaId){
        var url = window.location.origin+'/api/media/'+mediaId
        var win = window.open(url, '_blank');
        /*if (win) {
            //Browser has allowed it to be opened
            win.focus();
        } else {
            //Browser has blocked it
            alert('Please allow popups for this website');
        }*/
}

var selectedMediaIds = []
function onThumbnailPress(mediaId){
    if (selectedMediaIds.length == 0){
        openMediaInTab(mediaId)
    } else {
        vibrate(50)
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
}

function onThumbnailLongPress(mediaId){
    vibrate(100)
    if (selectedMediaIds.length == 0){
        selectedMediaIds.push(mediaId)
        ui_thumbnail_selectById(mediaId)
    } else {
        ui_updateByIdOrIds(selectedMediaIds, ui_thumbnail_deSelectById)
        selectedMediaIds = []
        onSelectedMediaChange()
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
        $('#selection-actions-btn-group').show()
    } else {
        hideHeader()
        $('#selection-actions-btn-group').hide()
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
  }

  // Scrolling up
  else {
    showHeader()
  }

  lastScrollPosition = scrollPosition;
});

function hideHeader(){
    // If the header is currently showing
    if (!$('#pageDashboard [data-role=header].ui-fixed-hidden').length) {
      $('#pageDashboard [data-role=header]').toolbar('hide');
    }
}

function showHeader(){
// If the header is currently hidden
    if ($('#pageDashboard [data-role=header].ui-fixed-hidden').length) {
      $('#pageDashboard [data-role=header]').toolbar('show');
    }
}

function onDeletePopupShow(selectedMediaIds){
    var deleteThumbnailPanel = $('#panel_delete_images')
    deleteThumbnailPanel.empty()
    var cellSize = thumbnailCellSize()
    var thumbnailsPanelWidth = Math.floor(pageWidth()/cellSize.width) * cellSize.width
    deleteThumbnailPanel.width(thumbnailsPanelWidth)
    for (i = 0; i < selectedMediaIds.length; i++) {
        deleteThumbnailPanel.append(
            $('<div>')
                .attr("id","delete_thumbnail_"+selectedMediaIds[i])
                .addClass("panel-thumbnail")
                .width(cellSize.width - 8)
                .height(cellSize.height - 8)
                .append(
                    $('<img>')
                        .attr("src","api/thumbnail/"+selectedMediaIds[i]+"?width="+MAX_THUMBNAIL_CELL_SIZE+"&height="+MAX_THUMBNAIL_CELL_SIZE)

                )
                .append (
                    $('<div>')
                        .on( "taphold", function( event ) {
                           //vibrate(100)
                           //onThumbnailLongPress(media.orig.id)
                        } )
                       .on( "tap", function( event ) {
                             // onThumbnailPress(media.orig.id)
                        } )

                )
        )
    }
}

function onDeleteResourcesAccepted(){
    var resultsPromiseMap = {}
    var mediaRemoveIds = selectedMediaIds.slice();
    for (i = 0; i < mediaRemoveIds.length; i++) {
        resultsPromiseMap[mediaRemoveIds[i]] = {
            result:0
        }

        $.ajax({
            resourceId: i,
            url: '/api/media/'+mediaRemoveIds[i],
            type: 'DELETE',
            success: function(result) {
                resultsPromiseMap[mediaRemoveIds[this.resourceId]].result = 1
                $("#delete_thumbnail_"+mediaRemoveIds[this.resourceId]).remove();
                $("#thumbnail_"+mediaRemoveIds[this.resourceId]).remove();
                var mediaIdIndex = $.inArray(mediaRemoveIds[this.resourceId], selectedMediaIds)
                selectedMediaIds.splice(mediaIdIndex,1)
                onSelectedMediaChange()
            },
            error: function(result) {
                resultsPromiseMap[mediaRemoveIds[this.resourceId]].result = 2
            },
            complete: function() {
                for (var id in resultsPromiseMap) {
                    if(resultsPromiseMap[id].result == 0){
                        return
                    }
                }
                parent.history.back();
            }
        });
    }


}