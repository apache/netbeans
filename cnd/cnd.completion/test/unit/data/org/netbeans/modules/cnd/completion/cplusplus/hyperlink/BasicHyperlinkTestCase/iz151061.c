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

struct iz151061BBB {
    int field;
};

struct iz151061BBB a[] = {
    { .field = 1}, // unresolved
    { .field = 2}, // unresolved
};

const struct iz151061BBB* iz151061foo() {

    struct iz151061AAA {
        int field1;
    };

    struct iz151061AAA a[] = {
        { .field1 = 1}, // unresolved
        { .field1 = 2}, // unresolved
    };

    for (const struct iz151061AAA* res; ;) {
        res->field1;
    }
    iz151061foo()->field;
}
