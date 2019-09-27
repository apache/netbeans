var MyObject234381 = new function() {
    this.times = 3;
    this.test = function() {
        return 1;
    };
};   

with (MyObject234381) {
    var z = test() * times;
}   