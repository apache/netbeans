function Bulici() {
    this.pepa = new Clobrda("pepa");
    this.lidickove = [this.pepa, new Clobrda("jozin")];
}
function Clobrda(jmeno) {
    this.jmeno = jmeno;
}
function init() {
    ko.applyBindings(new Bulici());
}

