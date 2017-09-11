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

package org.netbeans.lib.editor.codetemplates.spi;

import java.util.Collection;
import java.util.Map;
import org.netbeans.lib.editor.codetemplates.CodeTemplateParameterImpl;

/**
 * Code template parameter describes parsed parameter in the template's text.
 * <br>
 * A first occurrence of the parameter in the parametrized text of the code template
 * define a master parameter. All other occurrences of the parameter
 * with the same name will become slave parameters.
 * <br/>
 * The value of the master parameter
 * will be used for the slaves as well automatically and document modifications
 * to master parameter's value will be propagated to slaves as well.
 *
 * <p>
 * Master parameters can have additional hints in the form
 * <pre>
 * ${param hint=value [hint2=value2] ... }
 * ${param hint="string-literal" ... }
 * ${param hint ... }
 * </pre>
 * The hints give additional specification of what the parameter's value should be.
 * <br/>
 * The slave parameters inherit their value from their master so it has no sense
 * to define any hints for slave parameters.
 * <br/>
 * The hints without explicit <code>=value</code> are assigned with string value "true".
 * 
 * @author Miloslav Metelka
 */
public final class CodeTemplateParameter {
    
    /**
     * Name of the parameter corresponding to the caret position parameter.
     */
    public static final String CURSOR_PARAMETER_NAME = "cursor"; // NOI18N
    
    public static final String COMPLETION_INVOKE_HINT_NAME = "completionInvoke"; // NOI18N

    public static final String SELECTION_PARAMETER_NAME = "selection"; // NOI18N

    public static final String NO_FORMAT_PARAMETER_NAME = "no-format"; // NOI18N

    public static final String NO_INDENT_PARAMETER_NAME = "no-indent"; // NOI18N

    public static final String LINE_HINT_NAME = "line"; // NOI18N

    /**
     * Name of the hint that defines an explicit default value of a parameter.
     */
    public static final String DEFAULT_VALUE_HINT_NAME = "default"; // NOI18N
    
    /**
     * Name of the hint that defines whether the given parameter is editable
     * by the user or not.
     * <br/>
     * If the parameter is not editable the user cannot jump to it by <i>TAB</i>
     * key during the post-insert editing. The value however can be changed
     * by the code template processor(s).
     * <br/>
     * Example of non-editable parameter:
     * <pre>
     * ${param editable=false}
     * </pre>
     */
    public static final String EDITABLE_HINT_NAME = "editable"; // NOI18N
    
    /**
     * The ordering attribute defines the sequence in which placeholders are
     * completed. Use 0 for the first element, 1 for the second... Placeholders
     * without ordering information will be placed after the last placeholder
     * with ordering information.
     *
     * <p>
     * Example of ordering of parameters :
     * <pre>
     * // paramC comes first, then paramB, then paramA
     * ${paramA} ${paramB ordering=2} ${paramC ordering=1}
     * </pre>
     * </p>
     * https://netbeans.org/bugzilla/show_bug.cgi?id=181703
     * @since 1.43.0
     */
    public static final String ORDERING_HINT_NAME = "ordering"; // NOI18N

    private final CodeTemplateParameterImpl impl;
    
    CodeTemplateParameter(CodeTemplateParameterImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Get name of this parameter as parsed from the code template description's text.
     */
    public String getName() {
        return impl.getName();
    }
    
    /**
     * Get the present value of this parameter.
     * 
     * @return non-null text value of this parameter.
     *  <br/>
     *  The default value of the parameter is set to the name of the parameter.
     *  <br/>
     *  If the parameter has hint of name
     *  {@link #DEFAULT_VALUE_HINT_NAME} then the default value
     *  is taken from the hint.
     *
     *  <p>
     *  Once the code template gets inserted into the document
     *  (can be checked by {@link CodeTemplateInsertRequest#isInserted()})
     *  then the user may modify the parameter's value explicitly and this method
     *  will reflect these changes.
     */
    public String getValue() {
        return impl.getValue();
    }
    
    /**
     * Set a new value for this parameter.
     * <br/>
     * The value can only be set to the master parameters
     * because slave parameters will inherit values from their masters.
     * <br/>
     * If the code template was not yet inserted into the text the value
     * will be remembered and used as a default value during insertion.
     * <br/>
     * If the code template was already inserted and it's still actively
     * being changed then the value is propagated directly to the document's text.
     * 
     * @see CodeTemplateInsertRequest#isInserted()
     */
    public void setValue(String newValue) {
        impl.setValue(newValue);
    }
    
    /**
     * Check whether this parameter is editable by the user.
     *
     * @return true if this parameter is editable or false if it will
     *  be skipped during parameters user's editing.
     */
    public boolean isEditable() {
        return impl.isEditable();
    }

    /**
     * Check whether the value of this parameter was modified by the user.
     *
     * @return true if the user has explicitly modify value of this parameter
     *  by typing or false if not.
     */
    public boolean isUserModified() {
        return impl.isUserModified();
    }
    
    /**
     * Get starting offset of this parameter
     * in the {@link CodeTemplateInsertRequest#getInsertText()}.
     * 
     * @return &gt;=0 starting offset of this parameter in the text being
     *  inserted into the document.
     *  <br/>
     *  After the code template gets inserted into the document
     *  the value continues to be updated if the user changes the value
     *  by typing until the code template gets released which can be
     *  determined by {@link CodeTemplateInsertRequest#isReleased()}.
     */
    public int getInsertTextOffset() {
        return impl.getInsertTextOffset();
    }
    
    /**
     * Get starting offset of this parameter in the parametrized text.
     * <br/>
     * The parametrized text can be obtained
     * by {@link CodeTemplateInsertRequest#getParametrizedText()}.
     *
     * @return &gt;=0 index of the '${' in the parametrized text.
     * @see #getParametrizedTextEndOffset()
     */
    public int getParametrizedTextStartOffset() {
        return impl.getParametrizedTextStartOffset();
    }
    
    /**
     * Get the ending offset of this parameter in the parametrized text.
     * <br/>
     * The parametrized text can be obtained
     * by {@link CodeTemplateInsertRequest#getParametrizedText()}.
     *
     * @return &gt;=0 end offset of the parameter in the parametrized text
     *  pointing right after the closing '}' of the parameter.
     * @see #getParametrizedTextStartOffset()
     */
    public int getParametrizedTextEndOffset() {
        return impl.getParametrizedTextEndOffset();
    }

    /**
     * Get map of the [String,String] hints in the parameter.
     * <br/>
     * For example the hints map for <code>${param hint1 hint2="defaultValue"}</code>
     * will contain ["hint1","true"] and ["hint2","defaultValue"].
     */
    public Map<String, String> getHints() {
        return impl.getHints();
    }
    
    /**
     * Get the master parameter of this parameter.
     *
     * @return master parameter for this parameter or null if this parameter
     *  is master parameter.
     */
    public CodeTemplateParameter getMaster() {
        return impl.getMaster();
    }
    
    /**
     * Get unmodifiable collection of the slave parameters.
     *
     * @return non-null collection of the slave parameters for this parameter.
     *  <br/>
     *  The collection will be empty if this is a slave parameter
     *  or a master with no slaves.
     */
    public Collection<? extends CodeTemplateParameter> getSlaves() {
        return impl.getSlaves();
    }
    
    /**
     * Check whether this parameter is slave or not.
     */
    public boolean isSlave() {
        return impl.isSlave();
    }
    
    CodeTemplateParameterImpl getImpl() {
        return impl;
    }

}
