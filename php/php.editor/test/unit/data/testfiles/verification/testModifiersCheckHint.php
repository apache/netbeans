<?php
//START

class ClassMethods {

    abstract private function classAbstractPrivate();

    public abstract function classWithBody() {

    }

}

interface IfaceMethods {

    private function ifacePrivateMethod();

    protected function ifaceProtectedMethod();

    public final function ifaceFinalMethod();

}

class PossibleAbstract {

    abstract public function possibleAbstract();

}

final class FinalAbstract {

    abstract public function finalAbstract();

}

//END
?>