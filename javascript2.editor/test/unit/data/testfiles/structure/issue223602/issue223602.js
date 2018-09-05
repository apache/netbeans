Test223602 = {
    event: "bla",
    id: 10,
    complex : {
        name : "haha",
        number: 20,       
    }
};

function getTest223602() {
    return Test223602;   
}

var p223602_1 = getTest223602();
p223602_1.event;
var p223602_2 = getTest223602().event;
p223602_2.big();
var p223602_3 = getTest223602().complex.name;
p223602_3.big(); 
var EventEmitter = require("events").EventEmitter;
