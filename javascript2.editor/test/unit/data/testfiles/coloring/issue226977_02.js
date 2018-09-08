function Test() {
    this.onLoading = function() {};
    this.runAJAX = function(urlstring) {
        this.responseStatus = new Array(2);
        var self = this;
        this.xmlhttp.open(this.method, urlstring, this.asynchronous);
        this.xmlhttp.onreadystatechange = function() {
            switch (self.xmlhttp.readyState) {
                case 1:
                    self.onLoading();
                    break;
                case 2:
                    self.onLoaded();
                    self.declare = 1; // self is purple
                    break;
            }};
    };
}  