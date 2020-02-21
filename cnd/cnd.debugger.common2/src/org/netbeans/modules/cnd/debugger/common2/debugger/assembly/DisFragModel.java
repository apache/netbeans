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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

public interface DisFragModel extends Iterable<DisFragModel.Line> {
    static interface Listener {
        public void fragUpdated();
    }

    public void addListener(Listener listener);
    public void removeListener(Listener listener);
    public void clear() ;
    public Line getItem(int i);
    public int size();
    
    static class Line implements Disassembly.DisLine {
        private final String address;
        private final String instruction;
        private int idx;

        public Line(String address, String instruction) {
            this.address = address;
            this.instruction = instruction;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public int getIdx() {
            return idx;
        }

        @Override
        public void setIdx(int idx) {
            this.idx = idx;
        }

        @Override
        public String toString() {
            return "   " + address + instruction + "\n"; // NOI18N
        }
    }
}
