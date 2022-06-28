 




var Person = Class.create({
    /**
     * @constructor
     * @param {String} name
     */
    initialize: function(name){
       this.name = name; 
    },
    
    /**
     * @param {String} message
     */
    say: function(message) {
        console.log(message + ' ' + this.name);
    }
});   

var person = new Person();
person.
        