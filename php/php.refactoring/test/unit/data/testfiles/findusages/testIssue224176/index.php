<?php

class Test {
    public $hello;
    public function __Construct(){
        $this->hello = 'hello';
    }
}

class Test2 {
    public function TestFunc(Test $fred) {
        echo $fred->hello;
    }
}

class Test3 {
    public function TestFunc($fred) {
        echo $fred->hello;
    }
}

?>