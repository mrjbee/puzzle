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
    private tasksUnderAwaiting = new Array<ImageLoadingTask>()
    private tasksAwaitingViewport = new Array<ImageLoadingTask>()

    constructor() {
        $(document).scroll(() => {
            
            let tasksToTransfer = this.tasksAwaitingViewport.filter((task)=>{
                return this._isInViewport(task.img.get()[0])     
            })
            
            tasksToTransfer.forEach((task) => {
                let inex = this.tasksAwaitingViewport.indexOf(task)
                this.tasksAwaitingViewport.splice(inex,1)
                this.imageLoadingTasks.push(task)
            })

            if (tasksToTransfer.length > 0){
                this._execute()
            }
        });
    }

    pushTask(task:ImageLoadingTask){
        //remove bad quality preloading
        //task.lowResDone = true

        this.imageLoadingTasks.push(task)
        this._execute()
    }

    private _isInViewport(element:HTMLElement):boolean {
        var rect = element.getBoundingClientRect();
        var html = document.documentElement;
        return (
            rect.bottom >= 0 &&
            rect.right >= 0 &&
            rect.top <= (window.innerHeight || html.clientHeight) &&
            rect.left <= (window.innerWidth || html.clientWidth)
 
        );
    }

    private _calculateTaskWeight(task:ImageLoadingTask):Number{
        let answer = task.lowResDone ? 0 : 1;
        answer = this._isInViewport(task.img.get()[0]) ? answer * 2 + 1 : answer;
        return answer;
    }

    private _execute(){
        setTimeout(()=>{this._executeImpl()},0)
    }

    private _executeImpl(){
        $.mobile.loading( "show", {
                    text: "Loading more... Please wait",
                    textVisible: true,
                    theme: "b"
        });
        
        if (this.tasksUnderAwaiting.length < 5){
            this.imageLoadingTasks = this.imageLoadingTasks.sort((task, otherTask) => {
                let taskWeight = this._calculateTaskWeight(task)
                let otherTaskWeight = this._calculateTaskWeight(otherTask)
                if (taskWeight > otherTaskWeight){
                    return -1
                } else if (otherTaskWeight > taskWeight){
                    return 1
                } else {
                    return 0
                }
            })
            let taskToPerform = this.imageLoadingTasks.shift()
            if (taskToPerform){
                if (this._isInViewport(taskToPerform.img.get()[0])){
                    this._performTask(taskToPerform)
                } else{
                    this.tasksAwaitingViewport.push(taskToPerform)
                    if (this.imageLoadingTasks.length > 0){
                        this._execute()
                    } else {
                        $.mobile.loading( "hide" );
                    }    
                }
            } else {
                $.mobile.loading( "hide" );
            }
        }
    }

    private _performTask(task:ImageLoadingTask){
        this.tasksUnderAwaiting.push(task)
        if (task.lowResDone){
            task.img.one("load", () => {
                let inex = this.tasksUnderAwaiting.indexOf(task)
                this.tasksUnderAwaiting.splice(inex,1)
                if (this.imageLoadingTasks.length > 0){
                    this._execute()
                } else {
                    $.mobile.loading( "hide" );
                }                       
            })    
            task.img.attr("src", task.hiResUrl)
       } else {
            task.img.one("load", () => {
                let inex = this.tasksUnderAwaiting.indexOf(task)                              
                this.tasksUnderAwaiting.splice(inex,1)                       
                task.lowResDone = true
                this.pushTask(task)
                this._execute()
            })
            task.img.attr("src", task.lowResUrl)
        }
        this._execute()
    }
    

}