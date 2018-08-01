<?php

namespace Foo\Bar;

class FB {

    const CON = 1;

}

namespace Bar\Baz;

use Foo\Bar\FB as Ali;

class BB {

    /**
     *
     * @param Ali $param
     */
    function functionName(Ali $param) {
        Ali::CON;
    }

}

?>