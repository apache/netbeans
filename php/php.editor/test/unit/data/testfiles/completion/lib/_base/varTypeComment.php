<?php

class VarTypeCommentTest{}

/**
 *
 * @return string
 */
function printMyName() {
    echo "Krtecek";
}

{
    /* @var $hello VarTypeCommentTest */
    $hello = new DOMAttr();

    /*second comment*//* @var $hello2 type */
    $hello2 = get_browser();
}
?>
