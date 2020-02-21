namespace DDD {
  class DDD {};
}

namespace CCC {
  class CCC {};
  void func(CCC);
  CCC operator+(CCC,CCC);
  DDD::DDD operator+(DDD::DDD,DDD::DDD);
}

template<class A>
class AAA 
{

  template<class B> 
   AAA(B b);
   
   AAA(); 
};

template<typename A>
template<typename B>
AAA<A>::AAA<B>(B b)
{  }

template<typename A>
AAA<A>::AAA() 
{ }
