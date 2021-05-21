jasmine.TrivialReporter = function(doc) {
    this.document = doc || document;
    this.suiteDivs = {};
    this.logRunningSpecs = false;
};

jasmine.TrivialReporter.prototype.createDom = function(type, attrs, childrenVarArgs) {
    return 1;
};