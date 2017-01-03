/**
 * ImageLoadingTask
 */
class ImageLoadingTask {

    img:JQuery
    hiResUrl:string
    lowResUrl:string
    lowResDone = false;

    constructor(imgElem:JQuery, hiResUrl:string, lowResUrl:string) {
        this.img = imgElem
        this.hiResUrl = hiResUrl
        this.lowResUrl = lowResUrl    
    }

}

/**
 * ImageLoader
 */
class ImageLoader {
    
    private imageLoadingTasks = new Array<ImageLoadingTask>()
    private tasksLoadFinishAwaiting = new Array<ImageLoadingTask>()
    private _scrollLazyExecuteTimer = null

    constructor() {
        $(document).scroll(() => {
            clearTimeout(this._scrollLazyExecuteTimer)
            //execute if no more scrolling
            this._scrollLazyExecuteTimer = setTimeout(()=>{
               this._execute();     
            }, 200)
        });
    }

    pushTask(task:ImageLoadingTask){
        this.imageLoadingTasks.push(task)
        this._execute()
    }

    private _isInViewport(element:HTMLElement):boolean {
        var rect = element.getBoundingClientRect();
        var html = document.documentElement;
        
        //perhaps invisible, mean its defenetlly not visible to user with 0 size
        //firefox has problems with width for empty images F*CK
        if (rect.height == 0 /*|| rect.width == 0*/) return false;

        return (
            rect.bottom >= 0 &&
            //F*ck*ng firefox
            /* rect.right >= 0 && */
            rect.top <= (window.innerHeight || html.clientHeight) 
            /*&& rect.left <= (window.innerWidth || html.clientWidth)*/
        );
    }

    private _calculateTaskWeight(task:ImageLoadingTask):Number{
        let answer = task.lowResDone ? 1 : 2;
        answer = this._isInViewport(task.img.get()[0]) ? answer * 2 : answer * 0;
        return answer;
    }

    private _executeTimerId = null
    private _execute(){
        if (this._executeTimerId == null){
            this._executeTimerId = setTimeout(()=>{
                this._executeTimerId = null
                this._executeImpl()
            }, 0)
        }
    }

    private  _findNextTask() : ImageLoadingTask{
        let lowPriorTask = null
        for (let i=0;i<this.imageLoadingTasks.length;i++) {
            
            let task = this.imageLoadingTasks[i]
            let taskWeight = this._calculateTaskWeight(task)
            
            if (taskWeight == 2 && lowPriorTask == null){
                lowPriorTask = task
            } else if (taskWeight == 4){
                return task;
            } 
        }
        return lowPriorTask
    }

    private _executeImpl(){
        /*$.mobile.loading( "show", {
                    text: "Loading more... Please wait",
                    textVisible: true,
                    theme: "b"
        });
           $.mobile.loading( "hide" );
        */
        
        if (this.tasksLoadFinishAwaiting.length < 5 && this.imageLoadingTasks.length > 0){
            let taskToPerform = this._findNextTask()
            if (taskToPerform){
                let inex = this.imageLoadingTasks.indexOf(taskToPerform)
                this.imageLoadingTasks.splice(inex,1)
                this._performTask(taskToPerform)
            }
        }
    }


    private _performTask(task:ImageLoadingTask){
        this.tasksLoadFinishAwaiting.push(task)
        if (task.lowResDone){
            task.img.one("load", () => {
                let inex = this.tasksLoadFinishAwaiting.indexOf(task)
                this.tasksLoadFinishAwaiting.splice(inex,1)
                this._execute()
            })    
            task.img.attr("src", task.hiResUrl)
       } else {
            task.img.one("load", () => {
                let inex = this.tasksLoadFinishAwaiting.indexOf(task)                              
                this.tasksLoadFinishAwaiting.splice(inex,1)                       
                task.lowResDone = true
                this.pushTask(task)
                this._execute()
            })
            task.img.attr("src", task.lowResUrl)
        }
        //allow to schedule multiple at once
        this._execute()
    }
    

}