double val(my_bool *null_value)
{
    void (*func)(UDF_INIT *, uchar *, uchar *)=
    (void (*)(UDF_INIT *, uchar *, uchar *)) u_d->func_clear;
    func(&initid, &is_null, &error);

double (*funcD)(UDF_INIT *, UDF_ARGS *, uchar *, uchar *)=
  (double (*)(UDF_INIT *, UDF_ARGS *, uchar *, uchar *)) u_d->func;
double tmpD=funcD(&initid, &f_args, &is_null, &error);

    longlong (*funcL)(UDF_INIT *, UDF_ARGS *, uchar *, uchar *)=
      (longlong (*)(UDF_INIT *, UDF_ARGS *, uchar *, uchar *)) u_d->func;
    longlong tmpL=funcL(&initid, &f_args, &is_null, &error);
return tmpD + tmpL;
}
