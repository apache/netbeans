
// IZ#154349: wrongly flagged errors for destructor during template specialization

template<class T> class aptr {
    inline ~aptr() {
      //Do some default action
    }
};

template<> aptr<int>::~aptr() {
    //Do something more specialized
}
