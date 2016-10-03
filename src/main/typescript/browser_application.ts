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

    $( "#pageTagsEditor" ).on( "pagebeforeshow", function( event ) {
        onTagPopupShow(selectedMediaIds)
    } )

    $('#new-tag-btn').click(function(){
        var newTagTitle = $('#new-tag-title-edit').val().toLowerCase();
        var newTagColor = $("#new-tag-color-option option:selected" ).text().toLowerCase()
        onNewCommonTag(newTagTitle, newTagColor);
        return false;
    })

    $('#apply-tags-btn').click(function(){
        onApplyTags()
        return false;
    })

    $('#open-selection-btn').click(function(){
        for (i = 0; i < selectedMediaIds.length; i++) {
            openMediaInTab(selectedMediaIds[i])
        }
        parent.history.back();
        return false;
    })

    $.get("/api/tags")
        .success(function(data) {
            allTagsMap = {}
            for (i = 0; i< data.length; i++){
                allTagsMap[data[i].name] = data[i]
            }
            onAllTagsChanged()
        })
        .error(function() { alert("Error during loading tags"); });

    loadMoreMediaItems()
}

var allTagsMap = {}
var hasMoreMediaItems = true
var _mediaItemsOffset = 0;
function loadMoreMediaItems(){

    if (_mediaItemsOffset == 0){
        fetchedMedia = {};
    }

    $.mobile.loading( "show", {
                text: "Loading. PLease wait",
                textVisible: false,
    });

    $.get("/api/media-stream?offset="+_mediaItemsOffset+"&limit=50")
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

var fetchedMedia = {}

function onNewMediaItem(mediaResource){

    if (mediaResource == null){
        onMedia(null)
        return
    }

    fetchedMedia[mediaResource.id] = mediaResource
    var creationDate = new Date(mediaResource.creationDate);

    var sortingDate  = new Date(
        creationDate.getFullYear(),
        creationDate.getMonth(),0,0,0,0,0)

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
                    .text(moment(media.sortByDate).format("MMMM YYYY"))
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

function onTagPopupShow(selectedMediaIds){
    commonTag = {}
    commonTagRemove = {}
    var deleteThumbnailPanel = $('#panel_tags_images')
    deleteThumbnailPanel.empty()
    var cellSize = thumbnailCellSize()
    var thumbnailsPanelWidth = Math.floor(pageWidth()/cellSize.width) * cellSize.width
    deleteThumbnailPanel.width(thumbnailsPanelWidth)
    var tagsCountMap = {}
    for (i = 0; i < selectedMediaIds.length; i++) {
        var assignedTags = fetchedMedia[selectedMediaIds[i]].tags

        for(j=0; j < assignedTags.length; j++){
            if (assignedTags[j].name in tagsCountMap) {
                tagsCountMap[assignedTags[j].name] = tagsCountMap[assignedTags[j].name] + 1
            } else {
                tagsCountMap[assignedTags[j].name] = 1
            }
        }

        deleteThumbnailPanel.append(
            $('<div>')
                .attr("id","tags_thumbnail_"+selectedMediaIds[i])
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

    for (tag in tagsCountMap) {
        if (tagsCountMap[tag] == selectedMediaIds.length) {
            commonTag[tag] = allTagsMap[tag].color
        }
    }
    onCommonTagsChanged()
}


function onApplyTags(){

            var body = {
                       "mediaIds": selectedMediaIds,
                       "assignTags": [],
                       "removeTags": []
                   }

            for (tag in commonTag){
                body.assignTags.push({
                    name:tag,
                    color:commonTag[tag]
                })
                allTagsMap[tag] = {
                    name:tag,
                    color:commonTag[tag]
                }
            }

            //Update common tags with new colors and tags
            for (tag in commonTagRemove){
                if (!(tag in commonTag)){
                    body.removeTags.push({
                        name:tag,
                        color:commonTagRemove[tag]
                    })
                }
            }

            //Update selected media with changes
            $.each(selectedMediaIds, function(selectedId){
                var id = selectedMediaIds[selectedId]
                $.each(commonTag, function(key,val){

                    var index = $.inArray(key,$.map(fetchedMedia[id].tags, function(elem){
                        return elem.name
                    }))
                    if (index == -1){
                        fetchedMedia[id].tags.push({
                                                   name: key,
                                                   color:val
                                              })
                    } else {
                        fetchedMedia[id].tags[index].color = val
                    }
                })

                $.each(body.removeTags, function(itag, tag){
                    var index = $.inArray(tag.name,$.map(fetchedMedia[id].tags, function(elem){
                        return elem.name
                    }))
                    if (index != -1){
                        fetchedMedia[id].tags.splice(index,1)
                    }
                })

            })

            $.ajax({
                url: '/api/tags/update',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(body),
                success: function(result) {
                    //TODO: update content dash if required
                },
                error: function(result) {
                    alert(result)
                },
                complete: function() {
                    parent.history.back();
                }
            });
            onAllTagsChanged()
}

var commonTag = {}
function onNewCommonTag(title, color){
    commonTag[title] = color
    console.log("New tag"+title)
    onCommonTagsChanged()
}

var commonTagRemove = {}
function onRemoveCommonTag(title, color){
    commonTagRemove[title] = color
    delete commonTag[title]
    onCommonTagsChanged()
}


function onCommonTagsChanged(){
    var tagsPanel = $("#common-tag-panel")
    tagsPanel.empty()
    $.each(commonTag,function(key, val){
            tagsPanel.append(
                $('<div>')
                    .attr("id","common_tag_"+key)
                    .text(key)
                    .addClass("ui-page-theme-b")
                    .addClass("ui-btn-b")
                    .addClass("ui-corner-all")
                    .addClass("tag")
                    .addClass("tag-color-"+val)
                    .on( "tap", function( event ) {
                          onRemoveCommonTag(key, val)
                    } )
            )
        })
}

function onAllTagsChanged(){
    var tagsPanel = $("#all-tag-panel")
    tagsPanel.empty()
    $.each(allTagsMap,function(key, val){
        tagsPanel.append(
            $('<div>')
                .attr("id","common_tag_"+key)
                .text(key)
                .addClass("ui-page-theme-b")
                .addClass("ui-btn-b")
                .addClass("ui-corner-all")
                .addClass("tag")
                .addClass("tag-color-"+val.color)
                .on( "tap", function( event ) {
                      onNewCommonTag(key, val.color)
                } )
        )
    })
}
