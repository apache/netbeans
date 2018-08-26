<?php

interface Iface {
    function test1();
}

$a = new class implements Iface {
};
