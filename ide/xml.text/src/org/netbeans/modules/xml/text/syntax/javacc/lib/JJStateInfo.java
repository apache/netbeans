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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

import org.netbeans.editor.Syntax;

/**
 * State info holder enriched by jj substates.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */

public final class JJStateInfo extends Syntax.BaseStateInfo {

    private int[] states;

    public void setSubStates(int[] states) {
        this.states = states;
    }

    public int[] getSubStates() {
        return states;
    }


    /** @return whether passed substates equals to this substates. */
    public int compareSubStates(int[] sub) {
        if (states == null) return Syntax.DIFFERENT_STATE;
        if (sub == null) return Syntax.DIFFERENT_STATE;
        if (states.length != sub.length) return Syntax.DIFFERENT_STATE;
        
        for (int i = states.length-1; i>=0; i--) {  //faster
            if (states[i] != sub[i]) return Syntax.DIFFERENT_STATE;
        }
        return Syntax.EQUAL_STATE;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(JJ[").append("S:" + getState()); // NOI18N
        buf.append("P:" + getPreScan()).append("subS:");  // NOI18N
        for (int i=0; i<states.length; i++) {
            buf.append(states[i] + ","); // NOI18N
        }
        buf.append("]JJ)"); // NOI18N
        return buf.toString();
    }
}
