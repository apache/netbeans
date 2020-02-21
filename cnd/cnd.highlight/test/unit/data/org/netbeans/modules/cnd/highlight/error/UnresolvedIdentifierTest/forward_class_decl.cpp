namespace fwd_ns {

    class fwd;

    typedef fwd* PF;

    class fwd_user {
        fwd* prev;
        fwd* next;
        PF one_more;
        PF* yet_one_more;
        void foo(PF p1, fwd *p2);
    };

    template<typename T> class ImplT {
    };

    typedef ImplT<fwd> ImplFwd;

    template<typename T> class RimplT {
    };

    typedef RimplT<ImplT<fwd> > RimplTFwd;

}

using fwd_ns::fwd;

fwd* F;

class fwd_user_2 {
    fwd_ns::fwd *prev;
    fwd_ns::fwd *next;
};
    

struct Outer {
    struct Inner;
};

struct Outer::Inner {
     int x;
};


class Container
{
public:
  struct ForwardStruct;

  bool do_cast(const Container* __dst_type,
                     ForwardStruct & __result) const;
};

struct Container::ForwardStruct
{
  int whole_details;
  int foo();
  ForwardStruct (int details_) : whole_details (details_) { }
};

Container::ForwardStruct result_global(0);

bool Container::do_cast (const Container *dst_type,
                      ForwardStruct & result) const
{
  ForwardStruct result2 (result.whole_details);
  result_global.foo();
  result2.foo();
  result.foo();
  return true;
}
