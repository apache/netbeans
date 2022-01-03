namespace bug252427 {
    typedef int td_check252427;
    typedef const int td_const_check252427;
    typedef int * td_check_ptr252427;
    typedef const int * td_const_check_ptr252427;
    using alias_check252427 = int;
    using alias_const_check252427 = const int;
    using alias_check_ptr252427 = int *;
    using alias_const_check_ptr252427 = const int *;

    struct Container252427 {
        typedef int td_check252427;
        typedef const int td_const_check252427;
        typedef int * td_check_ptr252427;
        typedef const int * td_const_check_ptr252427;
        using alias_check252427 = int;
        using alias_const_check252427 = const int;
        using alias_check_ptr252427 = int *;
        using alias_const_check_ptr252427 = const int *;    
    };

    int fun252427() {
        typedef int td_check252427;
        typedef const int td_const_check252427;
        typedef int * td_check_ptr252427;
        typedef const int * td_const_check_ptr252427;
        using alias_check252427 = int;
        using alias_const_check252427 = const int;
        using alias_check_ptr252427 = int *;
        using alias_const_check_ptr252427 = const int *;
    }

    using alias_definition252427 = struct {
        int dummy252427;
    } *;
}