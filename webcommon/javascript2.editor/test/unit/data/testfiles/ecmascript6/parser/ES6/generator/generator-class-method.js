class Demo {
    * generator1() {
        yield 1;
        yield 1 + 10;
    }

    * generator2(i) {
        yield i;
        yield i + 10;
    }

    async * generator3() {
        yield 1;
        yield 1 + 10;
    }

    async * generator4(i) {
        yield i;
        yield i + 10;
    }
}