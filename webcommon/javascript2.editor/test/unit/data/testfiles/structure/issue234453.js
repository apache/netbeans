/**
 * 
 * @returns {Runner}
 */
function test() {
    var b = new Runner();
    return b;
}

var a = test();


function Runner(object, name, dispose) {
    this.name = name;
    this.notify = function() {
    };
}
