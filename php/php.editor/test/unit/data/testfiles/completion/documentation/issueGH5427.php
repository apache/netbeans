<?php

class ClassName {

    //The type should be displayed here. 
    public string $test_without_doc;  

    /**
     * The type should be displayed here.. 
     */
    public string $test_without_var_tag;

    /**
     * The type should be displayed here. 
     * @var string
     */
    public string $test_with_var_tag;
    
    public function test() { 
        $this->test_without_doc;
        $this->test_without_var_tag;
        $this->test_with_var_tag;
    }

}

?>