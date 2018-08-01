<?php

class ClassName {

    /**
     * Summary.
     *  
     * @var array This is desc.
     */
    public $a_with;
    
    /**
     * Summary.
     *  
     * @var array
     */
    public $b_without;
    
    /**
     * @return Foo GOOD.
     */
    function functionName() { 
        $this->a_with;
        $this->b_without;
    }

}

?>