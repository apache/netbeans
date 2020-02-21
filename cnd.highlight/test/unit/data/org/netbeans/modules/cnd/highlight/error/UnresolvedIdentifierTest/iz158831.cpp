template <class T> struct iz158831_C {
    int foo() {
        typedef typename T::stored_vertex stored_vertex;
        stored_vertex* v = new stored_vertex(0);
        v.iiiiiiiiiiiiiiii; // should be warning
    }
};