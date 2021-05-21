function f1(cont) {
    if (cont) {
        f1(false); // inner f1 is called
    }
    function f1() { // inner f1
        console.log("running f1 inner");
    }
    console.log("running f1 outer");
}
   
f1(true);