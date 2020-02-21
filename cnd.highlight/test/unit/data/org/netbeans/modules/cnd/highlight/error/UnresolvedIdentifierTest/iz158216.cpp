struct iz158216_tm {
};
int iz158216_main() {
    iz158216_tm mytm;
    (void) ({
        iz158216_tm mytm2; // unresolved
        mytm2 = mytm; // unresolved
    });
    mytm ++;
    return 0;
}
