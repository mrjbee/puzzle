/// <reference path="../../../typings/index.d.ts" />
/// <reference path="common_size.ts" />
/// <reference path="ui_builders.ts" />
/// <reference path="tag_manager.ts" />
/// <reference path="page_handlers.ts" />

var MULTI_SELECTION_HANDLERS = [new TagManagerPageHandler(), new RemoveMediaPageHandler()]
var THUMBNAILS_MATH = ThumbnailMath.DEFAULT;
var TAG_MANAGER = new TagManager();
var FILTER_MANAGER = new FilterManager();
var MEDIA_ITERATOR = new MediaIterator();

function initialize_browser_module(){
   
   THUMBNAILS_MATH.updatePageWidth($(window).width())
   TAG_MANAGER.onTagsChangedCallback = ()=>{
       $(".filter_tag_group").remove()
       var filterTagsList = $("#filter-tag-list")
       TAG_MANAGER.each((tag:Tag)=>{
           let li = $("<li>")
           li.attr("id","filter_tag_"+tag.name())
                    .addClass("filter_tag_group")
                    .addClass("ui-field-contain")
                    .append(
                        $("<label>")
                            .attr("for","filter_tag_input_"+tag.name())
                            .append(
                                $("<span>")
                                    .addClass("ui-corner-all")
                                    .addClass("filter_tag_caption")
                                    .addClass("tag-type-"+tag.type())
                                    .text(tag.name())
                            )
                     ).append(
                        $("<input>")
                            .attr("name","filter_tag_input_"+tag.name())    
                            .attr("id","filter_tag_input_"+tag.name())
                            .attr("type","checkbox")
                            .attr("data-mini","true")
                            .attr("data-iconpos","right")
                            .attr(FILTER_MANAGER.findTagFilterByTag(tag)? "checked" : "notchecked", "some")
                            .change(function() {
                                if($(this).is(":checked")) {
                                   FILTER_MANAGER.addTagFilter(tag)
                                } else{
                                   FILTER_MANAGER.removeTagFilter(tag) 
                                }
                            })                                
                    )

           filterTagsList.append(li)
           li.trigger('create');
       })
       filterTagsList.listview("refresh");
       $("#filter-tag-count").text(TAG_MANAGER.tagCount())       
   }

    $( "#left-panel" ).on( "panelopen", (event, ui ) => {
        ui_updateByIdOrIds(selectedMediaIds, ui_thumbnail_deSelectById)
        selectedMediaIds = []
        onSelectedMediaChange()
    } );

   $(window).resize(function() {
        THUMBNAILS_MATH.updatePageWidth($(window).width());
        var cellSize = THUMBNAILS_MATH.cellWidth
        var thumbnailsContentPanelWidth = THUMBNAILS_MATH.calculateThumbnailsPanelWidth()
        $('.panel-thumbnails').each(function() {
                $(this).width(thumbnailsContentPanelWidth);
        });
   });

   $("#drop-selection-btn").click(function (event){
         vibrate(50)
         ui_updateByIdOrIds(selectedMediaIds, ui_thumbnail_deSelectById)
         selectedMediaIds = []
         onSelectedMediaChange()
   })
   $( ":mobile-pagecontainer" ).on( "pagecontainerload", function( event, ui ) {
        var pageId = ui.toPage.first().attr('id') as string

        if ("photo-view-page" == pageId){
            let iterator = MEDIA_ITERATOR.clone() 
            new MediaPreviewPage(ui.toPage as JQuery, iterator).onLoad(waitForPreviewMediaId)  
        } else{
            //asume that on of the MULTI_SELECTION_HANDLERS would be found
            var pageHandlerIndex = MULTI_SELECTION_HANDLERS.map(it=>{
                return it.getPageId()
            }).indexOf(pageId)
            if (pageHandlerIndex < 0){
                alert("There is no handler for page = "+pageId)    
            } else{
            MULTI_SELECTION_HANDLERS[pageHandlerIndex].onLoad(
                ui.toPage as JQuery,
                selectedMediaIds)     
            }  
        }
    } );
    $('#open-selection-btn').click(function(){
        for (var i = 0; i < selectedMediaIds.length; i++) {
            openMediaInTab(selectedMediaIds[i])
        }
        parent.history.back();
        return false;
    });


    (<any> $.get("api/tags"))
        .success(function(data) {
            for (var i = 0; i< data.length; i++){
                TAG_MANAGER.updateTag(new Tag(
                    (data[i].name) as string,
                    (data[i].type) as string)
                )
            }
            TAG_MANAGER.notifyOnTagsChanged()
        })
        .error(function() { alert("Error during loading tags"); });
    
    $("#filter-apply-btn").click((event)=>{
        $("#panel_image").empty()
        let filters = encodeURIComponent(FILTER_MANAGER.asTagsFilterQuery());
        MEDIA_ITERATOR.applyFilters(filters)
        loadMoreMediaItems()
    })    
    loadMoreMediaItems()
}

