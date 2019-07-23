<?php

class some_class {

    /**
     *
     * @global test $variable 
     */
    public function some_method() {
        global $variable;
        echo $variable->getTestName();
    }

}