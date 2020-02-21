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

namespace bug247031 {
    struct AAA247031 {
        const int value;
        AAA247031(int val) : value(val) {};
        int getFromAAA() {return value;};
    };

    struct BBB247031 {
        const int value;
        BBB247031(int val) : value(val) {};
        int getFromBBB() {return value;};
    };

    namespace myns247031 {
        struct CCC247031 {
            CCC247031(int val);
            int getFromCCC();
        };
    }

    template <typename T>
    struct EEE247031 {
        EEE247031();
        EEE247031(T params...);
        T get();
    };

    AAA247031 func247031(AAA247031 a, int p) {
        return a;
    }

    BBB247031 func247031(BBB247031 b, float p) {
        return b;
    }

    AAA247031 funcWithParams247031(AAA247031 a, int ip1, BBB247031 b, int ip2) {
        return {ip1 + ip2};
    } 

    AAA247031 boo247031() {
        int x{};
        auto var = AAA247031{x};
        auto retVal = AAA247031{x}.getFromAAA();
        var.getFromAAA();
        func247031({x}, 1).getFromAAA(); 
        func247031({x}, 1.0f).getFromBBB();
        funcWithParams247031({x + 3}, x, {AAA247031{1}.getFromAAA() + x}, 5).getFromAAA();
        AAA247031{x}.getFromAAA(); 
        (AAA247031){x}.getFromAAA(); 
        auto scopedVar = myns247031::CCC247031{x};
        scopedVar.getFromCCC();
        myns247031::CCC247031{x}.getFromCCC();
        auto tpl = EEE247031<AAA247031>{};
        tpl.get().getFromAAA();
        EEE247031<BBB247031>{}.get().getFromBBB();
        EEE247031<const char *>{"one", "two"}.get();
        return AAA247031{x}.getFromAAA(); 
    }
}
