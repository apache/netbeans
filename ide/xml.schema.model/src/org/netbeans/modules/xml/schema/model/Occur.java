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

package org.netbeans.modules.xml.schema.model;

/**
 *
 * @author nn136682
 */
public interface Occur {

    public static enum ZeroOne implements Occur {
        ZERO, ONE;

        public static ZeroOne valueOfNumeric(String owner, String s) {
            int v = Integer.valueOf(s).intValue();
            if (v < 0 || v > 1) {
                throw new IllegalArgumentException("'" + owner + "' can only has value 0 or 1");
            }
            return v == 0 ? ZeroOne.ZERO : ZeroOne.ONE;
        }

        public String toString() {
            return this == ZeroOne.ZERO ? "0" : "1";
        }
    }
}
