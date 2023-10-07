<?php

class MyFoo {
    function __construct($foo) {
    }
    function myFnc() {
    }
}

(new \MyFoo("Whatever can be here"))->myFnc();
(new \MyFoo("Whatever can be here"))->notMyFnc();

?>