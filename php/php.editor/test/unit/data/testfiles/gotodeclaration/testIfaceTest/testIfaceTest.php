<?php
class mycls implements myface {
    const RECOVER_ORIG = 1;
    function mfnc() {}//mycls
}
interface myface {
    const RECOVER_ORIG = 2;
    function mfnc();//myface
}

myface::RECOVER_ORIG;
mycls::RECOVER_ORIG;

function function_face(myface $a) {
    $a->mfnc();//myface
}

function function_cls(mycls $a) {
    $a->mfnc();//mycls
}
?>
