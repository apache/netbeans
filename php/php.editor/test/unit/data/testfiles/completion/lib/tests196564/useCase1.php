<?php

class ParentTh {
    /** @return this */
    public static function staticFnc() {}

    public function parentFnc() {}
}

class ChildTh extends ParentTh {
    public function childFnc() {}
}

ParentTh::staticFnc()->; //cc parentFnc, staticFnc

ChildTh::staticFnc()->; //cc parentFnc, staticFnc + childFnc

?>