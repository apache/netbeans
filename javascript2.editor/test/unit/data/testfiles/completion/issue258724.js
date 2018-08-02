function Person(name, age) {
	this.name = name;
	this.age = age;
}

Person.prototype.getName = function () { //Anonymous function expression
	return this.n; // .name gets suggested
}

Person.prototype.getAge = function getAge() { //Named function expression
	return this.a; // .age does NOT get suggested (actually there is "no suggestions")
}