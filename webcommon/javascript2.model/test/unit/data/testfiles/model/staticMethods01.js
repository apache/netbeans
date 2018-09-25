
function Ridic(name, surname){
    this.name =  name;
    this.surname = surname;

    this.getName = function() {
        return /*this is ugly comment*/this.name;
    }
}

Ridic.prototype.getInfo = function () {
    return "info";
}

Ridic.getFormula = function(type) {
    if ( type == 1 ) return "l * w";
    if ( type == 2 ) return "1/2 * b * h";
    if ( type == 3 ) return "pie * r^2";
}