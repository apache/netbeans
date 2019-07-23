<?php
class MyCls {
    /**
     * @param type $prm
     * @param DateTime $param
     * @return \MyCls
     */
    function fnc($prm, DateTime $param) {
    }
}
$myCls = new MyCls();
$myCls->fnc("", new DateTime())-> // CC here
$myCls->fnc(new DateTime(), "")-> // CC here
$myCls->fnc(new DateTime())-> // CC here
?>