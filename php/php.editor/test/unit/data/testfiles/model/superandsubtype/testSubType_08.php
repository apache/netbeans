<?php

namespace TestNameSpace;

interface Super {}

interface Mid1 extends Super {}

interface Mid2 extends Mid1 {}

interface Mid3 extends Mid2 {}

class Sub implements Mid3 {}

?>