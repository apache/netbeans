exports.FreeList = function(name, max, constructor) {
  this.name = name;
  this.constructor = constructor;
  this.max = max;
  this.list = [];
};


exports.FreeList.prototype.alloc = function() {
  //debug("alloc " + this.name + " " + this.list.length);
  return this.list.length ? this.list.shift() :
                            this.constructor.apply(this, arguments);
};


exports.FreeList.prototype.free = function(obj) {
  //debug("free " + this.name + " " + this.list.length);
  if (this.list.length < this.max) {
    this.list.push(obj);
  }
};


var Car = require('../complexModule').Car;

var auto = new Car();
//cc;1;auto.;0;ride,speed,stop,defineProperty,hasOwnProperty;stop;auto.stop();pokus2,obj2,neco2,getDate,E


