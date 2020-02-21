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

class Cc {};
class Dd {}; //this must not show is completion dialog for ns::
int func() {}
char* buf;
double &number;

namespace ns { 
    using ::Cc;
    using ::Ee;
    using ::func;
    using ::buf;
    using ::number;
}

int main() {
    // completion test is performed here
    return 0;
}

namespace ns2 {
    //completion test is performed here
}

// this goes at the end of file to make completion task a bit harder
enum Ee { AA, BB };
