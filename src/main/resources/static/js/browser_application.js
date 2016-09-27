function initialize_browser_module(){
   loadMoreMediaItems(0)
}

var hasMoreMediaItems = true

function loadMoreMediaItems(offsetIndex){

    $.mobile.loading( "show", {
                text: "Loading. PLease wait",
                textVisible: false,
    });

    $.get("/api/media-stream?offset="+offsetIndex)
        .success(function(data) {
            hasMoreMediaItems = data.mediaResourceIds.length == data.paging.limit
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

function onMedia(media){
    if (media == null){
        return
    }
    if (selectedPattern == null){
        selectedPattern = randomInteger(0,2)
    }
    var cellSize = thumbnailCellSize()
    var thumbnailsPanelWidth = Math.floor(pageWidth()/cellSize.width) * cellSize.width

    var panel_image = $("#panel_image")
    var groupId = "sort_date_panel_" + media.sortByDate.getTime();
    var mediaPanel;
    if ( $( "#"+groupId ).length ) {
        var mediaPanel =  $( "#"+groupId )
    } else {
        var mediaPanel = $('<div>').attr("id",groupId);
        var mediaPanelTitle = $('<h3>')
            .addClass("ui-bar")
            .addClass("ui-bar-a")
            .text(media.sortByDate.toDateString());
        var mediaContentPanel = $('<div>')
            .addClass("ui-body")
            .addClass('center-panel')
            .width(thumbnailsPanelWidth)
        mediaPanel.append(mediaPanelTitle)
        mediaPanel.append(mediaContentPanel)
        panel_image.append(mediaPanel)
    }

    var content = mediaPanel.children("div");

    var thumbnailPanel = $('<div>')
        .attr("id","thumbnail_"+media.orig.id)
        .addClass("floating-box")
        .width(cellSize.width - 2)
        .height(cellSize.height - 2);
    thumbnailPanel.append($('<img>')
        .attr("src","api/thumbnail/"+media.orig.id+"?width="+MAX_THUMBNAIL_CELL_SIZE+"&height="+MAX_THUMBNAIL_CELL_SIZE)
        .addClass("image-thumbnail"))
    content.append(thumbnailPanel)
}