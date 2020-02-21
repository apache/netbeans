template<typename T> inline T bug186638_foo()
{
    static_cast<T>(0)->i;
    dynamic_cast<T>(0)->i;
    reinterpret_cast<T>(0)->i;
    const_cast<T>(0)->i;
}