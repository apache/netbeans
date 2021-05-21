<?php

try {
    echo "";
} catch (ExceptionType1 $e) {
    echo "";
} catch (\Exception | ExceptionType2 $e) {
    echo "";
}
