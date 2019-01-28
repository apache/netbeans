var value1 = 1, value2 = 2;

if( value1 = value2 ) {
    // definitelly valid warning
}
else if( value1 = getValue("condition A") ) {
    // typically valid warning ... 
}
else if( !(value1 = getValue("condition A")) ) {
    // OK: already does not complain, not sure why ...
}
else if( (value1 = getValue("condition A")) ) {
    // ... but (tentativelly) there should be 'some syntax' 
    // to let the inspector know that I indeed want to use implicit JS's
    // "value-to-boolean" convertion - probably additional brackets around 
    // expression is enough. 
    // 
    // Longer syntax that already works is below
    if( !!(value1 = getValue("condition A")) ) {
        // OK: already does not complain
    }
}
else if( (value1 = getValue("condition A")) !== null ) {
    // false positive warning - there is explicit boolean expression
}
else if( (value1 = getValue("condition B")) !== null ) {
    // false positive warning - there is explicit boolean expression
}
else if( (value1 = getValue("condition B")) !== null ) {
    // false positive warning - there is explicit boolean expression
}

//////////////////////////////////////////////////////////////////////

function getValue(condition) {
    if( condition )
        return "";
    return null;
}