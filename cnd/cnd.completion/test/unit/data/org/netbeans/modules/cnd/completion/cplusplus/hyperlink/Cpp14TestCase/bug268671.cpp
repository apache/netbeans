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

namespace bug268671 {
    template <typename T>
    T var268671;  
    
    struct BBB268671 {
        void mtd();
    };

    struct AAA268671 {   
        template <typename T>
        static T field;
    };      
     
    void foo() { 
        var268671<BBB268671>.mtd();
        AAA268671::field<BBB268671>.mtd();
    } 
}  