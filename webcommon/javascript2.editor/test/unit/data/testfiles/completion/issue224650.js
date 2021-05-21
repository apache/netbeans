function Test() {
    var test = this;

    this.logger = new function() {
        this.msg = "";
        this.print = false;

        function hiddenFnc(){}

        this.printMessage = function(title, msg, style) {
            this.bar = msg;
        };
        
        this.log = function(title, msgs, level, style) {
            this. // cc here
        };

    };
}  