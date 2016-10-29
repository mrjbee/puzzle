class TagFilter {
    
    private _tag:Tag;
    
    constructor(tag:Tag){
        this._tag = tag
    }

    get tag(){
        return this._tag
    }
}

/**
 * FilterManager
 */
class FilterManager {
   
    private _tagFilters : TagFilter[] = [];
    
    addTagFilter(tag:Tag){        
        let filter = this.findTagFilterByTag(tag)
        if (!filter){
            this._tagFilters.push(new TagFilter(tag))
        }
    }

    removeTagFilter(tag:Tag){
        
        let filter = this.findTagFilterByTag(tag)

        if (filter){
            this._tagFilters.splice(
                this._tagFilters.indexOf(filter),1
            )
        } 
    }

    findTagFilterByTag(tag:Tag){
        let existingFilters = this._tagFilters.filter((tagFilter:TagFilter)=>{
            return tagFilter.tag.name() == tag.name()
        })
        if (existingFilters.length > 0){
            return existingFilters[0]
        } else {
            return null
        }
    }
    asTagsFilterQuery():string{
        return this._tagFilters.map((tagFilter)=>{return tagFilter.tag.name()}).join(",")
    }
}