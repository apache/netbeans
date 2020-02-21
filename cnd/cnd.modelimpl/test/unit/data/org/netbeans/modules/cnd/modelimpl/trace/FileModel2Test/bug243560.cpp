namespace bug243560 {
    
    // =====================================
    template<typename T>
    struct X0243560_1 {
      union Inner243560_1 { };
    };

    template union X0243560_1<float>::Inner243560_1;
    
    
    // =====================================
    template<typename T, typename U>
    struct X2243560_2 {
      struct Inner243560_2 {
        T member1;
        U member2;
      };

    };

    template struct X2243560_2<int, float>::Inner243560_2;
    
    //=======================================
    template<typename T>
    struct X0243560_3 {
      struct Inner243560_3 {};
    };

    typedef X0243560_3<int> XInt243560_3;

    template struct XInt243560_3::Inner243560_3; // expected-warning{{template-id}}    
    
    //=======================================
    namespace has_inline_namespaces {
      inline namespace inner {
        template<class T> 
        struct X0243560_4 {
          struct MemberClass243560_4 {};
        };
      }
      struct X1243560_4 {};
      struct X2243560_4 {};

      template struct X0243560_4<X2243560_4>::MemberClass243560_4;
    }

    struct X4243560_4;

    template struct has_inline_namespaces::X0243560_4<X4243560_4>::MemberClass243560_4;    
    
    
    //========================================
    template<typename T>
    struct X0243560_5 {
      struct Inner243560_5 {};
    };

    template struct X0243560_5<long>::Inner243560_5;    
} 