<?php

class ClassName {

    private $boo; // camel or under?
    private $Boo_baz; //absolutely wrong
    private $bar; // camel or under?
    private $baz_bat; // under!!
    private $bazBat; // ...camel is wrong

    function functionName() {
        $this->BarBaz;
        $this->barbaz;
        $this->bar_baz;
        $this->Bar_baz; // absolutely wrong
    }

}
?>