<?php
interface I {
    /**
     * @param I $interface Description
     * @return I interface
     */
    function testInterface(I $interface);

}

/**
 * @method I testClass2(I $class) Description
 * @property I $prop Description
 */
class C
{
    /**
     * @param I $class Description
     * @return I class
     */
    function testClass(I $class){
    }
}

trait T {
    /**
     * @param I $trait Description
     * @return I trait
     */
    function testTrait(I $trait){
    }
}
