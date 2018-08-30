<?php
use Test\Sub\{ExceptionType1, ExceptionType2, ExceptionType3};

try {
    
} catch (ExceptionType1 | ExceptionType2 $e) {
    echo $e->getTraceAsString(); // multi
} catch (ExceptionType3 $e) {
    echo $e->getTraceAsString(); // single
} finally {
    
}
