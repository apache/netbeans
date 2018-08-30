var myLib = new MyLib();

function MyLib() {
    /**
     * This is dummy JsDoc
     */
    this.myMethod = function MyLib_myMethod(array) {
        var r = "";
        for( var i = 0; i < array.length; i++ ) {
            r = r + array[i];
        }
        return r;
    };
}

myLib.myMethod([1, 2, 3, 4]);
     