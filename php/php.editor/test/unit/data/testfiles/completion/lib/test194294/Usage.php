<?php

class UsageReturnTypeInOtherFile {

    function example() {
        $cls2 = new Cls2();
        $cls1 = new Cls1();

        Cls1::getInstance()->getName()->getName();
        $cls1->getName()->getInstance();

        $cls2->doSomething()->getInstance();
        
        
        
        
    }

}