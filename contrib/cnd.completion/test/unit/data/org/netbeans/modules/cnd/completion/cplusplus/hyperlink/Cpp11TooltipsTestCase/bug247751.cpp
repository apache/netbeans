/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

namespace bug247751 {
    struct AAA247751 {
        int aaa();
    };

    struct BBB247751 {
        int bbb();
    };

    struct CCC247751 {
        int ccc();
    };

    template <typename T1>
    struct Wrapper1_247751 {
        typedef T1& type;
    };

    template <typename T2>
    struct Wrapper2_247751 {
        typedef typename Wrapper1_247751<T2>::type type;
    };

    namespace std247751 {
        ///////////////////////////////////////
        // String    
        template <typename Char>
        struct basic_string247751 {
            typedef Char* iterator;
            iterator begin();
        };

        typedef basic_string247751<char> string247751;

        ///////////////////////////////////////
        // Tuple
        template <typename...Elements> 
        struct tuple247751 {};

        template< int I, class T >
        struct tuple_element247751;

        // recursive case
        template< int __i, class Head, class... Tail >
        struct tuple_element247751<__i, tuple247751<Head, Tail...>>
            : tuple_element247751<__i-1, tuple247751<Tail...>> { };

        // base case
        template< class Head, class... Tail >
        struct tuple_element247751<0, tuple247751<Head, Tail...>> {
           typedef Head type;
        };    

        template <int Ind, class... Elements>
        typename Wrapper1_247751<
            typename tuple_element247751<Ind, tuple247751<Elements...>>::type
        >::type
        get247751(tuple247751<Elements...> &tpl);

        template <typename...Elements>
        tuple247751<typename Wrapper2_247751<Elements>::type...> 
        make_tuple247751(Elements&&...args);

        ///////////////////////////////////////
        // Map  
        template <typename Key, typename Value> 
        struct map247751 {
            typedef Key key_type;
            typedef Value mapped_type;
            mapped_type& operator[](const key_type& key);
        };
    }

    int main247751() {
        auto var = std247751::make_tuple247751(AAA247751(), BBB247751(), CCC247751());
        auto elem0 = std247751::get247751<0>(var);
        auto elem1 = std247751::get247751<1>(var);
        auto elem2 = std247751::get247751<2>(var);
        std247751::map247751<int, int*> mapping;
        auto mapElem = mapping[3];
        std247751::string247751 str;
        auto stringVar = str;
        //auto stringIter = str.begin();
        return 0;
    }
}
