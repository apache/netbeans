/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.nodes;

/** Test whether Children.Keys inherited all functionality from Children.Array.
 * @author Jesse Glick
 */
public class ChildrenKeysAsLazyArrayTest extends ChildrenArrayTest {
    public ChildrenKeysAsLazyArrayTest (String s) {
        super (s);
    }

    @Override
    protected Children.Array createChildren () {
        return new Children.Keys (true) {
            protected Node[] createNodes (Object obj) {
                fail ("This should not get called as we are using just Children.Array functions of the keys");
                return null;
            }
        };
    }
    
}
