
void iz142674_foo() try {
    int k;
    k++;
    throw 1;
} catch (int i) {
    i++;
}

struct iz142674_C {
    iz142674_C() try {
        int k;
        k++;
        throw 1;
    } catch (int i) {
        i++;
    }

    void iz142674_foo() try {
        int k;
        k++;
        throw 1;
    } catch (int i) {
        i++;
    }
};
