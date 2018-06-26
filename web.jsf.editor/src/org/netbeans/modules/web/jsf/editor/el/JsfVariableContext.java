/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

        int comp = new Integer(getFrom()).compareTo(c.getFrom());
        if(comp != 0) {
            return comp;
        } else {
            return new Integer(getTo()).compareTo(c.getTo());
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode() + ": <" + getFrom() + "-" + getTo() + "> var='" +
                getVariableName() + "', value='" + getVariableValue() + "'"; //NOI18N
    }



}
