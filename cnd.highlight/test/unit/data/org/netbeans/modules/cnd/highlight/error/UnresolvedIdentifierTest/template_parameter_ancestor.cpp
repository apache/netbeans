class empty_class {};

// In the class below they all should be errors
template<typename T> class inherits_no_parameter  {

    static inherits_no_parameter get_instance();

    empty_class& get_empty();

    void inline_use_anc_mem(inherits_no_parameter x, empty_class e) {
        this->ancestor_member(); // error
        ancestor_member(); // error
        x.ancestor_member(); // error
        inherits_no_parameter::get_instance().ancestor_member(); // error
        inherits_no_parameter array[2];
        array[0].ancestor_member(); // error
        e.inexistent_method(); // error
        ::inexistent_function(); // error
        int x = ::inexistent_var; // error
        get_empty().inexistent_method(); // error
    }
};

template<typename T> struct template_parameter_descendant : public T {

    static template_parameter_descendant get_instance();

    empty_class& get_empty();

    void inline_use_anc_mem(template_parameter_descendant x, empty_class e) {
        this->ancestor_member111(); // warning
        ancestor_member(); // warning
        x.ancestor_member(); // warning
        template_parameter_descendant::get_instance().ancestor_member(); // warning
        template_parameter_descendant array[2];
        array[0].ancestor_member(); // warning
        e.inexistent_method(); // error
        ::inexistent_function(); // error
        int x = ::inexistent_var; // error
        get_empty().inexistent_method(); // error
    }
    void use_anc_mem(template_parameter_descendant x, empty_class e);
};

void template_parameter_descendant::use_anc_mem(template_parameter_descendant x, empty_class e) {
    this->ancestor_member(); // warning
    ancestor_member(); // warning
    x.ancestor_member(); // warning
    template_parameter_descendant::get_instance().ancestor_member(); // warning
    template_parameter_descendant array[2];
    array[0].ancestor_member(); // warning
    e.inexistent_method(); // error
    ::inexistent_function(); // error
    int x = ::inexistent_var; // error
    get_empty().inexistent_method(); // error
}

template<typename T> struct descendant_2 : template_parameter_descendant<T> {
    void inline_use_anc_mem(descendant_2 x, empty_class e) {
        // this->ancestor_member(); // warning
        ancestor_member(); // warning
        x.ancestor_member(); // warning
        descendant_2 array[2];
        array[0].ancestor_member(); // warning
        e.inexistent_method(); // error
        ::inexistent_function(); // error
        int x = ::inexistent_var; // error
        get_empty().inexistent_method(); // error
    }
};

template<typename T> struct descendant_3 : descendant_2<T> {
    void inline_use_anc_mem(descendant_3 x, empty_class e) {
        // this->ancestor_member(); // warning
        ancestor_member(); // warning
        x.ancestor_member(); // warning
        descendant_3 array[2];
        array[0].ancestor_member(); // warning
        e.inexistent_method(); // error
        ::inexistent_function(); // error
        int x = ::inexistent_var; // error
        get_empty().inexistent_method(); // error
    }
};

struct an_ancestor {
    void ancestor_member();
};

void usage() {
    template_parameter_descendant<an_ancestor> inst;
    inst.ancestor_member();  // resolved ok
    inst.inline_use_anc_mem(inst);
    inst.inexistent_method(); // error
    template_parameter_descendant::get_instance().ancestor_member(); // warning
}
