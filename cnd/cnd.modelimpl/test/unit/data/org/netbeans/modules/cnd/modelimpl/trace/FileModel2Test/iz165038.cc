struct _GMutex{
};

typedef  struct _GStaticMutex
{
  struct _GMutex *runtime_mutex;
  union {
    char   pad[24];
    double dummy_double;
    void  *dummy_pointer;
    long   dummy_long;
  } aligned_pad_u;
} GStaticMutex;


static GStaticMutex ( g__GModule_lock ) = { ((void*) 0) , { { 0,0,0,0,0,0,88,77,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0} } };
