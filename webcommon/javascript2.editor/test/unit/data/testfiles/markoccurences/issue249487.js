function TestCls() {
    this.myEvent = new MyApi.Event(myEventArgs);
    this.myEventArgs = myEventArgs; // <- here
    
    function myEventArgs(iArg) {
        this.iArg = iArg;
        console.log("run " + this.iArg);
    }
    this.iArg = 10;
    myEventArgs(this.iArg);
    console.log(this.iArg);
}

var test = new TestCls();

console.log(test.iArg);