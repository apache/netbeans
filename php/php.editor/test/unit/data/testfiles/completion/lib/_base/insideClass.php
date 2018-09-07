<?php
class InsideClass {
    const INS_CONST=1;
    private $fld = 0;
    public static $stfld=1;

    public function setFld($fld) {
        InsideClass::$stfld;
        $this->fld = $fld;
    }
    function __construct() {
        $this->setFld(1);
    }
    function __destruct() {
        $this->setFld(0);
    }
}
$v = new InsideClass();
$v->setFld(2);
?>