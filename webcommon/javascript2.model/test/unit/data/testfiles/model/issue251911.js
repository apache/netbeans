var MyLib = new function MyLib() {
    var _myLib = this;
 
    var TRUE   = !0,
        FALSE  = !1;
 
    _myLib.in = _myLib_in; 
    
    function _myLib_in(item, container) {
        if( _myLib_isArray(container) || _myLib_isStr(container) )
            return _myLib_indexOf(container, item) >= 0;
        
        for( var key in container ) {
            if( container[key] === item )
                return TRUE;
        }
        
        return FALSE;
    }
       
};