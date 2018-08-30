/** Implementation of the MyLib Library */
var MyLib = new function MyLib() {};

/** @constructor
 *  @return {MyLib.ObjB} description */
MyLib.ObjB = function (arg) {
    /** JsDoc for MyLib.ObjB.aproperty
     * @type {number|boolean} */
    this.aproperty = 10;
}; 

/** Constructs MyLib.ObjB
 *  @returns {MyLib.ObjB} */
MyLib.objB = function () {
    return new MyLib.ObjB(); 
};   
  

var b1 = MyLib.objB();    // <- place cursor into objB identifier
var res = b1.aproperty;