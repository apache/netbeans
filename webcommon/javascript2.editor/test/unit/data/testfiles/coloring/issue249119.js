function myFunction(param) {
    var f = param || function () {}; // usage is correctly detected
    var  a = param.a || 1;  // usage is correctly detected
    var g = function () {
        f();
        console.log(a);
    };
    g();
}              
myFunction({a: 4});
 
function myFunction2(param) {
    var f2 = param.f || function () {}; // usage is correctly detected
    var  a2 = param.a || 1;  // usage is correctly detected
    var g2 = function () {
        f2();
        console.log(a2);
    };
    g2();
}