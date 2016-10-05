
/**
 * MultiSelectionAction
 */
class MultiSelectionAction {

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

/**
 * PageHandler
 */
interface ActionPageHandler {
    onLoad(selectedResourceIDs:string[])
    onAccept(callback:(success:boolean) => void)
}

/**
 * DeleteResourcesPageHandler
 */
class DeleteResourcesPageHandler implements ActionPageHandler {

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