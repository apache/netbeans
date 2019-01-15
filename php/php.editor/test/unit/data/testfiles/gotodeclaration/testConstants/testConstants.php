<?php

namespace Foo {
    class Bar {
        const CON = 1;
    }
}

namespace Baz {
    use \Foo\Bar;
    class Bat extends Bar {

        public function someFunc() {
            parent::CON;
            self::CON;
            static::CON;
        }

    }

}

?>