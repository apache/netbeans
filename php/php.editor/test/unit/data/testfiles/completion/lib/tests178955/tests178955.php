<?php
class foo2 {
    public function getBar2(){}
}

class foo {
    /**
     * @return foo2
     */
    public static function find( ){}
    public function getBar(){}

}
if ( false ) {
    $foo = new foo();
    /*use case 1*/$foo->find();

} else {
    $foo = foo::find( $id );
    /*use case 2*/$foo->getBar2();
}
/*use case 3*/$foo->find();

$foo = foo::find( $id );
/*use case 4*/$foo->getBar2();

try {

} catch(Exception $exc) {
    $foo = new foo();
    /*use case 5*/$foo->find();
}
/*use case 6*/$foo->getBar2();

?>