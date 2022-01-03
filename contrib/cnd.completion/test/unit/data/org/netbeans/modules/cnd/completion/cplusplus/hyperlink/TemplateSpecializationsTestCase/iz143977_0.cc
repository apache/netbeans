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

//#include <stdio.h>

namespace iz143977_0 {
    class Object {
    public:
        const char* toString() const;
        int hashCode() const;
    };

    template<class K, class V = K> struct Pair {
        K first;
        V second;
    };

    int hash(Pair<Object> pair) {
        int hash = 7;
        hash = 79 * hash + pair.first.hashCode(); // hashCode should be resolved 
        hash = 79 * hash + pair.second.hashCode(); // hashCode should be resolved 
        return hash;
    }
    
    template<class K, class V = Object> class Map {
    public:
        V& operator [] (K& key);
    };

    Object printValue(Map<Object> m, Object key) {
        printf("value = %s\n", m[key].toString()); // toString should be resolved
        return m[key];
    }
}
