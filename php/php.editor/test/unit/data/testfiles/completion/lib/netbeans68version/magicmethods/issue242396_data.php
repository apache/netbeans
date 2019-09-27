<?php
namespace One {
    use Two\Cls as Alias;
    /**
     * @method Alias doSmth
     */
    class OneCls {

        function __construct() {
            
        }

    }
    
}

namespace Two {
    class Cls {
        function addddd() {}
    }
}

namespace Third {
    class Fac extends Fac2 {
    }
    
    use One\OneCls as Child;
    
    class Fac2 {
 
        /**
         * @return Child
         */
        public static function create() {
            
        }

    }
}