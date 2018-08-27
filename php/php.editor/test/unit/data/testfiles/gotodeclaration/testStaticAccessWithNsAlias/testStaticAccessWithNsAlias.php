<?php

namespace Foo {

    class Bar {
        const OMG = 1;
        public static $staticField = 2;
        static function someFunc() {

        }

    }

}

namespace Baz {

    use \Foo\Bar as Second;

    class Bat extends Second {

        public function someFunc() {
            parent::OMG;
            self::OMG;
            static::OMG;

            parent::$staticField;
            self::$staticField;
            static::$staticField;

            parent::someFunc();
        }

    }

}

?>