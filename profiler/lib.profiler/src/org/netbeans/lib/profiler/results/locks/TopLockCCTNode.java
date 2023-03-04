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
package org.netbeans.lib.profiler.results.locks;

import org.netbeans.lib.profiler.results.CCTNode;

/**
 *
 * @author Tomas Hurka
 */
class TopLockCCTNode extends LockCCTNode {

    private long totalTime;
    private int totalWaits;

    TopLockCCTNode() {
        super(null);
    }

    @Override
    public String getNodeName() {
        return "Invisible root node";  //NOI18N
    }

    @Override
    public long getTime() {
        if (totalTime == 0) {
            for (CCTNode ch : getChildren()) {
                if (ch instanceof LockCCTNode) {
                    totalTime += ((LockCCTNode) ch).getTime();
                }
            }
        }
        return totalTime;
    }

    @Override
    public int hashCode() {
        return TopLockCCTNode.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TopLockCCTNode;
    }

    @Override
    public double getTimeInPerCent() {
        return 100;
    }

    @Override
    public long getWaits() {
        if (totalWaits == 0) {
            for (CCTNode ch : getChildren()) {
                if (ch instanceof LockCCTNode) {
                    totalWaits += ((LockCCTNode) ch).getWaits();
                }
            }
        }
        return totalWaits;
    }
    
}
