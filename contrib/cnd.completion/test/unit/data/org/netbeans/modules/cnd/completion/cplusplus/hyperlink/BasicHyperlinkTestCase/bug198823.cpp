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

class bug198823_A {
public:
    int accept(int i) {
        return i;
    }
};

class bug198823_tcp
{
public:
  /// The type of a TCP endpoint.
  typedef basic_endpoint<bug198823_tcp> endpoint;
};

int bug198823_main(int argc, char** argv) {
    bug198823_A a(argc, bug198823_tcp::endpoint(tcp::v4(), port));
    a.accept(1);

    return 0;
}