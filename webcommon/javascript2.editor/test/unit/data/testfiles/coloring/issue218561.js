
Core.test(function() {
    var test = {
        /**
         *
         * @param {String} par1
         * @returns {undefined}
         */
        test: function(par1) {
            this.par1 = par1; // par1 after = is marked green as member variable
        },
        test2: function(par1) { // par1 is underlined as unused
            var object = new Test({
                par1: par1 // par1 after : is marked green as member variable
            });
        }
    };
});