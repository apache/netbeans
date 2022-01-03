int bug222883_main(int argc, char *argv[]) {
    int i = 0;
    try {
        G_1:
        i++;
        goto G_1;
    } catch (...) {
    }
    return 0;
}