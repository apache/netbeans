--TEST--
Heredocs can be used as default property value.
--FILE--
<?php

class e {

    public $e = <<<THISMUSTNOTERROR
If you see this, everything is ok.
THISMUSTNOTERROR;

};
$e = new e();
print $e->e . "\n";

?>
--EXPECT--
If you see this, everything is ok.