function testLoadMore(){
    if(Math.abs($(window).scrollTop() + $(window).height() - $(document).height()) < 3) {
        //Prevent loading when other dialog is opened
        if ($("#panel_image").is(":visible")){
            //if (loadAllowedMore){
                loadMoreMediaItems()
                // if (MEDIA_ITERATOR.canNext() || MEDIA_ITERATOR.hasMore()) {
            //}
        }
    }

    /*
    setTimeout(() => { 
        testLoadMore() 
    }, 300);
    */
}

function loadMoreMediaItems(){
    MEDIA_ITERATOR.next(30,(position:number, media:Media, isLast:boolean) => {
        //TODO: fix isLast
        if (media != null){
            onNewMediaItem(media.model())
        }     
        if (isLast){
            setTimeout(() => { 
                testLoadMore() 
            }, 300);
        }
    });
}

function onNewMediaItem(mediaResource){

    var creationDate = new Date(mediaResource.creationDate);

    var sortingDate  = new Date(
        creationDate.getFullYear(),
        creationDate.getMonth(),0,0,0,0,0)

    var media = {
        orig:mediaResource,
        sortByDate:sortingDate
    }

    onMedia(media)
}


var content
function onMedia(media){
    var thumbnailsPanelWidth = THUMBNAILS_MATH.calculateThumbnailsPanelWidth()
    var panel_image = $("#panel_image")
    var groupId = "panel_thumbnails_" + media.sortByDate.getTime();
    var panel_thumbnails;
    if ( $( "#"+groupId ).length ) {
        panel_thumbnails =  $( "#"+groupId )
    } else {
        panel_thumbnails = $('<div>').attr("id",groupId).addClass("panel-thumbnails-container")
            .append(
                $('<h4>')
                    .addClass("ui-bar")
                    .addClass("ui-bar-a")
                    .text(moment(media.sortByDate).format("MMMM YYYY"))
            )
            .append(
                 $('<div>')
                    .addClass("ui-body")
                    .addClass('panel-thumbnails')
                    .width(thumbnailsPanelWidth)
            )

        panel_image.append(panel_thumbnails)
    }

    content = panel_thumbnails.children("div");
    
    UiCommons.build(new ThumnailsPanelBuilder("thumbnail")
        .withMedia(
            UiCommons.describeMedia()
                .withId(media.orig.id as string)
                .withType(media.orig.type as string)
                .usingLazyLoading())
        .withTapFunction((id) => {
            onThumbnailPress(id)
        })
        .withTapholdFunction((id) => {
            onThumbnailLongPress(id)
        })
        .withParent(content))
}

function vibrate(ms){
   if ('vibrate' in navigator) {
      navigator.vibrate(ms);
   }
}

function openMediaInTab(mediaId){
        var url = 'api/media/'+mediaId
        var win = window.open(url, '_blank');
        /*if (win) {
            //Browser has allowed it to be opened
            win.focus();
        } else {
            //Browser has blocked it
            alert('Please allow popups for this website');
        }*/
}

var selectedMediaIds = []
var waitForPreviewMediaId

