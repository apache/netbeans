var SeLiteMisc= {};
SeLiteMisc.isInstance= function isPrdca( object, classes, className, message ) {
    var arr= [object, classes, message, className ];
    
    if( typeof classes==='function' ) {
        classes= [classes];
    }
    for( var i=0; i<classes.length; i++ ) {
        var clazz= classes[i];
        if( object ) {
        }
    }    
    return false; 
};