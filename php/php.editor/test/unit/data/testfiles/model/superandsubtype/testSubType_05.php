<?php

namespace TestNameSpace;

interface Super {}

interface Mid1 extends Super {}

interface Mid2 extends Mid1 {}

interface Mid3 extends Mid2 {}

interface Mid4 extends Mid3 {}

interface Sub extends Mid4 {}

?>