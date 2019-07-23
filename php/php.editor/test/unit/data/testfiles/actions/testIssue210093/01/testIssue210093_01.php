<?php

namespace Issue\Martin {

    class Pondeli {}

}

namespace {

    use \Issue\Martin\Pondeli;

    function testOk(Pondeli $param) {}

    function testFail(\Issue\Martin\Pondeli $param) {}

}
?>