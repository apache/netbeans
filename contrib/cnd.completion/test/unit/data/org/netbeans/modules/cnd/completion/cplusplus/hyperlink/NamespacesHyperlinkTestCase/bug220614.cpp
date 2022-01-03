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

namespace {
    void
    bug220614_function1() {
        std::cout << "::function1()\n";
    }
}
namespace bug220614_code {
    void 
    bug220614_function1() {
        std::cout << "code::function1()\n";
    }
    int
    bug220614_function2() {
        bug220614_function1();
        ::bug220614_function1();
        return 0;
    }
}
int
bug220614_main(int argc, char** argv) {
    return bug220614_code::bug220614_function2();
}