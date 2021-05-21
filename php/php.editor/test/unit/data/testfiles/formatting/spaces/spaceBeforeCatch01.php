<?php

try {
   echo inverse(5) . "\n";
   echo inverse(0) . "\n";
}catch (Exception $e) {
   echo 'Caught exception: '.  $e->getMessage(). "\n";
}

?>
