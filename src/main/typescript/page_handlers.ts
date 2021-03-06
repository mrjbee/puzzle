
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
                    type:this.commonTag[tag]
                })
                TAG_MANAGER.updateTag(new Tag(
                    tag as string,
                    this.commonTag[tag] as string
                    
                ))
            }

            //Update common tags with new type and tags
            for (var tag in this.commonTagRemove){
                if (!(tag in this.commonTag)){
                    body.removeTags.push({
                        name:tag,
                        type:this.commonTagRemove[tag]
                    })
                }
            }

            //Update selected media with changes
            $.each(selectedMediaIds, (selectedId) => {
                var id = selectedMediaIds[selectedId]
                $.each(this.commonTag, (key,val) =>{

                    var index = $.inArray(key,$.map(MEDIA_ITERATOR.findById(id)[1].tags(), (elem) =>{
                        return elem.name
                    }))
                    if (index == -1){
                        MEDIA_ITERATOR.findById(id)[1].tags().push({
                                                   name: key,
                                                   type:val
                                              })
                    } else {
                        MEDIA_ITERATOR.findById(id)[1].tags()[index].type = val
                    }
                })

                $.each(body.removeTags, (itag, tag) =>{
                    var index = $.inArray(tag.name,$.map(MEDIA_ITERATOR.findById(id)[1].tags(), (elem) =>{
                        return elem.name
                    }))
                    if (index != -1){
                        MEDIA_ITERATOR.findById(id)[1].tags().splice(index,1)
                    }
                })

            })

            $.ajax({
                url: 'api/tags/update',
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

    onLoad(page:JQuery, selectedResourceIDs:string[]){
        this.currentPage = page
        this.currentPage.find('#tag-editor-approved-btn').click(()=>{
            this.onAccept(page)
            return false;
        })

        this.currentPage.find('#new-tag-btn').click((event)=>{
            var newTagTitle = this.currentPage.find('#new-tag-title-edit').val().toLowerCase() as string;
            var newTagType = $('input[name=tag-type-options]:checked').val().toLowerCase()
            if (newTagTitle.trim().length == 0){
                return false;
            }
            this.onNewCommonTag(newTagTitle, newTagType);
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
        
            var assignedTags = MEDIA_ITERATOR.findById(selectedMediaIds[i])[1].tags()

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
                        .withType(MEDIA_ITERATOR.findById(selectedMediaIds[i])[1].type()))
                .withParent(thumbnailContentPanel))
        }

        for (var itTag in tagsCountMap) {
            if (tagsCountMap[itTag] == selectedMediaIds.length) {
                this.commonTag[itTag] = TAG_MANAGER.tag(itTag).type()
            }
        }
        this.onCommonTagsChanged()    
    }

    private onNewCommonTag(title:string, type:string){
        this.commonTag[title] = type
        this.onCommonTagsChanged()
    }

    private onRemoveCommonTag(title:string, type:string){
        this.commonTagRemove[title] = type
        delete this.commonTag[title]
        this.onCommonTagsChanged()
    }


    private onCommonTagsChanged(){
        var tagsPanel = this.currentPage.find("#common-tag-panel")
        tagsPanel.empty()
        $.each(this.commonTag, (key, val) => {
                tagsPanel.append(
                    $('<a>')
                        .attr("id","common_tag_"+key)
                        .attr("href","#")
                        .addClass("ui-btn-icon-left")
                        .addClass("ui-icon-tag")
                        .addClass("ui-btn")
                        .addClass("ui-btn-b")
                        .addClass("ui-shadow")
                        .addClass("ui-corner-all")
                        .addClass("tag")
                        .addClass("tag-type-"+val)
                        .text(key)
                        .on( "tap", (event) => {
                            this.onRemoveCommonTag(key, val)
                        }).trigger('create')      
                )
            })
    }

    //TODO pass TAG_MANAGER on creation ?!?
    private onAllTagsChanged(){
        var tagsPanel = this.currentPage.find("#all-tag-panel")
        tagsPanel.empty()
        TAG_MANAGER.each((tag)=>{
            tagsPanel.append(
                $('<a>')
                    .attr("id","common_tag_"+tag.name())
                    .attr("href","#")
                    .addClass("ui-btn-icon-left")
                    .addClass("ui-icon-tag")
                    .addClass("ui-btn")
                    .addClass("ui-mini")
                    .addClass("ui-btn-b")
                    .addClass("ui-shadow")
                    .addClass("ui-corner-all")
                    .addClass("tag")
                    .addClass("tag-type-"+tag.type())
                    .text(tag.name())
                    .on( "tap", (event) => {
                        this.onNewCommonTag(tag.name(), tag.type())
                    }).trigger('create')
            )
        })
        tagsPanel.append(
                $('<a>')
                    .attr("href","#new-tag-panel")
                    .attr("data-transition","slidedown")
                    .addClass("ui-btn-icon-left")
                    .addClass("ui-mini")
                    .addClass("ui-icon-plus")
                    .addClass("ui-btn")
                    .addClass("ui-btn-a")
                    .addClass("ui-shadow")
                    .addClass("ui-corner-all")
                    .addClass("tag")
                    .text("New Tag ...")
                    .trigger('create')
            )
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
                url: 'api/media/'+mediaId,
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
                        .withType(MEDIA_ITERATOR.findById(selectedMediaIds[i])[1].type()))
                .withParent(thumbnailContentPanel))
        }
    }
}

