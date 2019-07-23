function Obj() {
    this.buf = [];
    this.peek = function peek(pos) {
        var buf = this.buf,
            ch = (pos < buf.length ? buf[pos] : '\0'); // problem with first 'buf'
        return ch;
    };
}