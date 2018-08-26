<?php

namespace One;

trait MyTrait {
}

namespace Three;

class MyClass {
    use MyTrait;
}
