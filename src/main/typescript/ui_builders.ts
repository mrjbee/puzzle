/// <reference path="../../../typings/index.d.ts" />

/**
 * UiCommons
 */
class UiCommons {

    static build(builder:UIBuilder):JQuery{
        return builder.buildUI()
    }

    static describeMedia():ThumnailsMediaDescriptor{
        return new ThumnailsMediaDescriptor()
    }    
}


/**
 * Builder
 */
interface UIBuilder {
    buildUI():JQuery
}

abstract class ParentAwareBuilder implements UIBuilder {
    
    private _parent:JQuery

    withParent(elem:JQuery):ParentAwareBuilder {
        this._parent = elem
        return this
    } 

    abstract buildChilds():JQuery[]

    buildUI():JQuery{
        var elems = this.buildChilds()
        elems.forEach((elem)=>this._parent.append(elem))
        return this._parent
    }

}

class ThumnailsMediaDescriptor {
 
    _mediaId:string;
    _mediaType:string;
    
    withId(id:string):ThumnailsMediaDescriptor{
        this._mediaId = id;
        return this;
    }
    
    withType(type:string):ThumnailsMediaDescriptor{
        this._mediaType = type;
        return this;
    }
    
}



/**
 * ThumnailsPanelBuilder
 */
class ThumnailsPanelBuilder extends ParentAwareBuilder{
    
    private _media: ThumnailsMediaDescriptor
    private _prefix:String;
    private _tapFunction:(mediaId:string, event:Event) => void 
    private _tapholdFunction:(mediaId:string, event:Event) => void 

    constructor(idPrefix:string) {
        super()   
        this._prefix = idPrefix;
    }

    buildChilds():JQuery[]{
        var answer : JQuery[] = []
        answer.push(
            $('<div>')
                .attr("id",`${this._prefix}_${this._media._mediaId}`)
                .addClass("panel-thumbnail")
                .width(THUMBNAILS_MATH.cellWidth - 8)
                .height(THUMBNAILS_MATH.cellHeight - 8)
                .append(
                    $('<img>')
                        .attr("src","api/thumbnail/"+this._media._mediaId+"?width="+ThumbnailMath.MAX_THUMBNAIL_CELL_SIZE+"&height="+ThumbnailMath.MAX_THUMBNAIL_CELL_SIZE)

                )
                .append (
                    $('<div>')
                        .on( "taphold",(event) =>{
                            if (this._tapholdFunction != null){
                                this._tapholdFunction(this._media._mediaId, event)
                            }
                        }
                        ) 
                    .on( "tap", (event) => {
                            if (this._tapFunction != null){
                                this._tapFunction(
                                    this._media._mediaId, event)
                            }
                        } 
                    )
                )
                .append (
                    $('<div>')
                        .addClass("ui-page-theme-b")
                        .addClass("ui-btn-b")
                        .addClass("ui-corner-all")
                        .addClass("thumbnail_tooltip_"+this._media._mediaType.toUpperCase())
                        .text(+this._media._mediaType.toLocaleLowerCase())
                )
        )
        return answer;
    }

    withMedia(media:ThumnailsMediaDescriptor):ThumnailsPanelBuilder{this._media = media; return this}
    withTapFunction(func:(mediaID:string, event:Event) => void):ThumnailsPanelBuilder{this._tapFunction = func; return this}
    withTapholdFunction(func:(mediaID:string, event:Event) => void):ThumnailsPanelBuilder{this._tapholdFunction = func; return this}
}