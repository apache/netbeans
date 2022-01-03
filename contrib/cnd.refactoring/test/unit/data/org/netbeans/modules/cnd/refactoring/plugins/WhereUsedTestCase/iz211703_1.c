
long *get_execsect_fillbuf211703(long nbytes, int chip);
int exceeds_max_align211703(int nbytes);

int exceeds_max_align211703(int nbytes)
{
   return (nbytes>2);
}

void function211703() {
    exceeds_max_align211703(11);
    get_execsect_fillbuf211703(1, 2);
}

long *get_execsect_fillbuf211703(long nbytes, int chip) {
    function211703();
}