
var plyn = {
    count: 10
};

function helpId() {
    return 20;
}

function  Issue ( ){
    this.type = "type";
    this.amount = plyn.count;
    this.id = helpId();
};

Issue.prototype.getType = function () {
    return this.type;
};

Issue.prototype.getAmount = function () {
    return this.amount;
}

Issue.prototype.getId = function () {
    return this.id;
}

var is = new Issue();
is.getType().sub();
is.getId().toString();
