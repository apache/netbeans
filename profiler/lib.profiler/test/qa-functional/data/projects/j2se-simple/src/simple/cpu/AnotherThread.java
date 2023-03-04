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

package simple.cpu;

class AnotherThread extends Thread {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        Region r = new Region();
        r.run1000();
        r.run2000();
        r.run1000();

        for (int i = 0; i < 10; i++) {
            r.run100();
        }

        r.run1000();
    }
}
