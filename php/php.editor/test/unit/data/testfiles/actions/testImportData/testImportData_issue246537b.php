<?php

namespace One;

trait MyTrait {
}

namespace Two;

class MyTrait { // not a trait!
}

namespace Three;

class MyClass {
    use MyTrait;
}
