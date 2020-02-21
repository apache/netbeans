namespace ns191336 {
    enum E1 {
        e0 = -1,
        e1
    };

    typedef struct
    _s_t {
        unsigned int os : 2;
        unsigned int arch : 2;
        unsigned int driver : 6;
    } s_t;

    typedef struct _table {
        s_t ctag;
        int help_id;
    } table;

    typedef struct _desc {
        s_t ctag;
        int help_id;
        table *chelp;
    } desc;

    desc
    suffix_table[] = {
        [e1] =
        {
            .ctag.driver = 0,
            .chelp = (table[])
            {
                { .help_id = 0, .ctag.driver = 0},
                { .help_id = 0, .ctag.driver = 0}
            }
        }
    };

}
