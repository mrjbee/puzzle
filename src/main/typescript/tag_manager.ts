/**
 * Tag
 */
class Tag {

    private _name:string;
    private _color:string;

    constructor(name:string, color:string) {
        this._name = name
        this._color = color
    }

    updateColor(color:string){
        this._color = color
    }

    color():string {
        return this._color
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
}