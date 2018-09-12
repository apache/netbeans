function getdate() {
    return new Date();
}
var ar = ["ahoj", "cau", 10, getdate()];
//var a = "t";
//a.toUpperCase()   
console.log(ar[0].toUpperCase());
   
var prom1 = ar[1].toUpperCase();
var prom2 = ar[3].getDay();
var prom3 = ar[2].toPrecision();