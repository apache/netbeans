var name = 2;
age = 10;
function Ble () {
    this.name = name;
    this.age = age;
    this.print = function () {
        console.log(this.name);
    };
}
 
var b = new Ble();
name = 20;
b.print();  
