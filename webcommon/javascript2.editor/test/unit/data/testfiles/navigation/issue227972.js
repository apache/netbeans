var newtest = function() {
	this.method = function() { // nagigate from a.method()
		alert('dosomething');
	};
	
	this.otherfunction = function() {
		// Ctrl+Click on method() will take you to this.method above.
		this.method(); // case 1
	}
}

var a = new newtest();
// Ctrl click on a.method() navigates to newtest.method
a.method();

//======================================


function test() {
	this.method = function() { // navigate from b.mehtod()
		alert('dosomething');
	}
	
	this.otherfunction = function() {
		// Ctrl+Click on method() will take you to this.method above.
		this.method(); // case 2
	}
}

var b = new test();
// Ctrl+Click on method will take you to test.method
b.method();

//======================================

var NS = {};
NS.Test = function() {

	this.method = function() { // navigate from c.method()
		alert('dosomething');
	}
	
	this.otherfunction = function() {
	// ctrl+click on this will NOT navigate to method
		this.method(); // case 3
	}
};

var c = new NS.Test();
// ctrl + click on this will NOT navigate to NS.test.method;
c.method();
// Ctrl click on this will navigate to NS.test.method though.
NS.Test.method();

function MyClass() {}
MyClass.prototype.f= function() {};

MyClass.prototype.g= function() {
    this.f(); // If I Ctrl+click at f() called on this line, it won't take me to definition of f()
};