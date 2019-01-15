<?php

namespace Foo\Bar\Baz {
    class Def{}
}

namespace Bat {

    class ClassName {

        /**
         * @return Foo\Bar\Baz\Def
         */
        function functionName() {
            return new Foo\Bar\Baz\Def();
        }

    }
}

?>