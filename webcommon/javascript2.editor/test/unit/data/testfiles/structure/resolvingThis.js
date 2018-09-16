this.representsGlobal = "global space";
console.log(representsGlobal);

function SomeConstructor() {
    this.field01 = "only when the SomeConstuctor is called via new";
    
    function  privateFn() {
        this.field02 = "field02 belongs to this private function";
    }

    this.field03 = new privateFn();
    this.method01 = function () {
        this.field04 = "field04 belongs to the object above";
    } 
    privateFn();
}
SomeConstructor.prototype.method02 = function () {
    this.field05 = "field05 belongs to the object that has the prototype";
}  
var newObject = new SomeConstructor();
newObject.method01();
newObject.method02();

//console.log(newObject.field01);
//console.log(newObject.field03.field02);
//console.log(newObject.field04);
//console.log(newObject.field05);

var literal = {
    method03 : function () {
        this.field06 = "field06 belongs to the object above -> literal";
    }
}

literal.method03();
//console.log(literal.field06);