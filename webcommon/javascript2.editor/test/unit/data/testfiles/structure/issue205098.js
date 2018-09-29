var Test = function() {
    this.prop1 = true //shows up in navigator pane
};

Test.prototype.addItems = function() {
    this.prop2 = true; //doesn't show up in navigator pane
}
