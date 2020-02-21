namespace bug241017 {
    struct A241017 {
        A241017(int x) {}
    };

    int main241017() {
        int a = 1;
        A241017* ap = new A241017[4]{a, a, a, a}; 
        return 0;
    }
}