function initialize_browser_module(){

   $(window).resize(function() {
        var cellSize = thumbnailCellSize()
        var thumbnailsPanelWidth = Math.floor(pageWidth()/cellSize.width) * cellSize.width
        $('.panel-thumbnails').each(function() {
                $(this).width(thumbnailsPanelWidth);
        });
   });

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
                $('<h3>')
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
            .width(cellSize.width - 6)
            .height(cellSize.height - 6)
            .append(
                $('<img>')
                    .attr("src","api/thumbnail/"+media.orig.id+"?width="+MAX_THUMBNAIL_CELL_SIZE+"&height="+MAX_THUMBNAIL_CELL_SIZE)
            )
    )
}
