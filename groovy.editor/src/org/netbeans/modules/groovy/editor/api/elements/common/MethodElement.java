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
package org.netbeans.modules.groovy.editor.api.elements.common;

import java.util.List;
import java.util.Objects;

/**
 * General method element, either from an AST or from an index.
 *
 * @author Tor Norbye
 * @author Martin Janicek
 */
public interface MethodElement {

    /**
     * Gets the list of the {@link MethodParameter}s.
     *
     * @return list of the {@link MethodParameter}s.
     */
    List<MethodParameter> getParameters();

    /**
     * Gets only the parameter types of the method.
     *
     * @return parameter types of the method
     */
    List<String> getParameterTypes();

    /**
     * Gets the return type of the method.
     *
     * @return return type of the method
     */
    String getReturnType();


    /**
     * Information about method parameter such as parameter type and name.
     */
    public static final class MethodParameter {

        private final String fqnType;
        private final String type;
        private final String name;

        public MethodParameter(String fqnType, String type) {
            this(fqnType, type, null);
        }

        public MethodParameter(String fqnType, String type, String name) {
            this.fqnType = fqnType;
            this.type = type;
            this.name = name;
        }

        public String getFqnType() {
            return fqnType;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return type + " " + name; // NOI18N
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 13 * hash + Objects.hashCode(this.fqnType);
            hash = 13 * hash + Objects.hashCode(this.type);
            hash = 13 * hash + Objects.hashCode(this.name);
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
            final MethodParameter other = (MethodParameter) obj;
            if (!Objects.equals(this.fqnType, other.fqnType)) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }
    }
}
