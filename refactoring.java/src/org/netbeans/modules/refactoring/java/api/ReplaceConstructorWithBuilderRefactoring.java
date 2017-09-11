/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.api;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Replace Constructor with Builder Refactoring.
 * This refactoring creates a new Builder class and replaces all new class 
 * expressions with builder calls.
 * <br>
 * <br>
 * For instance it replaces:
 * <pre>
 * Test t = new Test("foo", 1);
 * </pre>
 * with builder pattern
 * <pre>
 * Test t = new TestBuilder().setA("foo").setB(1).createTest();
 * </pre>
 * 
 * @author Jan Becicka
 * @since 1.36
 */
public final class ReplaceConstructorWithBuilderRefactoring extends AbstractRefactoring {

    
    private String builderName;
    private List<Setter> setters;

    /**
     * Constructor accepts TreePathHandles representing constructor
     * @param constructor
     */
    public ReplaceConstructorWithBuilderRefactoring(@NonNull TreePathHandle constructor) {
        super(Lookups.singleton(constructor));
    }

    /**
     * Getter for builder name
     * @return fully qualified name of builder
     */
    public @NonNull String getBuilderName() {
        return builderName;
    }

    /**
     * 
     * @param builderName 
     */
    public void setBuilderName(@NonNull String builderName) {
        this.builderName = builderName;
    }

    /**
     * Getter for list of setters
     * @return
     */
    public @NonNull List<Setter> getSetters() {
        if (setters==null) {
            //never return null;
            return Collections.EMPTY_LIST;
        }
        return setters;
    }

    /**
     * setter for list of setters
     * @param setters
     */
    public void setSetters(@NonNull List<Setter> setters) {
        this.setters = setters;
    }

    /**
     * Setter represent one setter of Builder pattern
     */
    public static final class Setter {

        private final String name;
        private final String type;
        private final boolean optional;
        private final String defaultValue;
        private final String varName;

        /**
         * The only way how to create setter.
         * @param name the name of the setter. For instance "setA"
         * @param type the type of the setter. For instance "int"
         * @param defaultValue the default value. Might be null. For instance "1".
         * @param varName the name of the variable. For instance "a".
         * @param optional true if the setter is optional in case, that argument is the same as default value.
         */
        public Setter(
                @NonNull String name,
                @NonNull String type,
                @NullAllowed String defaultValue,
                @NonNull String varName,
                boolean optional) {
            this.name = name;
            this.type = type;
            this.optional = optional;
            this.defaultValue = defaultValue;
            this.varName = varName;
        }

        /**
         * Getter for setter name.
         * @return 
         */
        public @NonNull String getName() {
            return name;
        }

        /**
         * Getter for optional.
         * @return
         */
        public boolean isOptional() {
            return optional;
        }

        
        /**
         * Getter for type.
         * @return 
         */
        public @NonNull String getType() {
            return type;
        }

        /**
         * Getter for default value.
         * @return can return null
         */
        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * Getter for variable name.
         * @return 
         */
        public @NonNull String getVarName() {
            return varName;
        }

    }
    
}
