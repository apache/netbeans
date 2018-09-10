<?php
    class Test147883A {
        function test(){}
    }

    class Test147883B extends Test147883A {
       function test(){}
    }

    $a = new Test147883B();
    echo $a->test();
?>
