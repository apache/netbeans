<?php

namespace TestNameSpace;

interface Super {}

class Mid1 implements Super {}

class Mid2 extends Mid1 {}

class Mid3 extends Mid2 {}

class Sub extends Mid3 {}

?>