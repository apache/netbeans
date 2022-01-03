
int method1() {
    int i = 0;
    int j = i + 4;
    return j;
}

int method2(int i) {
    return i + method1();
}



void method3() {

    int res = method1() + method2();

}
