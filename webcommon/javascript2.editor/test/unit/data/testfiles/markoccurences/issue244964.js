var foo;

    (function () {
        var bar;

        foo = {
            setBar: function () {
                if (!bar) {
                    bar = {};      // "bar" has global variable color
                                   // and ctrl-click doesn't navigate above
                }
            }
        };
    }()); 