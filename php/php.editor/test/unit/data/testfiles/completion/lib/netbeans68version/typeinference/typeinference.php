<?php
/**
 * @property clsA $fld2
 */
class clsA {
    /**
     *
     * @var clsA
     */
    public $fld;
    function methodA1() {
        $fncA = fncA();
        return $fncA;
    }
    function methodA2() {
        $methodA1 = $this->methodA1();
        return $methodA1;
    }
    function methodA3() {
        $methodA2 = $this->methodA2();
        return $methodA2;
    }
    function methodA4() {
        return $this;
    }
    function methodA5() {
        return $this->methodA1()->methodA2()->methodA3()->methodA4();
    }
    function methodA6() {
        //recursion not allowed
        return $this->methodA1()->methodA2()->methodA3()->methodA4()->methodA5()->methodA6();
    }
    function methodA7() {
        $methodA1 = $this->methodA1();
        $methodA2 = $methodA1->methodA2();
        $methodA3 = $methodA2->methodA3();
        $methodA4 = $methodA3->methodA4();
        return $methodA4->methodA5();
    }
}
function fncA() {
    return new clsA();
}
function fncB() {
    $fncA = new mysqli();
    $fncA = fncA();
    return $fncA;
}
function fncC() {
    $clsA = new clsA();
    $clsB = $clsA;
    $clsC = $clsB;
    return $clsC;
}
function fncD() {
    $clsA = new clsA();
    return $clsA->methodA4();
}
function fncE() {
    $clsA = new clsA();
    return $clsA->methodA5();
}
function fncF() {
    $clsA = new clsA();
    return $clsA->methodA6();
}
function fncG() {
    $clsA = new clsA();
    return $clsA->methodA7();
}
function fncH() {
    switch ($variable) {
        case value1:
            return new mysqli();
        case value1:
            return new clsA();

    }
}
function fncI() {
    $clsA = new clsA();
    $clsA->fld = new clsA();
    $fld = $clsA->fld;
    return $fld;
}


$clsA = new clsA();
$clsA->methodA1()->fld;
$clsA->methodA2()->fld;
$clsA->methodA3()->fld;
$clsA->methodA4()->fld;
$clsA->methodA5()->fld;
$clsA->methodA7()->fld;
fncA()->fld;
fncB()->init();
fncC()->fld;
fncD()->fld;
fncE()->fld;
fncG()->fld;
fncH()->fld;
fncI()->fld;
fncF()->fld;

$clsAForFlds = new clsA();
$clsAForFlds->methodA1()->fld->methodA1();
$clsAForFlds->methodA1()->fld2->methodA1();
?>
