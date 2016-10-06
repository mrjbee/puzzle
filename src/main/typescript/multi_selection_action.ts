
/**
 * PageHandler
 */
interface ActionPageHandler {
    onLoad(selectedResourceIDs:string[])
    onAccept(callback:(success:boolean) => void)
}

/**
 * TagEditorPageHandler
 */
class TagEditorPageHandler implements ActionPageHandler {

    private commonTag = {}
    private commonTagRemove = {}

    onAccept(callback:(success:boolean) => void){
        
            var body = {
                       "mediaIds": selectedMediaIds,
                       "assignTags": [],
                       "removeTags": []
                   }

            for (var tag in this.commonTag){
                body.assignTags.push({
                    name:tag,
                    color:this.commonTag[tag]
                })
                allTagsMap[tag] = {
                    name:tag,
                    color:this.commonTag[tag]
                }
            }

            //Update common tags with new colors and tags
            for (var tag in this.commonTagRemove){
                if (!(tag in this.commonTag)){
                    body.removeTags.push({
                        name:tag,
                        color:this.commonTagRemove[tag]
                    })
                }
            }

            //Update selected media with changes
            $.each(selectedMediaIds, (selectedId) => {
                var id = selectedMediaIds[selectedId]
                $.each(this.commonTag, (key,val) =>{

                    var index = $.inArray(key,$.map(fetchedMedia[id].tags, (elem) =>{
                        return elem.name
                    }))
                    if (index == -1){
                        fetchedMedia[id].tags.push({
                                                   name: key,
                                                   color:val
                                              })
                    } else {
                        fetchedMedia[id].tags[index].color = val
                    }
                })

                $.each(body.removeTags, (itag, tag) =>{
                    var index = $.inArray(tag.name,$.map(fetchedMedia[id].tags, (elem) =>{
                        return elem.name
                    }))
                    if (index != -1){
                        fetchedMedia[id].tags.splice(index,1)
                    }
                })

            })

            $.ajax({
                url: '/api/tags/update',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(body),
                success: function(result) {
                    //TODO: update content dash if required
                },
                error: function(result) {
                    callback(false)
                },
                complete: function() {
                    callback(true) 
                }
            });        
    }

    //TODO: deals with fetchedMedia
    onLoad(selectedResourceIDs:string[]){

        $('#new-tag-btn').click(function(){
            var newTagTitle = $('#new-tag-title-edit').val().toLowerCase();
            var newTagColor = $("#new-tag-color-option option:selected" ).text().toLowerCase()
            this.onNewCommonTag(newTagTitle, newTagColor);
            return false;
        })

        this.onAllTagsChanged()

        this.commonTag = {}
        this.commonTagRemove = {}
        var thumbnailContentPanel = $('#panel_tags_images')
        thumbnailContentPanel
            .empty()
            .width(THUMBNAILS_MATH.calculateThumbnailsPanelWidth())
        var tagsCountMap = {}
        for (var i = 0; i < selectedMediaIds.length; i++) {
        
            var assignedTags = fetchedMedia[selectedMediaIds[i]].tags

            for(var j=0; j < assignedTags.length; j++){
                if (assignedTags[j].name in tagsCountMap) {
                    tagsCountMap[assignedTags[j].name] = tagsCountMap[assignedTags[j].name] + 1
                } else {
                    tagsCountMap[assignedTags[j].name] = 1
                }
            }

            UiCommons.build(new ThumnailsPanelBuilder("tags_thumbnail")
                .withMedia(
                    UiCommons.describeMedia()
                        .withId(selectedMediaIds[i])
                        .withType(fetchedMedia[selectedMediaIds[i]].type as string))
                .withParent(thumbnailContentPanel))
        }

        for (var itTag in tagsCountMap) {
            if (tagsCountMap[itTag] == selectedMediaIds.length) {
                this.commonTag[itTag] = allTagsMap[itTag].color
            }
        }
        this.onCommonTagsChanged()    
    }

    private onNewCommonTag(title:string, color:string){
        this.commonTag[title] = color
        console.log("New tag"+title)
        this.onCommonTagsChanged()
    }

    private onRemoveCommonTag(title:string, color:string){
        this.commonTagRemove[title] = color
        delete this.commonTag[title]
        this.onCommonTagsChanged()
    }


