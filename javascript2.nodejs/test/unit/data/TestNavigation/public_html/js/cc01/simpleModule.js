
function Message() {

}

Message.prototype.setText = function (text) {
    this.text = text;
}

Message.prototype.setCode = function (code) {
    this.code = code;
}

function OutputMessage() {
    this.output = [];
    this.outputEncodings = [];
    this.writable = true;
}


module.exports.message = new Message();
module.exports.Out = OutputMessage;

