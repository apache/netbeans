<?php
try {
    // something
}catch(ExceptionType1|ExceptionType2 $e) {
echo $e->getTraceAsString();
} catch (ExceptionType3 $e) {echo $e->getTraceAsString();
} finally {
    // test
}
