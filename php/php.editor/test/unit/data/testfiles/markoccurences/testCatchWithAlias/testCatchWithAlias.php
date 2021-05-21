<?php
namespace Blah\Sec;

class MyException {}

namespace Omg;
use Blah\Sec as BS;
try {
    new BS\MyException();
} catch (BS\MyException $ex) {
    echo $ex;
}
?>