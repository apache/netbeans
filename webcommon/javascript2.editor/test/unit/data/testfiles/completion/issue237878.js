var Synergy = {model: {}};
Synergy.model.TestCase = function(title) {
    this.title = title;

    this.print = function() {
        console.log("printing....");
    };
};  

var a = new Synergy.model.TestCase("A");
a.
        