function onThumbnailPress(mediaId){
    if (selectedMediaIds.length == 0){
        waitForPreviewMediaId = mediaId;
        ($(":mobile-pagecontainer") as any)
            .pagecontainer("change","pages/photoView.html", {
                 transition:"slide",
                 showLoadMsg:true,
                 changeHash:true
            } );
    } else {
        vibrate(50)
        var mediaIdIndex = $.inArray(mediaId, selectedMediaIds)
        if (mediaIdIndex == -1){
            selectedMediaIds.push(mediaId)
            ui_thumbnail_selectById(mediaId)
        } else {
            selectedMediaIds.splice(mediaIdIndex,1)
            ui_thumbnail_deSelectById(mediaId)
        }
         onSelectedMediaChange()
    }
}

function onThumbnailLongPress(mediaId){
    vibrate(100)
    if (selectedMediaIds.length == 0){
        selectedMediaIds.push(mediaId)
        ui_thumbnail_selectById(mediaId)
    } else {
        ui_updateByIdOrIds(selectedMediaIds, ui_thumbnail_deSelectById)
        selectedMediaIds = []
        onSelectedMediaChange()
    }
    onSelectedMediaChange()
}

function ui_updateByIdOrIds(idOrIds, updateFunction){
    if ($.isArray(idOrIds)){
        $.each(idOrIds, function( index, value ) {
            updateFunction(value);
          })
    } else {
        updateFunction(idOrIds);
    }
}

function onSelectedMediaChange(){
    if (selectedMediaIds.length != 0){
        showHeader()
        $('#selection-actions-btn-group').show()
    } else {
        hideHeader()
        $('#selection-actions-btn-group').hide()
    }
    $('#selected-counter-text').text(selectedMediaIds.length)
}

function ui_thumbnail_deSelectById(id){
    $("#thumbnail_"+id+" div").removeClass("selection")
}

function ui_thumbnail_selectById(id){
    $("#thumbnail_"+id+" div").addClass("selection")
}

var lastScrollPosition;

$(document).scroll( function() {
  
  
  var headerToRefElement:Element = null
  //TODO: current assumption that querry select in order appeared on a page
  $(".panel-thumbnails-container > h4:first-child").each((index, element) => {
        var rect = element.getBoundingClientRect();
        var html = document.documentElement;
        if (rect.top < 20){
            headerToRefElement = element;
        }
  })

  var captionChanged:boolean = false

  if (headerToRefElement){
      captionChanged = $("#top-most-header").text() != headerToRefElement.innerHTML
      $("#top-most-header").text(headerToRefElement.innerHTML)
  } else {
      let text = 'My <span class="selected-text">Private</span>'
      captionChanged = $("#top-most-header").html() != text
      $("#top-most-header").html(text)
  }

  
  
  var scrollPosition = $(this).scrollTop();

  // Scrolling down
  if (scrollPosition > lastScrollPosition){
    if (captionChanged){
        showHeader()
    } else{
        hideHeader(false)
    }
    testLoadMore()
  }

  // Scrolling up
  else {
    showHeader()
  }

  lastScrollPosition = scrollPosition;

  if (scrollPosition > 10){
      $(".ui-header").addClass("ui-header-shadow")
  } else {
      $(".ui-header").removeClass("ui-header-shadow")
  }

});

var scheduledHideTimeout = null
function hideHeader(imediatlly=true){
    clearTimeout(scheduledHideTimeout)
    if (imediatlly){
        if ($("#left-panel.ui-panel-open").length == 1) {return}
        // If the header is currently showing
        if (!$('#pageDashboard [data-role=header].ui-fixed-hidden').length) {
        $('#pageDashboard [data-role=header]').toolbar('hide');
        }
    } else {
        scheduledHideTimeout = setTimeout(()=>{
            hideHeader(true)
        }, 500)
    }
}

function showHeader(){
   clearTimeout(scheduledHideTimeout)
   if ($("#left-panel.ui-panel-open").length == 1) {return}

// If the header is currently hidden
    if ($('#pageDashboard [data-role=header].ui-fixed-hidden').length) {
      $('#pageDashboard [data-role=header]').toolbar('show');
    }
}

