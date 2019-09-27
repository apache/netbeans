<?php

class Koza {

    function __construct() {
        
    }
    
    /**
     * @param type $keyClosure closure returning the key
     */
    protected static function test($keyClosure) {
        $key = $keyClosure($row->KEY); // <-- not highlighted
    }

}
    
?>