/**
 * MediaPreviewPage
 */
class MediaPreviewPage {
    
    private mediaIterator: MediaIterator;
    private page:JQuery
    private curMedia:Media
    private updateDashboardRequest = false

    constructor(page:JQuery, iter: MediaIterator) {
        this.mediaIterator = iter;
        this.page = page
    }

    _loadNextAndSetImage(onResult:(hasNext:boolean, hasPrev:boolean) => void){
        this.page.find("#media-preview")
              .removeAttr("src")
        let hasNextAnswer = true;      
        this.mediaIterator.next(1,(position, media) => {
            if (media == null){
                alert("Sory no media more")
            } else{
                this.curMedia = media;
                this.page.find("#media-preview")
                    .attr("src","api/thumbnail/"+media.id()+"?width="+window.innerWidth+"&height="+window.innerHeight)
                
                this.mediaIterator.next(1,(position, media) => {
                    this.mediaIterator.seek(position - 1)
                    onResult(media != null, position > 1)
                })

            }
        }) 
        
    }

    _updateNvaigationUI(hasNext:boolean, hasPrev:boolean){
       if (hasNext){
            this.page.find("#go-next-btn").removeClass("ui-state-disabled")
       } else {
            this.page.find("#go-next-btn").addClass("ui-state-disabled")
       }

       if (hasPrev){
            this.page.find("#go-prev-btn").removeClass("ui-state-disabled")
       } else {
            this.page.find("#go-prev-btn").addClass("ui-state-disabled")
       }
    }

    onLoad(currentMediaId:string){
        this.updateDashboardRequest = false
        let curIndex = this.mediaIterator.findById(currentMediaId)[0] - 1
        this.mediaIterator.seek(curIndex);
        this._updateNvaigationUI(false, false)
        this._loadNextAndSetImage((next,prev)=>{this._updateNvaigationUI(next,prev)})

        this.page.find("#go-prev-btn").click(()=>{
            this._updateNvaigationUI(false, false)
            this.mediaIterator.seek(this.mediaIterator.position() - 2);
            this._loadNextAndSetImage((next,prev)=>{this._updateNvaigationUI(next,prev)})
        })  

        this.page.find("#go-next-btn").click(()=>{
            this._updateNvaigationUI(false, false)
            this._loadNextAndSetImage((next,prev)=>{this._updateNvaigationUI(next,prev)})
        })   

        this.page.find("#fullscreen-btn").click(()=>{
            $("#media-actions-popup").popup( "close" )
            this._fullScreen()
        })  

        this.page.find("#raw-btn").click(()=>{
            $("#media-actions-popup").popup( "close" )
            var url = 'api/media/'+this.curMedia.id()
            var win = window.open(url, '_blank');   
        })

        this.page.find("#download-btn").click(()=>{
            $("#media-actions-popup").popup( "close" )
            var url = 'api/media/'+this.curMedia.id()+"?disposition=attachment"
            var win = window.open(url, 'download_window');   
        })

        this.page.find("#exit-btn").click(()=>{
            if (this.updateDashboardRequest){
                $("#panel_image").empty()
                MEDIA_ITERATOR.seek(-1)
                loadMoreMediaItems()
            }
        })


        this.page.find("#delete-media-btn").click(()=>{
             this._updateNvaigationUI(false, false)
             let idToRemove = this.curMedia.id()
             $.ajax({
                url: 'api/media/'+idToRemove,
                type: 'DELETE',
                success: (result) => {
                    this.updateDashboardRequest = true
                    if (this.mediaIterator.canNext()){
                        this._loadNextAndSetImage((next,prev)=>{this._updateNvaigationUI(next,prev)})
                        this.mediaIterator.delete(idToRemove)
                    } else {
                        this.mediaIterator.delete(idToRemove)
                        $("#exit-btn").click();                    
                    } 
                },
                error: function(result) {
                    alert("Medi resource removing failed.")
                    console.log(result)
                },
            });
            
       })

    }

    _fullScreen() {
        let doc = (document as any)
        if (!document.fullscreenElement &&    // alternative standard method
            !doc.mozFullScreenElement && !document.webkitFullscreenElement && !doc.msFullscreenElement ) {  // current working methods
            if (document.documentElement.requestFullscreen) {
            document.documentElement.requestFullscreen();
            } else if (doc.documentElement.msRequestFullscreen) {
            doc.documentElement.msRequestFullscreen();
            } else if (doc.documentElement.mozRequestFullScreen) {
            doc.documentElement.mozRequestFullScreen();
            } else if (document.documentElement.webkitRequestFullscreen) {
            doc.documentElement.webkitRequestFullscreen((Element as any).ALLOW_KEYBOARD_INPUT);
            }
        } else {
            if (document.exitFullscreen) {
            document.exitFullscreen();
            } else if (doc.msExitFullscreen) {
            doc.msExitFullscreen();
            } else if (doc.mozCancelFullScreen) {
            doc.mozCancelFullScreen();
            } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
            }
        }
    }
     

}