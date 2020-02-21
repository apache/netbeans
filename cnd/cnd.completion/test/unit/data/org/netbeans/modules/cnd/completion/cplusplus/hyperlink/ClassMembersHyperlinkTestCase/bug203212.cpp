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

class bug203212_host;

namespace bug203212_nm01 {
    class wrapper {
    private:
        bug203212_host *h;
    public:
        wrapper(bug203212_host *_h) : h(_h) { }

        void execute01();
    };
};

class bug203212_host {
public:
    host() { }
    void action01() { }
    void action02() { }
} bug203212_the_host;

void
bug203212_nm01::wrapper::execute01() {
    h->action01();
    h->action02();
}

int bug203212_main(int argc, char** argv) {
    bug203212_nm01::wrapper(&bug203212_the_host).execute01();
    return 0;
}