(function () {
        /**
         * @public                  // this doesn't belong here
         */
        function foo() {
            return 'foo';
        }

        function bar() {
            return foo().length;    // "foo" has the global variable color
        }

        function baz() {
            return foo();           // this "foo" is fine
        }
    }());   