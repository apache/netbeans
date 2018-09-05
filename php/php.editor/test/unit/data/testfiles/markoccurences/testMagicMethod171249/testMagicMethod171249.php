<?php
 
/**
 * @method   OldClass newerFunction <- intentional spaces after @method
 */
class NewClass { 

    public function newFunction() {
        $this->newerFunction(); 
        return new OldClass();
    }
}

class OldClass {
    
}
?>