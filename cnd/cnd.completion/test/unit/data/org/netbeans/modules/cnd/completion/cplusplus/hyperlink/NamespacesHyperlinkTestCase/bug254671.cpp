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

#define DECLARE_5(name) int name##0; int name##1; int name##2; int name##3; int name##4;
#define DECLARE_25(name) DECLARE_5(name##0) DECLARE_5(name##1) DECLARE_5(name##2) DECLARE_5(name##3) DECLARE_5(name##4)
#define DECLARE_50(name) DECLARE_25(name##0) DECLARE_25(name##1)

DECLARE_50(var254671_);

struct dummy_forward254671 *var254671_51;

namespace bug254671 {
    typedef int type_254671;
}

using namespace bug254671;

void foo254671() {
    type_254671 a;
}

