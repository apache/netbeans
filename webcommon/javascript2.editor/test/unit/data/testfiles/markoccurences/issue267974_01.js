function(method) {
    SchemaBuilder.prototype[method] = function () {
        this._sequence.push({
            method
        });

    }
}