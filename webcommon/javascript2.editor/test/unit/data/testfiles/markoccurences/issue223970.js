function Wrap() {
  this.pers = new Person();
}


function Person() {
  this.run = function() {
  };
}
  
var wrapper = new Wrap();
wrapper.pers.run(); // place cursor here to inside run

var per = new Person();
per.run();
           