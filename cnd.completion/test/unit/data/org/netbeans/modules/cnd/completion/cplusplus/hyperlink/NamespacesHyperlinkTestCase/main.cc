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

#include "file.h"

int main(int argc, char** argv) {
     // here test text is inserted
    S1::foo();
    S1::var1 = 14;
    S1::S2::boo();
    S1::S2::var2 = 105;
    return 0;
}

void usingNS1() {    
    using namespace S1;
    var1 = 10;
    foo();
    clsS1 c1;
    c1.clsS1pubFun();
}

void usingNS1S2() {
    using namespace S1::S2;
    var2 = 10;
    boo();
    clsS2 c2;
    c2.clsS2pubFun();
}

void usingDirectivesS1() {
    using S1::clsS1;
    clsS1 c1;
    using S1::var1;
    var1 = 10;
    using S1::foo;
    foo();
}

void usingDirectivesS1S2() {
    using S1::S2::clsS2;
    clsS2 c2;
    using S1::S2::var2;
    var2 = 10;
    using S1::S2::boo;
    boo();
}

void usingNS2() {
    using namespace S1;
    using namespace S2;
    
    var2 = 10;
    boo();
    clsS2 c2;
    c2.clsS2pubFun();    
}

void usingDirectivesS2() {
    using namespace S1;
    myCout = 10;
    using S2::clsS2;
    clsS2 c2;
    using S2::var2;
    var2 = 10;
    using S2::boo;
    boo();
}

void usingCout() {
    S1::myCout;
    using S1::myCout;
    myCout;
}

void usingS1AccessNestedTypesOfTemplatedClass {
    S1::myType::reference ref1;
    using namespace S1;
    myType::reference ref2;
}

// IZ#144982: std class members are not resolved in litesql
namespace XXX {
    struct string {
        int size();
    };
}

namespace YYY {
    using namespace XXX;
}

namespace YYY {
    int foo() {
        string s;
        s.size();
    }
}

namespace ZZZ {
    using namespace YYY;
    int bar() {
        string s;
        s.size();
    }
}
