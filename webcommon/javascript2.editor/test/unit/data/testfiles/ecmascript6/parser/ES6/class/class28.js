"use strict";

class Animal { 
  constructor(name) {
    this.name = name;
  }
  
  speak() {
    console.log(this.name + ' makes a noise.');
  }
}

class Dog extends Animal {
  speak() {
    console.log(this.name + ' barks.');
  }
}
 
var animal = new Animal("Jitka");
var dog = new Dog("Pepa");
animal.speak();
dog.speak();