    private onCommonTagsChanged(){
        var tagsPanel = $("#common-tag-panel")
        tagsPanel.empty()
        $.each(this.commonTag, (key, val) => {
                tagsPanel.append(
                    $('<div>')
                        .attr("id","common_tag_"+key)
                        .text(key)
                        .addClass("ui-page-theme-b")
                        .addClass("ui-btn-b")
                        .addClass("ui-corner-all")
                        .addClass("tag")
                        .addClass("tag-color-"+val)
                        .on( "tap", (event) => {
                            this.onRemoveCommonTag(key, val)
                        } )
                )
            })
    }

    //TODO direct access to allTagsMap
    private onAllTagsChanged(){
        var tagsPanel = $("#all-tag-panel")
        tagsPanel.empty()
        $.each(allTagsMap, (key, val) => {
            tagsPanel.append(
                $('<div>')
                    .attr("id","common_tag_"+key)
                    .text(key)
                    .addClass("ui-page-theme-b")
                    .addClass("ui-btn-b")
                    .addClass("ui-corner-all")
                    .addClass("tag")
                    .addClass("tag-color-"+val.color)
                    .on( "tap", (event) => {
                        this.onNewCommonTag(key, val.color)
                    } )
            )
        })
    }
}

/**
 * DeleteResourcesPageHandler
 */
class DeleteMediaPageHandler implements ActionPageHandler {

    onAccept(callback:(success:boolean) => void){
        var resultsPromiseMap = {}
        var mediaRemoveIds = selectedMediaIds.slice();
        mediaRemoveIds.forEach((mediaId,index)=>{
            resultsPromiseMap[mediaId] = {
                result:0
            }

            $.ajax({
                url: '/api/media/'+mediaId,
                type: 'DELETE',
                success: function(result) {
                    resultsPromiseMap[mediaId].result = 1
                    $("#delete_thumbnail_"+mediaId).remove();
                    $("#thumbnail_"+mediaId).remove();
                    var mediaIdIndex = $.inArray(mediaId, selectedMediaIds)
                    selectedMediaIds.splice(mediaIdIndex,1)
                    onSelectedMediaChange()
                },
                error: function(result) {
                    resultsPromiseMap[mediaId].result = 2
                },
                complete: function() {
                    var answer = true;
                    for (var id in resultsPromiseMap) {
                        if(resultsPromiseMap[id].result == 0){
                            return
                        }
                        if (resultsPromiseMap[id].result == 2){
                            answer = false
                        }
                    }
                    callback(answer)
                }
            });
        })
    }

    onLoad(selectedResourceIDs:string[]){
        var thumbnailContentPanel = $('#panel_delete_images')
        thumbnailContentPanel.empty()
        thumbnailContentPanel.width(THUMBNAILS_MATH.calculateThumbnailsPanelWidth())
        for (var i = 0; i < selectedMediaIds.length; i++) {
            UiCommons.build(new ThumnailsPanelBuilder("delete_thumbnail")
                .withMedia(
                    UiCommons.describeMedia()
                        .withId(selectedMediaIds[i])
                        .withType(fetchedMedia[selectedMediaIds[i]].type as string))
                .withParent(thumbnailContentPanel))
        }
    }
}

/**
 * MultiSelectionAction
 */
class MultiSelectionAction {

    static ACTION_TAG_EDITOR = new MultiSelectionAction(
                "tagEditor",
                "Tag Editor",
                new TagEditorPageHandler())

    static ACTION_REMOVE_RESOURCES = new MultiSelectionAction(
                "deleteResources",
                "Delete Selected Media",
                new DeleteMediaPageHandler())

            
    private _id:string;
    private _humanName:string;
    private _actionPageHandler:ActionPageHandler
    
    constructor(id:string, humanName:string, pageHandler:ActionPageHandler){
        this._id = id
        this._humanName = humanName
        this._actionPageHandler = pageHandler
    }

    get id():string {
        return this._id
    }

    get humanName():string {
        return this._humanName
    }

    get actionPageHandler():ActionPageHandler {
        return this._actionPageHandler
    }

    generatePageUrl():string{
        return `pages/${this.id}Page.html`
    }
}