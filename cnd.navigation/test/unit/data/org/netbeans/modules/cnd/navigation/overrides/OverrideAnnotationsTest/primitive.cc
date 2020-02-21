struct PrimitiveAncestor {
    PrimitiveAncestor() {}
    PrimitiveAncestor(int);
    virtual void v_func();
    virtual void v_inline_func() {
    }
    void nv_func();
    void nv_inline_func() {
    }
};

class PrimitiveDescendant : PrimitiveAncestor {
    PrimitiveDescendant() {}
    PrimitiveDescendant(char);
    virtual void v_func();
    virtual void v_inline_func() {
    }
    void nv_func();
    void nv_inline_func() {
    }
};

PrimitiveAncestor::PrimitiveAncestor(int) {
}

void PrimitiveAncestor::v_func() {
}

void PrimitiveAncestor::nv_func() {
}

PrimitiveDescendant::PrimitiveDescendant(char) {
}

void PrimitiveDescendant::v_func() {
}

void PrimitiveDescendant::nv_func() {
}
