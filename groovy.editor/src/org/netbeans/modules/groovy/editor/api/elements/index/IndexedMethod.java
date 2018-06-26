/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.groovy.editor.api.elements.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

/**
 * A class describing a Groovy method that is in "textual form" (signature, filename, etc.)
 * obtained from the code index.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public final class IndexedMethod extends IndexedElement implements MethodElement {

    private final List<MethodParameter> parameters;
    private final String returnType;

    public IndexedMethod(
            IndexResult result,
            String clz,
            String name,
            String returnType,
            List<MethodParameter> parameters,
            String attributes,
            int flags) {

        super(result, clz, name, attributes, flags);
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return getSignature();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReturnType() {
        return returnType;
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(in);
        sb.append("#"); // NOI18N
        sb.append(name);

        if (!parameters.isEmpty()) {
            sb.append("("); // NOI18N
            for (MethodParameter param : parameters) {
                sb.append(param.getFqnType());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")"); // NOI18N
        }

        return sb.toString();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return parameters;
    }

    @Override
    public List<String> getParameterTypes() {
        List<String> paramTypes = new ArrayList<>();

        for (MethodParameter parameter : getParameters()) {
            paramTypes.add(parameter.getType());
        }
        return paramTypes;
    }

    @Override
    public ElementKind getKind() {
        if (((name == null) && signature.startsWith("initialize(")) || // NOI18N
                ((name != null) && name.equals("initialize"))) { // NOI18N

            return ElementKind.CONSTRUCTOR;
        } else {
            return ElementKind.METHOD;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.in);
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.signature);
        hash = 47 * hash + Objects.hashCode(this.modifiers);
        hash = 47 * hash + Objects.hashCode(this.parameters);
        hash = 47 * hash + Objects.hashCode(this.returnType);
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
        final IndexedMethod other = (IndexedMethod) obj;
        if (!Objects.equals(this.in, other.in)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.signature, other.signature)) {
            return false;
        }
        if (!Objects.equals(this.modifiers, other.modifiers)) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(this.returnType, other.returnType)) {
            return false;
        }
        return true;
    }

}
