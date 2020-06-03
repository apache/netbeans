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

template<typename T1, typename T2> class subrule_list {
};

template<int T1, typename T2, typename T3> class subrule_parser {
};

template <int N, typename ListT>
struct get_subrule {
    typedef typename get_subrule<N, typename ListT::rest_t>::type type;
};

template <typename ParserT, typename ScannerT>
struct parser_result
{
    //typedef typename parser_type::template result<ScannerT>::type type;
};

template <int ID, typename ScannerT, typename ContextResultT>
struct get_subrule_result
{
            typedef typename
                get_subrule<ID, typename ScannerT::list_t>::type
            parser_t;

            typedef typename parser_result<parser_t, ScannerT>::type
            def_result_t;
};
