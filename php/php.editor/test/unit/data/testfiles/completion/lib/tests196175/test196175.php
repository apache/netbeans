<?php
// no completion, beacuse IndexController is from namespace
// and here is no "namespace App\Test;" or "use \App\Test\IndexController;"
$a = new IndexController();
$a->doesNotWorkHere();