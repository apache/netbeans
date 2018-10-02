!(function() {
    function Test() {
       
        this.publisher = new function() {
            this.listeners = [];  
            this.name = "Jitka";
            this.subscribe = function(callback, event) {
                event = event || 'any';
                if (typeof this.listeners[event] === "undefined") {
                    this.listeners[event] = [];
                }
                this.listeners[event].push(callback);
            };
        };
    }
}());