/**
 * @type {Array<String>} Array
 */
var arrStr = [];
arrStr.push("test");

/** 
 * @type {Array<Object>} Array
 */
var arrObj = [];
arrObj[0] = new Object();
arrObj[1];

/**
 * @type {Object.<Sring, Number>} personObj description
 */
var personObj = {firstName:"John", age:36};
personObj["firstName"];

/**
 * @type {Array.<{firstName:String, age:Number}>} Array somedesc
 */
var personsArr = [{firstName:"John", age:36},
                  {firstName:"James",age:46}];
personsArr.push({});  

/**
 * 
 * @type {{myNum:number, myString:string}} someObj
 */
var someObj = {myNum:10, myString:"test" };
someObj["myNum"];

/**
 * 
 * @type {Array.<Object.<String,Number>>} Array
 */
var personsNew = [{firstName:"John", age:36},
                  {firstName:"James",age:46}];           

personsNew.push({});


/**
 * Display object details
 * @param {Array.<{firstName: String,age: Number}>} persons somedescription
 * @returns {Array.<String>} test
 */
function printObjs(args) {
   for (var index in args) {
       console.log(args[index].firstName + " : " + args[index].age);
   }   
   return ["t1", "t2"];
}

printObjs(persons);

 
/**
 * 
 * @param {{myNum: number, myString: string}} someObj
 * @returns {String}
 */
function someFunc(args) {
   return "test"+ args.myNum + " , " +args.myString ;
}
someFunc(someObj);

/**
 * 
 * @param {*} args
 * @returns {String}
 */
function anotherFunc(args) {
  return args.toString();  
}
anotherFunc("test");

/**
 * @return {{myNum: number, myObject}}
 * An anonymous type with the given type members.
 */ 
function getTmpObject() {
    return {
        myNum: 2,
        myObject: 0 || undefined || {}
    };
}
getTmpObject();


/**
 * 
 * @param   name {String} Someone's name
 * @returns   messageStr {String} Received message
 */
function tryMessage(somebody) {
    return "Hello" + somebody;
}

tryMessage();

   /**
     * Helper function: Returns both select boxes and data, either as source
     *   or as target parameter
     * @param  targetName {String} Which select box will be the target
     * @return  object {{"targetSelect": [jquerified select object],
     *                 "sourceSelect": [jquerified select object],
     *                 "targetData"  : object,
     *                 "sourceData"  : object}} a custom object
     **/
    function getSelect(targetName) {
      var $target, $source, targetData, sourceData;

      if (targetName === "left") {
        $target = $leftSelect;
        $source = $rightSelect;
        targetData = leftSelectOptionsData;
        sourceData = rightSelectOptionsData;
      } else {
        $target = $rightSelect;
        $source = $leftSelect;
        targetData = rightSelectOptionsData;
        sourceData = leftSelectOptionsData;
      }

      return {
        "targetSelect": $target,
        "sourceSelect": $source,
        "targetData": targetData,
        "sourceData": sourceData
      };
    }

getSelect();

/**
 * 
 * @param name {String test
 * @returns returnObj {{"name" : string}}
 */
function returnObj(test) {
    return {"name" : test};
}

returnObj();

/**
 * 
 * @param {String testString
 * @returns {String}
 */
function testType(testString) {
    return "";
}

testType();
