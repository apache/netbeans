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

class iz142674_c {
public:
    iz142674_c(): i(0), j(0) {}
    iz142674_c(const int i): i(i), j(i) {};
    ~iz142674_c() {};

    // setters
    iz142674_c& seti(const int i = 0) { this->i = i; return *this; };
    iz142674_c& setj(const int j = 0) { this->j = j; return *this; };
private:
    int i, j;
};

int iz142674_main(int argc, char** argv) {
    iz142674_c* c2 = new iz142674_c(0);
    bool b(false);
    int i(1);

    c2->seti(i).setj(i); // ok
    c2->seti((b?i:-1)).setj((b?i:-1)); // ok
    c2->seti((b?i:-1)).setj((b?-1:i)); // ok
    c2->seti((b?-1:i)).setj((b?i:-1)); // error, unresolved setj

    return EXIT_SUCCESS;
}