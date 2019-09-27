<?php


class test {

    function __construct() {
    }

    public function start($param) {
        $aa = $this->loadclass1();
        $aa->

        $bb = $this->loadclass2();
    }

    /**
     * @return ClassName 
     */
    private function loadclass1() {
        return new ClassName();
    }

    private function loadclass2() {
        return new ClassName;
    }

}

class ClassName {
    function aa() {}
    function bb() {}
    function cc() {}
}