function Tester() {
    this.perform = function(action, step) {
        step = 2;
        console.log("start");
        if (action === "build") {
            step = 3; // step into
            doBuild(step); // step into here
        }
    };
}

function doBuild(step) {
    var a = 1;
    step += 1;
    var d = new Date(); // lb here
    step = minorTask(step); // step over + check step var
    finishBuild(step);
}

function minorTask(step) {
    return step += 1;
}

function finishBuild(step) {
    step += 1;
}

function test2(url) {
    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.onreadystatechange = function() {
        
    };
    request.send();
}
