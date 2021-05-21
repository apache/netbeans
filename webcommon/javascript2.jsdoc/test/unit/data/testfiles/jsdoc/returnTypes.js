/**
 * @returns {Shape} A new shape
 */
Shape.prototype.clone = function(){
   return new Shape();
}

/* Here should be returned null as well since no type or return tag is available. */
Shape.prototype.clone4 = function(){
   return new Shape();
}

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
    return MyObj.getVersion;
}

/**
 * @returns {Number}
 */
Math.E;
