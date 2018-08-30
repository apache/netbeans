var MyLib = new function MyLib() {
    var _events;
     
    this.on = _on; 
    
    function _on(evtId, handler) {
        var handlers = _events[evtId];
        
        if( !handlers )
            _events[evtId] = handlers = []; // <-- related line
        
        return handlers.push(handler); // <-- here
    }; 
};   
   
