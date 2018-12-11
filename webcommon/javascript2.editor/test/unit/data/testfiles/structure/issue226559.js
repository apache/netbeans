function Test226559() {
    this.method1 = function() {
        this.method2 = function() {

        };
    };
}

var test226559 = new Test226559();
test226559.method1();