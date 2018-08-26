<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
namespace Libs\Bar;

include 'Libs/Kolesa/Buz.php';
include 'Libs/Bar/Buz.php';
include 'Libs/Tetov/Buz.php';
include 'Libs/Komarov/Buz.php';

/**   
 * Description of Foo   
 *
 * @author cesilko    
 */ 
class Foo  extends Buz { // bere z namespace Libs\Bar
    function foo(IBuz $buz) {     
        return $buz->barMoje();
    }
    
    public function mama() {
        parent::mama();
    }

}

use \Libs\Komarov;
use Libs\Kolesa\Buz;
use Libs\Kolesa\Buz as Alias1;
use Libs\Komarov as Alias2;

class Foo2 extends Buz {    // bere z koles. use Libs\Kolesa\Buz ma prednost pred namespace
}

class Foo3 extends \Libs\Tetov\Buz { // FQN 
}

class Foo4 extends Komarov\Buz {  // slozi se s Libs\Komarov
}


class Foo5 extends Alias1 {  // slozi se s Libs\Koles diky use Alias1
}

class Foo6 extends Alias2\Buz {  // slozi se s Libs\Komarov diky use Alias2
}




?>