var pool = []; // purple
var pool2 = [];
function makeRequests() {
    pool = [];
    var pool2 = [];
    for (var i = 0; i < numberOfRequests; i++) {
        pool[i] = i; 
        pool2[i] = i;
    }
} 