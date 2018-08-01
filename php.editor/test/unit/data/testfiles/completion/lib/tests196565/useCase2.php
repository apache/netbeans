<?php

class ParentSe {
    /** @return self */
    public static function staticFnc() {}

    public function parentFnc() {}
}

class ChildSe extends ParentSe {
    public function childFnc() {}
}

ParentSe::staticFnc()->; //cc parentFnc, staticFnc

ChildSe::staticFnc()->; //cc parentFnc, staticFnc

?>