MyParamTestContext.charAt(22);
MyParamTestContext.testParamDoc2("text", 13);

/**
 * 
 * @param {Number}  param1 Description of the param1 in method definedinOtehrFile    
 * @return {Number} how many it does
 */
MyParamTestContext.definedInOtherFile = function (param1) {
    return "haha";
}

MyParamTestContext.definedInOtherFile(22);

formatter.print("text");
