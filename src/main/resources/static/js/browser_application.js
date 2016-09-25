$(document).ready(function() {

/*
     $.mobile.loading( "show", {
                text: "Loading. PLease wait",
                textVisible: false,
     });
    $.mobile.loading( "hide" );
*/
    loadMoreMediaItems(0)
    });


var hasMoreMediaItems = true

function loadMoreMediaItems(offsetIndex){
    $.get("/api/media-stream?offset="+offsetIndex)
        .success(function(data) {
            hasMoreMediaItems = data.mediaResourceIds.length == data.paging.limit
            console.log("Has more elements:"+hasMoreMediaItems)
            for (i = 0; i < data.mediaResourceIds.length; i++) {
                onNewMediaItem(data.mediaResourceIds[i])
            }
        })
        .error(function() { alert("Error during loading media-stream"); })
        .complete(function() {
        });
}

function onNewMediaItem(mediaResource){
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

function onMedia(media){
    var groupId = "sort_date_panel_" + media.sortByDate.getTime();
    //console.log(sortDateMs+": "+media.sortByDate.toDateString())
    var mediaPanel;
    if ( $( "#"+groupId ).length ) {
        var mediaPanel =  $( "#"+groupId )
    } else {
        var mediaPanel = $('<div>').attr("id",groupId);
        var mediaPanelTitle = $('<h3>')
            .addClass("ui-bar")
            .addClass("ui-bar-a")
            .addClass("ui-corner-all")
            .text(media.sortByDate.toDateString());
        var mediaContentPanel = $('<div>').addClass("ui-body")
        mediaPanel.append(mediaPanelTitle)
        mediaPanel.append(mediaContentPanel)
        $("#panel_image").append(mediaPanel)
    }
    //TODO replace with selector
    var content = mediaPanel.children("div");

    var thumbnailPanel = $('<div>')
        .attr("id","thumbnail_"+media.orig.id)
        .addClass("floating-box");
    thumbnailPanel.append($('<img>')
        .attr("src","api/thumbnail/"+media.orig.id+"?width=200&height=150"))
    content.append(thumbnailPanel)
}