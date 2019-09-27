function test224462() {
    
    var prom224462 = 10;
    
    try {
        test224462_1();
    } catch (err224462) {
        var prom224462_1 = "ahoj";
        formatter.println(err224462);
        formatter.println(prom224462);
        function printAhoj() {
            formatter.println();
        }
    }
        
    formatter.print(err224462);
    
    try {
        formatter.println(prom224462_1);
    } catch (err224462) {
        prom224462 = 20;
        formatter.say(err224462);
        formatter.println(prom224462);
    }
    
    
}

test224462();