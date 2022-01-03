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

namespace bug242719 {
    
    struct BBB242719;

    struct AAA242719 {
        int foo(); 

        BBB242719& operator + (BBB242719 &other) {
            return other;
        }    
    };

    struct BBB242719 {
        int boo();

        AAA242719& operator + (AAA242719 &other) {
            return other;
        }    
    };

    int main(int argc, char** argv) {
        AAA242719 a;
        BBB242719 b;
        (b + a).foo(); // foo is unresolved, it suggests boo here
        (a + b).boo();
        return 0;
    } 
}