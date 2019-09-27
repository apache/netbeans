<?php

class MyConfig {
    /**
     *
     * @param Ty;p;e $param Type with semicolons
     */
    function functionName($param) {
        /* @var $foo omg;wtf */
        $param = <<<OMG
                My string ;; with semicolon
OMG;
    }

}

?>