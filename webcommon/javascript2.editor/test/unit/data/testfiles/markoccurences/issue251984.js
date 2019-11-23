var myLib = new function MyLib() {
   
    this.f1 = f1; 
    
    /**
     * f1 JsDoc, note: "this.f1 = f1;" occurs before function 'f1' in text
     * @param {string} f1arg f1arg JsDoc
     */
    function f1(f1arg) {
        // many lines of text ...
    };
    
    /**
     * f2 JsDoc, note: "this.f2 = f2;" occurs after function 'f2' in text
     * @param {string} f2arg f2arg JsDoc
     */
    function f2(f2arg) {
        // many lines of text ...
    };
    this.f2 = f2;
    
    // (1.1) Intellisence - OK
    f1("arg");
    // (1.2) Intellisence - OK
    f2("arg");
};

// (2.1) Intellisence is NOT aware about myLib.f1().
myLib.f1();

// (2.2) Intellisence is NOT aware about myLib.f2().
myLib.f2();    