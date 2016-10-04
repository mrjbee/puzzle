/// <reference path="../../../typings/index.d.ts" />

/**
 * ThumbnailMath
 */
class ThumbnailMath {
    
    static MAX_THUMBNAIL_CELL_SIZE = 300

    private _pageWidth:number
    private _cellWidth:number
    private _cellHeight:number

    constructor(width:number) {
        this._pageWidth = width
        this._cellWidth = Math.floor(width / 2)
        if (this._cellWidth > ThumbnailMath.MAX_THUMBNAIL_CELL_SIZE) {
            this._cellWidth = ThumbnailMath.MAX_THUMBNAIL_CELL_SIZE
        }
        this._cellHeight = this._cellWidth * 0.8    
    }

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
        this._pageWidth = width
    }

    calculateThumbnailsPanelWidth():number{
        return Math.floor(this.pageWidth/this.cellWidth) * this.cellWidth
    }

}