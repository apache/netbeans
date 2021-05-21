var myLib = function MyLib() {
    var f1;
    /**
     * f1 JsDoc
     * @param {string} f1arg f1arg JsDoc
     */
    f1 = this.f1 = function f1(f1arg) {};
    
    /**
     * f2 JsDoc
     * @param {string} f2arg f2arg JsDoc
     */
    var f2 = this.f2 = function f2(f2arg) {};

    // (1.1) Intellisence DOES work correctly for 'f1'
    //f1("arg");
    // (1.2) Intellisence PARTIALLY works for f2() 
    // - no list of arguments displayed in combo
    //   though JsDoc is displayed correctly
    //f2("arg");
    f;
};

// (2) Intellisence works for f1().
//myLib.f1();
myLib.;

// (3) Intellisence is NOT aware about myLib.f2().
//myLib.f2();