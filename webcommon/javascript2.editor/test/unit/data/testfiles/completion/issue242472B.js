var myLib = new function MyLib() {
    
    /**
     * f3 JsDoc, note: "this.f3 = f3;" is inlined between JsDoc and f3(), what 
     * is syntactically incorrect - JsDoc should be assigned to 'this.f3' but it 
     * would be nice to recognize it as JsDoc for f3() function instead.
     * @param {string} f3arg f1arg JsDoc
     */
    this.f3 = f3; function f3(f3arg) {
        // many lines of text ...
    };

    // (1.3) Intellisence does NOT show JsDoc
    f;
    this.f;
};

// (2.3) Intellisence is NOT aware about myLib.f3().
myLib.;