namespace iz148236_std {

    template<class charT, class traits> class basic_ios {
        inline operator void*() const;
    };

    template<class charT, class traits> inline basic_ios<charT, traits>::operator void*() const {
        return (void*) 0;
    }

    template <class stateT> class fpos {
    public:
        inline operator long() const;
    };
    template <class stateT> inline fpos<stateT>::operator long() const {
        return (long) 0;
    }
}