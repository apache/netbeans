template <class T>
class Realbase_for {
public:
  Realbase_for(const T& k);
private:
  T ker;
};

template<>
inline Realbase_for<long>::Realbase_for(const long& l) : ker(l) {
}
