/**
 * @constructor
 */
var MyLib = new function MyLib() {
    
    function fn1() {
        fn2().toLowerCase();  // test
        fn2().toLowerCase();
        return "";
    } 

    /**
     * @returns {string}
     */
    function fn2() {
        return "";
    }
};      
