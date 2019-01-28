<?php
try {
            if($foo== 'bar'){} else{ return false; }
        } catch(InvalidArgumentException $e) {
            echo "an exception";
            return false; 
        }
?>