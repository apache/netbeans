<?php

class ParentSt {
    /** @return static */
    public static function staticFnc() {}

    public function parentFnc() {}
}

class ChildSt extends ParentSt {
    public function childFnc() {}
}

ParentSt::staticFnc()->; //cc parentFnc, staticFnc

ChildSt::staticFnc()->; //cc parentFnc, staticFnc + childFnc

?>