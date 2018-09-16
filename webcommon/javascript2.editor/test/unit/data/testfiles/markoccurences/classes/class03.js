"use strict";

var Polygon = class Polygon2 {
  constructor(height, width) {
    this.height = height;
    this.width = width;
    
  }
  
  static test () {
        console.log("test");
        var p = new Polygon2(1,2);
        console.log(p.width);
  }
  
  
};  
   
var p = new Polygon (10, 20);
console.log(p.width);
console.log(p.height);
console.log(Polygon.test());