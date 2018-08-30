<?php

class ClassName {

    private $BooBaz; // just studly caps, others are wrong
    private $Boo_baz; //absolutely wrong
    private $bar;
    private $baz_bat;
    private $bazBat;

    function functionName() {
        $this->BarBaz; // ok
        $this->barbaz;
        $this->bar_baz;
        $this->Bar_baz; // absolutely wrong
    }

}
?>