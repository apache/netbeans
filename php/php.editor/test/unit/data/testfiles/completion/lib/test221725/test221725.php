<?php

class SetFoo
{
    /**
     *
     * @param type $foo
     * @return \SetFoo
     */
    public function setFoo($foo)
    {
        $this->foo = $foo;
        return $this;
    }
}
class God {

    function help() {
    }

    function me() {
    }

}

$class = new SetFoo();
$class->setFoo(new God())->setFoo($foo);
?>