function test (condition) {
    "use strict"  
    function getNumber() {return 0;}
    if (condition) {
        function getNumber() {  // function 2
            return 2;
        }
        console.log(getNumber()); // 2
    } else {
        function getNumber() { // function 3
            return 3;
        }
        console.log(getNumber()); // 3
    } 
    return getNumber();
}     

console.log(test(true));
console.log(test(false));