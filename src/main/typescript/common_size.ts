var MAX_THUMBNAIL_CELL_SIZE = 300

var _cellSizeLayoutIndex = 0;
var _cellSizesPerLayout = []

function initialize_size_module(){
   _cellSizesPerLayout.push(_initBigGridThumbnailCellSize())
}

function _initBigGridThumbnailCellSize(){
    var cellWidth = Math.floor(pageWidth() / 2)
    if (cellWidth > MAX_THUMBNAIL_CELL_SIZE) {
        cellWidth = MAX_THUMBNAIL_CELL_SIZE
    }
    return {
        width:cellWidth,
        height:Math.round(cellWidth * 0.8)
    }
}

function thumbnailCellSize(){
    return _cellSizesPerLayout[_cellSizeLayoutIndex]
}

function pageWidth(){
    return $(window).width();
}
