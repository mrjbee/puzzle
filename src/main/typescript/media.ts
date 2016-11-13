/// <reference path="../../../typings/index.d.ts" />


class Media {
    
    private _model:any

    constructor(model:any){
        this._model = model    
    }

    id():string{
        return this._model.id
    }

    tags():any {
        return this._model.tags
    }

    type():string{
        return this._model.type
    }
    model():any {
        return this._model;
    }
}


/**
 * MediaLoader
 */
class MediaLoader {

    private _loadAtOnce = 50

    loadMore(startingFrom:number,
             filter:string, 
             onLoad:(medias: Media[], hasMore: boolean, startIndex:number, total:number) => void) {
        
        (<any> $.get("api/media-stream?offset="+startingFrom+"&limit="+this._loadAtOnce+"&tags="+filter))
        .success((data) => {
            let hasMore = data.mediaResourceIds.length == data.paging.limit
            let answer = new Array<Media>();
            for (var i = 0; i < data.mediaResourceIds.length; i++) {
                answer.push(new Media(data.mediaResourceIds[i]))
            }
            onLoad(answer, hasMore, startingFrom, data.paging.actualCount)
        })
        .error(function() { alert("Error during loading media-stream. Please refresh a page"); });
    } 
}

/**
 * MediaRepository
 */
class MediaRepository {

    private _mediaList = new Array<Media>()
    
    constructor() {}

    clear(){
        this._mediaList = new Array<Media>()
    }

    add(media:Media){
        this._mediaList.push(media)
    }

    get(pos:number):Media {
       return this._mediaList[pos]
    }

    delete(pos:number):Media {
       return this._mediaList.splice(pos,1)[0]
    }

    size():number {
        return this._mediaList.length
    }
}

/**
 * MediaIterator
 */
class MediaIterator {

    private _mediaLoader: MediaLoader
    private _mediaRepository: MediaRepository
    private _position = -1
    private _hasMore = true;
    private _filters = ""

    constructor(loader = new MediaLoader(), repository = new MediaRepository()) {
        this._mediaLoader = loader
        this._mediaRepository = repository
    }

    clone(): MediaIterator {
        let answer = new MediaIterator(this._mediaLoader, this._mediaRepository)
        answer._hasMore = this._hasMore
        answer._position = this._position
        answer._filters = this._filters
        return answer;
    }

    applyFilters(filters:string) {
        this._filters = filters
        this._position = -1
        this._hasMore = true
        this._mediaRepository.clear()
    }

    filters():string{
        return this._filters
    }

    dropFilter(){
        this.applyFilters("")
    }

    position() : number {
        return this._position
    }

    seekPrev() : boolean {
        return this.seek(this._position-1)
    }

    seekNext() : boolean {
        return this.seek(this._position+1)
    }

    seek(position:number) : boolean {
        if (position > this.maxSeekPosition()){
            return false
        } else if (position < -1){
            return false
        }
        
        this._position = position
        return true
    }

    maxSeekPosition() : number {
        return this._mediaRepository.size() - 1
    }

    findById(id:string) : [number, Media] {
        for (let i=0; i < this._mediaRepository.size(); i++) {
            let media = this._mediaRepository.get(i);
            if (media.id() == id) {
                return [i, media]
            } 
        }
        return [-1, null]
    }

    delete(id:string):Media{
        let pos = this.findById(id)[0]
        if (pos < 0){
            return null
        } else {
            let answer = this._mediaRepository.delete(pos)        
            if (this._position >= pos){
                this._position += -1
            }    
            return answer
        }
    }

    next(count:number, mediator:(position:number, media:Media, isLast:boolean) => void) {
        this._do_next(this._position+count, mediator)            
    }

    canNext():boolean {
        return this._position < (this._mediaRepository.size() -1)
    }

    private _do_next(until_pos:number, mediator:(position:number, media:Media, isLast: boolean) => void) {
        if (!this.seekNext()){

            if (!this._hasMore){
                mediator(this._position+1, null, true)
                return        
            }

            //try load items
            this._mediaLoader.loadMore(
                this._position + 1,
                this._filters,
                (medias: Media[], hasMore: boolean, startIndex:number, total:number) =>{
                    //add only if not already added (side effect perhaps)
                    if (this.maxSeekPosition() < startIndex){
                        medias.forEach((val:Media) => {
                            this._mediaRepository.add(val)    
                        })        
                        this._hasMore = hasMore
                    }
                    //try again
                    this._do_next(until_pos, mediator)    
                })
        } else {
            
            let finalElement = (this._position == until_pos) 
            mediator(
                this._position, 
                this._mediaRepository.get(this._position),
                finalElement)

            if (!finalElement){
                this._do_next(until_pos, mediator)    
            }

        }

    }
}