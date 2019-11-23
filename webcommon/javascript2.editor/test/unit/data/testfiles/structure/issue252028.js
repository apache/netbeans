function MySpace() {
    this.name = "hello";
    var PrivateObject = function PrivateO(){
        console.log("running... ");
        this.sound = "prd";
        this.name = "cau";
        console.log(this.sound);
    }; 

    var prom = new PrivateObject();
    console.log(this.name);
    console.log(prom.sound);
    console.log(prom.name);
    return this;
}
  
var star = new MySpace();
console.log(star.name);