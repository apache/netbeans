#define TEMPLATE template

template <typename TT>
struct B {
    typename TT::TEMPLATE unique<TT> x;
};

int main() {
    return 0;
}