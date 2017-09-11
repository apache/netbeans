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

package org.netbeans.modules.form;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.form.codestructure.CodeExpression;

/**
 * Design support for beans binding. Note that this is not an API for
 * a general binding support. It is more a hack that allows this module
 * not to depend on the beans binding library/support directly.
 *
 * @author Jan Stola, Tomas Pavek
 */
public interface BindingDesignSupport {

    /**
     * Returns binding group class.
     * 
     * @return binding group class.
     */
    Class getBindingGroupClass();

    /**
     * Returns binding validator class.
     * 
     * @return binding validator class.
     */
    Class getValidatorClass();

    /**
     * Returns binding converter class.
     * 
     * @return binding converter class.
     */
    Class getConverterClass();

    /**
     * Returns beans binding replication support.
     * 
     * @return beans binding replication support.
     */
    BindingVisualReplicator createReplicator();

    /**
     * Generates source code for the instantiation of the specified binding.
     * 
     * @param prop property for which the source code should be generated.
     * @param buf buffer into which the source code should be generated.
     * @param context context of the code generator.
     * @return variable used for the generated binding.
     */
    String generateBinding(BindingProperty prop, StringBuilder buf, CodeGeneratorContext context);
    
    /**
     * Makes sure that the Beans Binding library is on the classpath of the project.
     * 
     * @return {@code true} if the library was added on the classpath
     * returns {@code false} otherwise.
     */
    boolean updateProjectForBeansBinding();

    /**
     * Turns given string (usually dot-separated path) into EL expression
     * by adding <code>${</code> and <code>}</code> braces.
     * 
     * @param path string to transform into EL expression.
     * @return EL expression corresponding to the given path.
     */
    String elWrap(String path);

    /**
     * Removes <code>${</code> and <code>}</code> braces from a simple
     * EL expression. Non-simple expressions are left untouched.
     * 
     * @param expression expression to unwrap.
     * @return unwrapped expression or the given string
     * (if it is not a simple EL expression).
     */
    String unwrapSimpleExpression(String expression);

    /**
     * Determines whether the given string is simple EL expression. 
     * 
     * @param expression string to check.
     * @return <code>true</code> if the given string starts with
     * <code>${</code> and ends with <code>}</code>, returns <code>false</code>
     * otherwise.
     */
    public boolean isSimpleExpression(String expression);

    /**
     * Produces a title from the given camel case string. For example,
     * returns 'First Name' for 'firstName'.
     * 
     * @param title text to capitalize.
     * @return title from the given camel case string.
     */
    public String capitalize(String title);
    
    /**
     * Determines type of RAD component.
     *
     * @param comp RAD component whose type should be returned.
     * @return <code>TypeHelper</code> that corresponds to the type of the given component.
     */
    FormUtils.TypeHelper determineType(RADComponent comp);
    
    /**
     * Determines type of the binding described by the given component and source path.
     *
     * @param comp source of the binding.
     * @param sourcePath binding path from the source.
     * @return type of the binding.
     */
    FormUtils.TypeHelper determineType(RADComponent comp, String sourcePath);

    List<BindingDescriptor>[] getBindingDescriptors(RADComponent component);

    public List<BindingDescriptor>[] getBindingDescriptors(FormUtils.TypeHelper type);

    public List<BindingDescriptor> getAllBindingDescriptors(FormUtils.TypeHelper type);
    
    /**
     * Changes the binding between two components (affects only reference instances in the model).
     * 
     * @param oldBinding the old definition of the binding.
     * @param newBinding the new definition of the binding.
     */
    void changeBindingInModel(MetaBinding oldBinding, MetaBinding newBinding);

    /**
     * Beans binding replication support.
     */
    interface BindingVisualReplicator {
        /**
         * Creates binding according to given MetaBinding between given source and
         * target objects. The binding is registered, so it is automatically unbound
         * and removed when the MetaBinding is removed (or the source/target component).
         * 
         * @param bindingDef description of the binding
         * @param source binding source
         * @param target binding target
         * @param inModel determines whether we are creating binding in the model
         */
        void addBinding(MetaBinding bindingDef, Object source, Object target, boolean inModel);

        void establishUpdatedBindings(RADComponent metacomp, boolean recursive, Map map, boolean inModel);

        void establishOneOffBindings(RADComponent metacomp, boolean recursive, Map map);
    }

    /**
     * Code generation context (set of callbacks usefull during
     * code generation of beans binding code).
     */
    interface CodeGeneratorContext {

        String getBindingDescriptionVariable(Class descriptionType, StringBuilder buf, boolean create);

        String getExpressionJavaString(CodeExpression exp, String thisStr);
    }


}
