test.run(function () {
    "use strict";

    var layout;

    TEST = {
        test: function () {
            if (layout) {
                layout.destroy();
                layout = null;
            }
        }
    };
});