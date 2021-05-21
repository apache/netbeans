var Ob = {
    fireEvent: function(key, value) {
        var property = this.config[key];

        if (property && property.event) {
            property.event.fire(value);
        }
    }

}; 