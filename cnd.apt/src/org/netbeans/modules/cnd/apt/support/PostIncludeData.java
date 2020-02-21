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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.apt.support.api.PPMacroMap.State;

/**
 * entry to store post include data
 */
public final class PostIncludeData {

    private final APTMacroMap.State postIncludeMacroState;
    private volatile int[]          deadBlocks;

    /*package*/ PostIncludeData() {
        // clean entry
        this.postIncludeMacroState = null;
        this.deadBlocks = null;
    }

    public PostIncludeData(State postIncludeMacroState, int[] deadBlocks) {
        this.postIncludeMacroState = postIncludeMacroState;
        this.deadBlocks = deadBlocks;
    }

    public State getPostIncludeMacroState() {
        return postIncludeMacroState;
    }

    public boolean hasPostIncludeMacroState() {
        return postIncludeMacroState != null;
    }

    public int[] getDeadBlocks() {
        // TODO: return copy?
        return deadBlocks;
    }

    public void setDeadBlocks(int[] deadBlocks) {
        // TODO: make a copy?
        this.deadBlocks = deadBlocks;
    }

    public boolean hasDeadBlocks() {
        return deadBlocks != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (deadBlocks == null) {
            sb.append("null"); // NOI18N
        } else {
            sb.append("[");//NOI18N
            for (int i = 0; i < deadBlocks.length; i += 2) {
                if (i > 0) {
                    sb.append("][");//NOI18N
                }
                sb.append(deadBlocks[i]);
                sb.append("-");//NOI18N
                sb.append(deadBlocks[i + 1]);
            }
            sb.append("]");//NOI18N
        }
        sb.append("\n state {").append(postIncludeMacroState).append("}"); // NOI18N
        return sb.toString();
    }

}
