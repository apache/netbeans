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

class iz104945_A {
public:
    int method();
    int method(int*);
    int method(const int* const);
    int method(iz104945_A*);
    int method(const iz104945_A* const);
};

int iz104945_A::method() {
    return 0;
}

int iz104945_A::method(int* a) {
    return 0;
}

int iz104945_A::method(const int* const a) {
    return 0;
}

int iz104945_A::method(iz104945_A* a) {
    return 0;
}

int iz104945_A::method(const iz104945_A* const a) {
    return 0;
}