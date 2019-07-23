function test () {
    "use strict"; 
    var test = 10;
    if (true) {
        let test = 20;
        console.log(test);
    }
    console.log(test); // 10
}

test(); 