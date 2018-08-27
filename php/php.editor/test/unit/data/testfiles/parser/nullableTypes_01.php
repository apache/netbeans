<?php

function answer(): ?int {
    return null;
}

function answer2(): ?MyClass {
    return 77;
}

function answer3(): ?\Foo\Bar {
    return new \Foo\Bar();
}
