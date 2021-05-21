function Test() {
    this.listeners = [];
    var subs = this.listeners[event] || [];
    for (var i = 0, max = subs.length; i < max; i += 1) {
        if (subs[i] === fn) {
            subs.splice(i, 1);
        }
    }
}  