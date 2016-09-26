var cellSizeLayoutIndex = 0;
var cellSizesPerLayout = []

$(window).resize(function() {
    var cellSize = currentCellSizeLayout()
    var thumbnailsPanelWidth = Math.floor(panelImageWidth()/cellSize.width) * cellSize.width

    $('.center-panel').each(function() {
            $(this).width(thumbnailsPanelWidth);
    });
});

$(document).ready(function() {
    cellSizesPerLayout.push(bigGridLayoutSaleSize())
    loadMoreMediaItems(0)
});

function currentCellSizeLayout(){
    return cellSizesPerLayout[cellSizeLayoutIndex]
}

function panelImageWidth(){
    return $(window).width();
}

function bigGridLayoutSaleSize(){
    var pageWidth = panelImageWidth()
    var cellWidth = Math.floor(pageWidth / 2)
    if (cellWidth > 300) {
        cellWidth = 300
    }

    return {
        width:cellWidth,
        height:Math.round(cellWidth * 0.8)
    }
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
        })
        .error(function() { alert("Error during loading media-stream"); })
        .complete(function() {
            $.mobile.loading( "hide" );
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
    var cellSize = currentCellSizeLayout()
    var thumbnailsPanelWidth = Math.floor(panelImageWidth()/cellSize.width) * cellSize.width
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
        .attr("src","api/thumbnail/"+media.orig.id+"?width="+(cellSize.width-2)+"&height="+(cellSize.height-2)))
    content.append(thumbnailPanel)
}