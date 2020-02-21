namespace function_returning_enum {
    enum _fre___codecvt_result{_fre___codecvt_ok};

    struct _fre__IO_codecvt
    {
      enum _fre___codecvt_result (*_fre___codecvt_do_out) (struct _fre__IO_codecvt *, int *);
    };

    enum _fre___codecvt_result (*_fre___codecvt_do_out_global) (struct _fre__IO_codecvt *, int *);
}