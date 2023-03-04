# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

Tests created in this package are here just because there is some problem with Classpath when multiple tests are run from
the same file (some classpath information are lost after the first test and as a result other tests dependent on this
classpath information will fail). Because at the moment I have no clue where is the problem, I decided to created temporary
package for these few tests and create new file per test. I will fix the original problem later with better groovy base knowledge.

In each of those newly created tests there is a comment containing a reference to the original file where the test should be located.