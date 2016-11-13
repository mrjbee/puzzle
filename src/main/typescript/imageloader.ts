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

    pushTask(task:ImageLoadingTask){
        this.imageLoadingTasks.push(task)
        this._execute()
    }

    private _execute(){
        if (this.tasksUnderAwaiting.length < 5){
            let taskToPerform = this.imageLoadingTasks.shift()
            if (taskToPerform){
                this._performTask(taskToPerform)
            }
        }
    }

    private _performTask(task:ImageLoadingTask){
        this.tasksUnderAwaiting.push(task)
        if (task.lowResDone){
            task.img.one("load", () => {
                let inex = this.tasksUnderAwaiting.indexOf(task)
                this.tasksUnderAwaiting.splice(inex,1)                       
                this._execute()
            })    
            task.img.attr("src", task.hiResUrl)
       } else {
            task.img.one("load", () => {
                let inex = this.tasksUnderAwaiting.indexOf(task)                              
                this.tasksUnderAwaiting.splice(inex,1)                       
                task.lowResDone = true
                this.pushTask(task)
            })
            task.img.attr("src", task.lowResUrl)
        }
    }
    

}