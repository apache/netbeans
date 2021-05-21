<?php

namespace First;

interface MyFace {}

namespace Second;

use First\MyFace;

interface AnotherFace extends MyFace {}

namespace Third;

use Second\AnotherFace;

class Foo implements AnotherFace {}

?>