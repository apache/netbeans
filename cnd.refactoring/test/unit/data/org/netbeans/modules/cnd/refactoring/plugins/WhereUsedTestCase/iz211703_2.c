
/* from iz211703_1.c */
extern long *get_execsect_fillbuf211703(long nbytes, int chip);
extern int exceeds_max_align211703(int nbytes);

void foo211703() {
    exceeds_max_align211703(10);
    get_execsect_fillbuf211703(11, 21);
}
