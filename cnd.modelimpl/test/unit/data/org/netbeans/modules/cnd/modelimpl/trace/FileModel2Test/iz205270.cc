namespace std {
template<class _Elem,
    class _Traits>
    class basic_ostream
        : virtual public basic_ios<_Elem, _Traits>
    {    // control insertions into a stream buffer
public:


     basic_ostream(_Myt&& _Right)
        {    // construct by moving _Right
        }

    _Myt&  operator=(_Myt&& _Right)
        {    // move from _Right
        return (*this);
        }

    void  swap(_Myt& _Right)
        {    // swap with _Right
        }
    };

template<class _Elem,
    class _Traits,
    class _Ty> inline
    basic_ostream<_Elem, _Traits>&
        operator<<(basic_ostream<_Elem, _Traits>&& _Ostr, _Ty _Val)
    {    // insert to rvalue stream
    return (_Ostr << _Val);
    }
 }
