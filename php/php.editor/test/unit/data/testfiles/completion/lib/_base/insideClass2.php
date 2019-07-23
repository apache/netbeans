<?php
class cls_A {
    function meth_a() {
        $this->meth_aa();
    }
    function meth_aa() {}
}

function fnc_b() {
    class cls_B {
        public function meth_b() {
            if (0) {
                $this->meth_b();
            }
        }
        protected function meth_bb() {}
    }
    $bVar = new cls_B();
    /**/$bVar->meth_b();
}

$aVar = new cls_A();
$aVar->meth_a();
?>
