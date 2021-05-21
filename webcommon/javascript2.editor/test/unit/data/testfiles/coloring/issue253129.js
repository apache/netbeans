var f1 = f2 = function () {
    console.log("run f1");
}     
   
f1();
f2();  

var f3, f4, f5 = f3 = f4 = function (){
    console.log("run f3");
};

f3();
f4();
f5();