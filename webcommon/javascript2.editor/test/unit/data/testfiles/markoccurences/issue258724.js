function Person(name, age) {
	this.name = name;
	this.age = age;
}

Person.prototype.getName = function () { //Anonymous function expression
	return this.name; // .name gets suggested
}

Person.prototype.getAge = function getAge() { //Named function expression
	return this.age; // .age does NOT get suggested (actually there is "no suggestions")
}