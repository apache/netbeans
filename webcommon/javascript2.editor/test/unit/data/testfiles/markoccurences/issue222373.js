
function Person(){
  this.test = {
    name : "A"
  };
}
        
var a = new Person();
a.test.name = "B";
   
var b = new Person();
b.test.name = "B";

var c = a;
c.test.name = "c";
