namespace std {
    namespace tr1 {
        /// integral_constant
        template<typename _Tp, _Tp __v>
        struct integral_constant
        {
          static const _Tp                      value = __v;
          typedef _Tp                           value_type;
          typedef integral_constant<_Tp, __v>   type;
        };
  
        /// typedef for true_type
        typedef integral_constant<bool, true>     true_type;

        /// typedef for false_type
        typedef integral_constant<bool, false>    false_type;

        // is_function
        template<typename>
        struct is_function
        : public false_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes...)>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes......)>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes...) const>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes......) const>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes...) volatile>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes......) volatile>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes...) const volatile>
        : public true_type {
        };

        template<typename _Res, typename... _ArgTypes>
        struct is_function<_Res(_ArgTypes......) const volatile>
        : public true_type {
        };

        /// alignment_of
        template<typename _Tp> 
        struct alignment_of : public integral_constant<std::size_t, __alignof__(_Tp)> {
        };
    }
}
