var ABC = {
    x: '',

    y: function () {
        var x;

        x = 'abc';

        this.x = x; // wrong hint: The global variable 'x' is not declared
    }
};