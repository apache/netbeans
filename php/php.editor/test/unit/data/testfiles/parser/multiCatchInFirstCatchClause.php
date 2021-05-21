<?php

try {
    echo "";
} catch (ExceptionType1 | ExceptionType2 $e) {
    echo "";
} catch (\Exception $e) {
    echo "";
}
