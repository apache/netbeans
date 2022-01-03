
struct env210983 {
    int max;
    int indent;
    env210983() { max = 0; indent = 4; }
    ~env210983() {}
};

static int boo210983(env210983* old) {
    env210983 e;
    e.indent;
    old->max;
    return 0;
}
