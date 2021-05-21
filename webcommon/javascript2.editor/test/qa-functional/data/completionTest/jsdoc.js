/**
 * 
 * @returns {Car}
 */
function foo(){}

//cc;8;foo().;drive,manufacturer;


/**
 * 
 * @param {type} value
 * @param {type} pretty
 * @returns {Number|Date}
 */
function print(value, pretty) {
    if (pretty)
        return "<h1>" + value + "</h1>";
    else
        return 0;
}

//cc;24;print().;getDay,MAX_VALUE;


/**
 * 
 * @returns {Runner}
 */
function test(){
    var a = new Runner();
    return a;
}

var a = test();
//cc;37;a.;name,notify;

/**
 * 
 * @param {type} object
 * @param {Date} name
 * @param {Car} dispose
 * @returns {Runner}
 */
function Runner(object, name, dispose){
    this.name = name;
    
//cc;49;dispose.;drive,manufacturer;
    
    this.notify = function(){};
}