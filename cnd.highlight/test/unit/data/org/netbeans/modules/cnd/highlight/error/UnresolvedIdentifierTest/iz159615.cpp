struct A {
    int i;
    A operator++() {
        i++;
        return *this;
    }
    A operator->() {
        i++;
        return *this;
    }
};
int main() {
    A a;
    a -> i;
    return 0;
}