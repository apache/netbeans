function f1(cont) {
    var f1 = function () {
        console.log("running f1 inner");
    } 
    if (cont) {
        f1(false); // inner f1 is called
    } 
     
    console.log("running f1 outer");
}
     
f1(true);