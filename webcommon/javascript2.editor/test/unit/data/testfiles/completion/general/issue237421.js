/**
 * @private
 */
test.prototype.getTest = function() {
    return {property1 : {aa: 0, abb: "ahoj"},
            method2 : function () {}};
};

test.prototype.run = function() {
    var foo = this.g ;
};

test.prototype.run2 = function() {
    var foo2 = this.getTest().m;
};

test.prototype.run3 = function() {
    var foo3 = this.getTest().property1.a;
}; 