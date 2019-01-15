"use strict";
var foo = function () { return 0; }


console.log(foo());         // 0
{
    let plet = 1;
    function foo() { return 1; }
    console.log(foo());     // 1
    {
        function foo() {
            return 2
        }
        console.log(foo()); // 2
    }
    console.log(foo());     // 1
}
console.log(foo());         // 0