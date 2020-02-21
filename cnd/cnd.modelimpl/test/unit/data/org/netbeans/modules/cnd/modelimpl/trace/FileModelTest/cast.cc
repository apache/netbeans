
int main() {
    static_cast<void (*) ()> (0);

    int v;
    reinterpret_cast<int*> (v);
    reinterpret_cast<int (*)> (v);

    return 0;
}
