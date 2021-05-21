<?php

namespace Name\Space {

    class SomeClass {

        public function autocomplete() {

        }

    }

    $object = $this->getObject()->getOtherObject();
    assert($object instanceof SomeClass);
    $object->autocomplete(); // 1
}

namespace Other\Name\Space {
    $object = $this->getObject()->getOtherObject();
    assert($object instanceof \Name\Space\SomeClass);
    $object->autocomplete(); // 2
}

namespace More\Name\Space {

    use Name\Space\SomeClass;

    $object1 = $this->getObject()->getOtherObject();
    assert($object1 instanceof \Name\Space\SomeClass);
    $object1->autocomplete(); // 3

    $object2 = $this->getObject()->getOtherObject();
    assert($object2 instanceof SomeClass);
    $object2->autocomplete(); // 4
}

?>