function addTest(a, b) {
    
    if( a = b) {
        a = 10;
    }
    
    while (a = b) {
        b++;
    }
    
    for (var i = a; a = b; a++) {
        b = b + 1;
    }
    
    do {
        +b;
    } while (a = 20);
}
