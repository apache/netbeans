/** Implementation of the MyLib Library */
var MyLib = new function MyLib() {};


MyLib.ObjB = function (arg) {
    this.aproperty = 10;
};

MyLib.objB = function () {
    return new MyLib.ObjB(); 
};        

