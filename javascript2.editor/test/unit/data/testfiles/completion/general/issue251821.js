/** Implementation of the MyLib Library */
var MyLib = new function MyLib() {};

/** @constructor
 *  @returns {MyLib.ObjA} description */
MyLib.ObjA = function (arg) {
    /** JsDoc for MyLib.ObjA.aproperty
     * @type {number|string} */
    this.aproperty = undefined;
};
   
/**
 * Constructs MyLib.ObjA
 * @returns {MyLib.ObjA}
 */
MyLib.objA = function () {
};

/** @type MyLib.ObjA */
var a1 = MyLib.objA();
var res = a1.aproperty.toLocaleString();        