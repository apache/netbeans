
void dtrace_dof_difo() {
    static const struct {
        int section;
    } difo[] = {
        {1},
        {2}
    };
    int l = difo[0].section;
    l++;
}
