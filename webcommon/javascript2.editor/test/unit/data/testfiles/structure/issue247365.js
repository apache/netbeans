function Book(name) {
    this.name = name;
}


Book.prototype.printName = function printName() {
    this.counter = 0;
    return this.name;
};

Book.prototype.print2 = Book.prototype.printName;

 
//Book.prototype = EventEmitter;

var book = new Book("Testing");
//book.

console.log();    
console.log(book.printName());
console.log(book.print2());
