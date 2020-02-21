struct ABC {int aaa; };
int main(int argc, char** argv) {
    ABC abc;
    abc.aaa = 0; // write
    abc.aaa++; // read/write
    ++abc.aaa; // read/write
    int i = abc.aaa; // read
    return 0;
}
