enum
{
    _PC_NAME_MAX,
#define	_PC_NAME_MAX			_PC_NAME_MAX
    _PC_PATH_MAX = _PC_NAME_MAX
#define	_PC_PATH_MAX			_PC_PATH_MAX
};

#if (4294967295) > 0
#define LONG_VALUE 1
#endif
 
#define bcopy(src,dst,len) memcpy((dst),(src),(len))
#define memcpy(dst,src,len) bcopy((src),(dst),(len)) 

#define key1(x) key2((x))
#define key2(x) key3((x))
#define key3(x) key1((x))

void foo() {
    memcpy(dst, buffer, 10);
    int i = key1(_PC_NAME_MAX) + LONG_VALUE;
}
