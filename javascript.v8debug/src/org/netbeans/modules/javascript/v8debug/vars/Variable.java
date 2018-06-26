/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.vars;

import java.util.Objects;
import org.netbeans.lib.v8debug.V8Scope;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public class Variable {
    
    public static enum Kind {
        ARGUMENT,
        LOCAL,
        PROPERTY,
        ARRAY_ELEMENT
    }
    
    private final Kind kind;
    private final String name;
    private final long ref;
    private V8Value value;
    private final V8Scope scope;
    private String valueLoadError;
    private boolean hasIncompleteValue;
    
    public Variable(Kind kind, String name, long ref, V8Value value, boolean incompleteValue) {
        this(kind, name, ref, value, incompleteValue, null);
    }
    
    public Variable(Kind kind, String name, long ref, V8Value value, boolean incompleteValue,
                    V8Scope scope) {
        this.kind = kind;
        this.name = name;
        this.ref = ref;
        this.value = value;
        this.scope = scope;
        this.hasIncompleteValue = incompleteValue;
    }
    
    public boolean hasIncompleteValue() {
        return hasIncompleteValue;
    }
    
    public Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public long getRef() {
        return ref;
    }

    public V8Value getValue() throws EvaluationError {
        if (valueLoadError != null) {
            throw new EvaluationError(valueLoadError);
        }
        return value;
    }
    
    void setValue(V8Value value) {
        this.value = value;
        this.hasIncompleteValue = false;
    }
    
    void setValueLoadError(String valueLoadError) {
        this.valueLoadError = valueLoadError;
    }
    
    /** Variable's scope or <code>null</code>. */
    public V8Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + (int) (this.ref ^ (this.ref >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variable other = (Variable) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.ref != other.ref) {
            return false;
        }
        return true;
    }
    
    // TODO: Attach referenced values?
}
