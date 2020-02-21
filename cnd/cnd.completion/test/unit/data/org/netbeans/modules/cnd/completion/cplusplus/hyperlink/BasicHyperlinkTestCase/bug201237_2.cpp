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

class bug201237_2_A {
public:
    bool foo(){
        return true;
    }
    bug201237_2_A* bar(bool){
        return (bug201237_2_A*)0;
    }
    void bar(int) {
    }
};

int bug201237_2_main(int argc, char** argv) {
    bug201237_2_A f;
    f.bar( f.foo() ? f.foo() : f.foo() )->foo();
    f.bar(true)->foo();
    f.bar(10);
    return 0;
}
