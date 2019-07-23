var MyCtx = MyCtx || {};
/**
 * @constructor
 * @returns {undefined}
 */
MyCtx.Auto = function() {
    this.description = {
        name: "Skoda"
    };


}

var aaa = new MyCtx.Auto();
console.log(aaa.description.name);
