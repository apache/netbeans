<?php

namespace Foo {
    class Bar {

        function someFunc() {}

    }
}

namespace Baz {
    use \Foo\Bar;
    class Bat extends Bar {

        public function someFunc() {
            parent::someFunc();
        }

    }

}
?>