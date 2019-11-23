<?php

namespace MyVendor\PackageOne;

use MyVendor\PackageThree\MyIface;
use MyVendor\PackageTwo\MyTrait;

class MyClass implements MyIface {
    use MyTrait;
}

namespace MyVendor\PackageTwo;

trait MyTrait {
}

namespace MyVendor\PackageThree;

interface MyIface {
}
