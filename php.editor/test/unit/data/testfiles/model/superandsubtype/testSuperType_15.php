<?php

namespace TestNameSpace;

trait Super {}

class Mid {
    use Super;
}

class Sub extends Mid {}


?>