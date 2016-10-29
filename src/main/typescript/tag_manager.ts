/**
 * Tag
 */
class Tag {

    private _name:string;
    private _type:string;
    
    constructor(name:string, type:string) {
        this._name = name
        this._type = type
    }

    updateType(type:string){
        this._type = type
    }

    type():string {
        return this._type
    }

    name():string{
        return this._name
    }
}


/**
 * TagManager
 */
class TagManager {
    
    private _tags : { [key:string]:Tag; } = {};
    private _onTagsChanged:() => void 

    set onTagsChangedCallback(callback:() => void){
        this._onTagsChanged = callback
    }

    tag(name:string):Tag {
      return this._tags[name]
    }

    updateTag(tag:Tag){
        this._tags[tag.name()] = tag
    }

    each(func:(tag:Tag) => void ){
       for (var key in this._tags){
           func(this._tags[key])
       }
    }
    
    notifyOnTagsChanged() {
        this._onTagsChanged()
    }

    tagCount() : number {
        return Object.keys(this._tags).length
    }
}