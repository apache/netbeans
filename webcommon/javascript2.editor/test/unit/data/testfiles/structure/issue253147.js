var test = {};

/**
 * @private
 * 
 */
test.prototype.getTest = function() {
    return {property1 : {aaa: 0, abb: "ahoj"},
            method2 : function () {}};
};

test.prototype.run = function() {
    var foo = this.getTest();
};  