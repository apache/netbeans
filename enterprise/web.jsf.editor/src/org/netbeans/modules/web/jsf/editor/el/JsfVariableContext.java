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

package org.netbeans.modules.web.jsf.editor.el;

/**
 * Note: Uses embedded offsets of the html snapshot!
 *
 * @author marekfukala
 */
public class JsfVariableContext implements Comparable {

    protected int from;
    protected int to;
    protected String variableName;
    protected String variableValue;
    protected String resolvedType;

    JsfVariableContext(int from, int to, String variableName, String variableType) {
        this.from = from;
        this.to = to;
        this.variableName = variableName;
        this.variableValue = variableType;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public String getVariableName() {
        return variableName;
    }

    /** @return end offset of the variable context. The offset is the html parser result embedded offset. */
    public int getTo() {
        return to;
    }

    /** @return start offset of the variable context. The offset is the html parser result embedded offset. */
    public int getFrom() {
        return from;
    }

    public String getResolvedExpression() {
        return resolvedType == null ? null : new StringBuilder().append("#{").append(resolvedType).append('}').toString();
    }

    void setResolvedType(String type) {
        this.resolvedType = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JsfVariableContext other = (JsfVariableContext) obj;
        if (this.from != other.from) {
            return false;
        }
        if (this.to != other.to) {
            return false;
        }
        if ((this.variableName == null) ? (other.variableName != null) : !this.variableName.equals(other.variableName)) {
            return false;
        }
        if ((this.variableValue == null) ? (other.variableValue != null) : !this.variableValue.equals(other.variableValue)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.from;
        hash = 23 * hash + this.to;
        hash = 23 * hash + (this.variableName != null ? this.variableName.hashCode() : 0);
        hash = 23 * hash + (this.variableValue != null ? this.variableValue.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        final JsfVariableContext c = (JsfVariableContext)o;

        int comp = Integer.valueOf(getFrom()).compareTo(c.getFrom());
        if (comp != 0) {
            return comp;
        } else {
            return Integer.valueOf(getTo()).compareTo(c.getTo());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode() + ": <" + getFrom() + "-" + getTo() + "> var='" +
                getVariableName() + "', value='" + getVariableValue() + "'"; //NOI18N
    }



}
