function f1(cont) {
    var f1 = function f1(cont) {
        if (cont) {
            f1(false);  // the f1 private name is called
        }
        console.log("running f1 inner");
    } 
    if (cont) {
        f1(true); // inner f1 is called
    } 
     
    console.log("running f1 outer");
}
     
f1(true); // outer f1 is called