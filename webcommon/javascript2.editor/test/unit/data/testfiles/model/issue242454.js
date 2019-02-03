var myLib = new function MyLib() {
   
    
    /**
     * ShareableFunc
     * @param {string} arg description
     */
    function PublicAndPrivateUsageFunc(arg) {}
    
    this.publicInnerFunc = PublicAndPrivateUsageFunc;
    
   
};

myLib.
        