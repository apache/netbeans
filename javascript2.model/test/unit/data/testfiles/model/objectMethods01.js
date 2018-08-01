
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