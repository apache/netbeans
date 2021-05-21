function initGa() {
    var ugaVarName = "ga";
    var uga = window[ugaVarName];

    if( !uga ) {
        uga = function() {
            uga.q.push(arguments);
        };
        
        uga.q = []; // <-- here: The global variable "uga" is not declared.
        window[ugaVarName] = uga;
    }       
}
     