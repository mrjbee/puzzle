/// <reference path="../../../typings/index.d.ts" />

/**
 * ThumbnailMath
 */
class ThumbnailMath {
    
    static MAX_THUMBNAIL_CELL_SIZE = 300
    static DEFAULT = new ThumbnailMath()
    
    private _pageWidth:number=-1
    private _cellWidth:number
    private _cellHeight:number

    get pageWidth():number{
        return this._pageWidth
    }

    get cellWidth():number{
        return this._cellWidth
    }

    get cellHeight():number{
        return this._cellHeight
    }

    updatePageWidth(width:number){
        if (this._pageWidth == -1){
            this._pageWidth = width
            this._cellWidth = Math.floor(width / 2)
            if (this._cellWidth > ThumbnailMath.MAX_THUMBNAIL_CELL_SIZE) {
                this._cellWidth = ThumbnailMath.MAX_THUMBNAIL_CELL_SIZE
            }
            this._cellHeight = this._cellWidth * 0.8    
        }
        this._pageWidth = width
    }

    calculateThumbnailsPanelWidth():number{
        return Math.floor(this.pageWidth/this.cellWidth) * this.cellWidth
    }

}