function resolveDay(day, index, days, action) {
    for (var i = 0, max = day; i < max; i++) {
        (function(index) {
            console.log("--------------");
            console.log("i: " + i);
            console.log("day: " + day);     
            console.log("index: " + index);
            console.log("action: " + action);
        }()); 
    }
}

resolveDay(3, 2, 7, "test");