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
package org.netbeans.modules.el.lexer;

/**
 * Represents single state of the EL lexer.
 * @author ads
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELState {

    private int state;
    private int conditionalCount;

    public ELState(int state, int conditionalCount) {
        this.state = state;
        this.conditionalCount = conditionalCount;
    }

    public int getState() {
        return state;
    }

    public int getConditionalCount() {
        return conditionalCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ELState other = (ELState) obj;
        if (this.state != other.state) {
            return false;
        }
        if (this.conditionalCount != other.conditionalCount) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + this.state;
        hash = 13 * hash + this.conditionalCount;
        return hash;
    }

}
