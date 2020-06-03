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

namespace bug249463 { 
    struct AAA249463 {
        int foo() const;
    };

    AAA249463 someStr249463;

    namespace zoo249463 {
        auto &test2 = someStr249463;
    }
    
    struct roo249463 {
        static constexpr auto s_field = AAA249463();
    };
    
    void someFunc249463() {
      zoo249463::test2.foo();
      roo249463::s_field.foo();
    }
}