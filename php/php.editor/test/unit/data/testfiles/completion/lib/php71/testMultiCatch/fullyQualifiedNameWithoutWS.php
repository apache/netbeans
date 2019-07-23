<?php

try {
    
} catch(\Test\Sub\ExceptionType1|\Test\Sub\ExceptionType2 $e) {
    echo $e->getTraceAsString(); // multi
} catch(\Test\Sub\ExceptionType3 $e) {
    echo $e->getTraceAsString(); // single
} finally {
    
}
