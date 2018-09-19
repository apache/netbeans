let model = "modelVal";
let i =0;
let car = {
model : "modelVal1" ,
["model" + ++i] : "modelVal2" ,
["model" + ++i] : "modelVal3",
[model] : "modelVal3"
};

