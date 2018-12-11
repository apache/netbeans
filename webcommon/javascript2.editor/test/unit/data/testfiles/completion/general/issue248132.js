!(function () {
    var issue248132 = Date.UTC();
    
    var w1 = new window.Worker();
    var w2 = window.Worker;
}());