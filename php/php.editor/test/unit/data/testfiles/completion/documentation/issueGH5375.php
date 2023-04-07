<?php

class ClassName {

    /**
     * This text should be displayed here.
     */
    public array $test_without_var_tag;

    /**
     * This text should be displayed here. 
     * @var array
     */
    public array $test_with_var_tag;
    
    public function test() { 
        $this->test_without_var_tag;
        $this->test_with_var_tag;
    }

}

?>