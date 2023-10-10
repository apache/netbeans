/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.editor.codetemplates.spi;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.CodeTemplateInsertHandler;
import org.netbeans.lib.editor.codetemplates.CodeTemplateParameterImpl;
import org.netbeans.lib.editor.codetemplates.CodeTemplateSpiPackageAccessor;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;

/**
 * Code template insert request parses the code template's text
 * to gather the data necessary to insert
 * the particular code template into the document (such as the template's parameters).
 *
 * <p>
 * <strong>State</strong>
 * </p>
 * The insert request can be in three states:
 * <ul>
 *   <li>It is not inserted into the document yet.
 *     Both {@link #isInserted()} and {@link #isReleased()}
 *     return false. Registered {@link CodeTemplateProcessor}s
 *     will be asked to fill in the default values into the parameters.
 *   <li>It is inserted and the user modifies the parameters' values in the document.
 *     {@link #isInserted()} returns true and {@link #isReleased()} returns false.
 *   <li>It is released. {@link #isReleased()} returns true. There is no more
 *     work to do. Code templates processor(s) servicing the request will be released.
 * </ul>
 *
 * <p>
 * <strong>Parameters</strong>
 * </p>
 * The code template's text is first parsed to find the parameters.
 * Each first occurrence of a parameter with particular name define
 * a master parameter. All the other occurrences of a parameter with the same name
 * define slave parameters (of the previously defined master).
 *
 * @see CodeTemplateParameter
 * 
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateInsertRequest {
    
    static {
        CodeTemplateSpiPackageAccessor.register(new SpiAccessor());
    }
    
    private final CodeTemplateInsertHandler handler;
    
    CodeTemplateInsertRequest(CodeTemplateInsertHandler handler) {
        this.handler = handler;
    }

    /**
     * Get code template associated with this insert request.
     */
    public CodeTemplate getCodeTemplate() {
        return handler.getCodeTemplate();
    }

    /**
     * Get the text component into which the template should be inserted
     * at the current caret position.
     */
    public JTextComponent getComponent() {
        return handler.getComponent();
    }
    
    /**
     * Get list of master parameters in the order they are located
     * in the code template text.
     * <br>
     * The master parameters can be explored by the code template processor
     * and their default values can be changed as necessary.
     *
     * @return non-null unmodifiable list of master parameters.
     */
    public List<? extends CodeTemplateParameter> getMasterParameters() {
        return handler.getMasterParameters();
    }
    
    /**
     * Get master parameter with the given name.
     *
     * @param name non-null name of the master parameter to be searched.
     * @return master parameter with the given name or null if no such
     *  parameter exists.
     */
    public CodeTemplateParameter getMasterParameter(String name) {
        for (Iterator<? extends CodeTemplateParameter> it = getMasterParameters().iterator(); it.hasNext();) {
            CodeTemplateParameter master = it.next();
            if (name.equals(master.getName())) {
                return master;
            }
        }
        return null;
    }

    /**
     * Get all the parameters (masters and slaves)
     * present in the code template text in the order as they occur
     * in the parametrized text.
     *
     * @return non-null unmodifiable list of all parameters.
     * @see #getMasterParameters()
     */
    public List<? extends CodeTemplateParameter> getAllParameters() {
        return handler.getAllParameters();
    }
    
    /**
     * Check whether the code template that this request
     * represents was already inserted into the document.
     *
     * @return true if the code template was already inserted into the document
     *  and the inserted default values are being modified by the user
     *  which can result into
     *  {@link CodeTemplateProcessor#parameterValueChanged(CodeTemplateParameter, boolean)}.
     *  <p>
     *  Returns false if the code template was not yet inserted into the document
     *  i.e. the {@link CodeTemplateProcessor#updateDefaultValues()}
     *  is currently being called on the registered processors.
     * @see #isReleased()
     */
    public boolean isInserted() {
        return handler.isInserted();
    }
    
    /**
     * Check whether this request is already released which means
     * that the code template was inserted and values of all the parameters
     * were modified by the user so there is no more work to be done.
     *
     * @return whether this request is already released or not.
     *  If the request was not yet released then {@link #isInserted()}
     *  gives additional info whether request is inserted into the document or not.
     * @see #isInserted()
     */
    public boolean isReleased() {
        return handler.isReleased();
    }
    
    /**
     * Get the present parametrized text handled by this request.
     * <br>
     * By default the code template's parametrized text obtained
     * by {@link CodeTemplate#getParametrizedText()} is used.
     * <br>
     * The parametrized text can be modified by {@link #setParametrizedText(String)}.
     */
    public String getParametrizedText() {
        return handler.getParametrizedText();
    }
    
    /**
     * Set the parametrized text to a new value.
     * <br>
     * This may be necessary if some parameters are just artificial
     * and should be expanded by a particular code template processor
     * before the regular processing.
     * <br>
     * Once this method is called the new parametrized text will be parsed
     * and a fresh new list of parameters will be created.
     *
     * @param parametrizedText new parametrized text to be used.
     */
    public void setParametrizedText(String parametrizedText) {
        handler.setParametrizedText(parametrizedText);
    }

    /**
     * Get the text where all the parameters are replaced
     * by their present values.
     * <br>
     * This is the text to be physically inserted into the document
     * once the template processors possibly update the parameter's values.
     * <br>
     * After the text gets physically inserted into the document this method
     * continues to return the "living" text of the document with the inserted template.
     */
    public String getInsertText() {
        return handler.getInsertText();
    }

    /**
     * Return offset of the inserted template in the document's text.
     * <br>
     * The offset is physically represented as a swing position
     * so it will reflect possible subsequent document mutations.
     * <br>
     * Before the template gets inserted into the document this method
     * returns zero.
     */
    public int getInsertTextOffset() {
        return handler.getInsertOffset();
    }

    private static final class SpiAccessor extends CodeTemplateSpiPackageAccessor {
        
        public CodeTemplateInsertRequest createInsertRequest(CodeTemplateInsertHandler handler) {
            return new CodeTemplateInsertRequest(handler);
        }

        public CodeTemplateParameter createParameter(CodeTemplateParameterImpl impl) {
            return new CodeTemplateParameter(impl);
        }
        
        public CodeTemplateParameterImpl getImpl(CodeTemplateParameter parameter) {
            return parameter.getImpl();
        }

    }

}
