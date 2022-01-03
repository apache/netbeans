typedef struct AVRational {
    int num;
    int den;
} AVRational;

void foo() {
    AVRational av1(1, 2), av2;
    av2 = (AVRational){1, av1.den};
}