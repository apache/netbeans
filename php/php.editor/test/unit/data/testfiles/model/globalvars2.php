<?php
class MyCls {
    /**
     * @return MyCls
     */
    static function mystaticfnc() {
        return new MyCls;
    }
    /**
     * @return MyCls
     */
    function myfnc() {
        return new MyCls;
    }
    /**
     * @return MyCls2
     */
    function myfnc2() {
        return new MyCls2;
    }
}

class MyCls2 extends MyCls{
}
$my = new MyCls();
$our = $your = $my;
$my = new MyCls2;
$ourComplex = $our->myfnc()->myfnc()->myfnc2();
$otherComplex = MyCls::mystaticfnc();
$foreign2 = $foreign->getVal();
$last = $otherComplex;

function myfnc() {
    $my = new MyCls();
    $our = $your = $my;
    $my = new MyCls2;
    $ourComplex = $our->myfnc()->myfnc()->myfnc2();
    $otherComplex = MyCls::mystaticfnc();
    $foreign2 = $foreign->getVal();
    $last = $otherComplex;
}
?>
