int compare_int(m1, m2)
    const void *m1;
    const void *m2;
{
    int mi1 = *(const int *)m1;
    int mi2 = *(const int *)m2;

    return (mi1 - mi2);
}
