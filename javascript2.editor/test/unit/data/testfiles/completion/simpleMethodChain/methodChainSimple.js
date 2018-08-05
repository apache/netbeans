var MyObj = {
    version: 10,
    factory: function () {
        return this;
    },
    
    create: function () {
        return new MyObj();
    },
    
    getInfo: function() {
       return "text";  
    },
      
    /**
    * @return {Number}
    */
    getVersion: function() {
        return version;
    }
}
    
/**
 * @return {Number} 
 */
function martion () {
    return MyObj.getVersion();
}

MyObj.create().getInfo().big();
