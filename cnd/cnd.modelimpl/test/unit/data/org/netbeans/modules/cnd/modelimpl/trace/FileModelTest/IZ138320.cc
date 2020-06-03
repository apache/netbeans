class V {};

template <class T = const long *,
          class V = char * const,
          class D = int const *,
          class P = V const *,
          class R = V const &>
struct input_iterator_helper
{};
