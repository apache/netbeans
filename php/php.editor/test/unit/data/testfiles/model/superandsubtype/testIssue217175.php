<?php
namespace TestNameSpace;

interface Sub extends Bar {
    function fnc();
}

interface Bar extends Sub {
}

interface Super extends Sub {
}

class MyCls {
    /**
     * @return Super|Sub
     */
    function omg() {}
}

?>