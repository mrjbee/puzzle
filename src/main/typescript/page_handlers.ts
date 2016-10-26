
interface MultiSelectionPageActionHandler {
    getPageId():string
    onLoad(page:JQuery, selectedResourceIDs:string[])
}

class TagManagerPageHandler implements MultiSelectionPageActionHandler {

    private commonTag = {}
    private commonTagRemove = {}
    private currentPage:JQuery

    getPageId():string{
        return "tag-editor-page"
    }

    onAccept(page:JQuery){
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
                TAG_MANAGER.updateTag(new Tag(
                    tag as string,
                    this.commonTag[tag] as string
                    
                ))
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
                    alert("Error updating tags. Please try later...")   
                },
                complete: function() {
                    parent.history.back();
                }
            });
            TAG_MANAGER.notifyOnTagsChanged();        
    }

    //TODO: deals with fetchedMedia
    onLoad(page:JQuery, selectedResourceIDs:string[]){
        this.currentPage = page
        this.currentPage.find('#tag-editor-approved-btn').click(()=>{
            this.onAccept(page)
            return false;
        })

        this.currentPage.find('#new-tag-btn').click(()=>{
            var newTagTitle = this.currentPage.find('#new-tag-title-edit').val().toLowerCase();
            var newTagColor = this.currentPage.find("#new-tag-color-option option:selected" ).text().toLowerCase()
            this.onNewCommonTag(newTagTitle, newTagColor);
            return false;
        })

        this.onAllTagsChanged()

        this.commonTag = {}
        this.commonTagRemove = {}
        var thumbnailContentPanel = this.currentPage.find('#panel_tags_images')
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
                this.commonTag[itTag] = TAG_MANAGER.tag(itTag).color()
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
        var tagsPanel = this.currentPage.find("#common-tag-panel")
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

    //TODO pass TAG_MANAGER on creation ?!?
    private onAllTagsChanged(){
        var tagsPanel = this.currentPage.find("#all-tag-panel")
        tagsPanel.empty()
        TAG_MANAGER.each((tag)=>{
            tagsPanel.append(
                $('<div>')
                    .attr("id","common_tag_"+tag.name())
                    .text(tag.name())
                    .addClass("ui-page-theme-b")
                    .addClass("ui-btn-b")
                    .addClass("ui-corner-all")
                    .addClass("tag")
                    .addClass("tag-color-"+tag.color())
                    .on( "tap", (event) => {
                        this.onNewCommonTag(tag.name(), tag.color())
                    } )
            )
        })
    }
}

/**
 * DeleteResourcesPageHandler
 */
class RemoveMediaPageHandler implements MultiSelectionPageActionHandler {

    getPageId():string{
        return "delete-media-page"
    }

    onAccept(page:JQuery){
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

                    if (!answer){
                        alert("Fail to remove some medias")
                    }
                    parent.history.back()
                }
            });
        })
    }

    onLoad(page:JQuery, selectedResourceIDs:string[]){
        page.find('#remove-media-approved-btn').click(()=>{
            this.onAccept(page)
            return false;
        })
        var thumbnailContentPanel = page.find('#panel_delete_images')
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