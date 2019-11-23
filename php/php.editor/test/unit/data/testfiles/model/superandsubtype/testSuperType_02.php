<?php

namespace TestNameSpace;

class Super {}

class Mid1 extends Super {}

class Mid2 extends Mid1 {}

class Mid3 extends Mid2 {}

class Mid4 extends Mid3 {}

class Sub extends Mid4 {}

?>