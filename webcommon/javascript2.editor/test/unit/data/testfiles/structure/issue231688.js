function Test() {
    var test = this;

    this.logger = new function() {
        this.msg = "";
        this.print = false;

        function hiddenFnc() {
        }

        this.log = function(title, msgs, level, style) {
        };

    };
}