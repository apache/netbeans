function Man (firstName) {
    var firstName = firstName; 

    this.getFirstName = function () {
        return firstName;
    };
}