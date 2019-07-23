
var MyParamTestContext = {
    charAt: function(index) {
        // This is just a stub for a builtin native JavaScript object.
    }
};
/**
 * @param {String} param1 Description of param1
 * @param {Number} param2 Description of param2
 */

MyParamTestContext.testParamDoc = function (param1, param2) {
    param1.length;
    return {x : 22};
}

/**
 * @param {String} Description of param1
 * @param {Number} Description of param2
 */
MyParamTestContext.testParamDoc2 = function (param1, param2) {
    return {x : 22};
}

MyParamTestContext.testParamDoc();
