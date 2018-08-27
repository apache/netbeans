<?php

class NullableTypes {

    public function returnType1(): ?string {
    }

    public function returnType2(): ?\Foo\Bar {
    }

    public function parameterType1(?Foo $foo) {

    }

    public function parameterType2(?Foo $foo, string $bar) {

    }

    public function all(?\Foo $foo, string $bar, ?callable $baz): ?string {

    }

}
