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

package org.netbeans.api.editor.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The definition of a code template. A code template is basically a piece of
 * code with an abbreviation associated to it. When a user types the abbreviation
 * to the editor and presses the expansion key the code associated with the
 * abbreviation gets expanded. The code can contain various parameters that the user
 * can enter during the expansion.
 * 
 * <p>The <code>CodeTemplateDescription</code>s can be obtained from
 * <code>CodeTemplateSettings</code> class that can be loaded from <code>MimeLookup</code>
 * for a particular mime type. See the example below.
 * 
 * <pre>{@code
 * Lookup l = MimeLookup.getLookup(MimePath.parse(mimePath));
 * CodeTemplateSettings cds = l.lookup(CodeTemplateSettings.class);
 * List<CodeTemplateDescription> codeTemplates = cds.getCodeTemplateDescriptions();
 * }</pre>
 * 
 * <p><b>IMPORTANT</b>: There is a much more powerful API for working with editor
 * code templates in
 * <a href="@org-netbeans-modules-editor-codetemplates@/overview-summary.html">Editor Code Templates</a>
 * module. If you are retrieving this class from <code>MimeLookup</code> you should
 * probably use the Editor Code Templates API instead.
 * 
 * @see CodeTemplateSettings
 * @author Miloslav Metelka
 */
public final class CodeTemplateDescription {

    private final String abbreviation;
    private final String description;
    private final String parametrizedText;
    private final List<String> contexts;    
    private final String uniqueId;
    private final String mimePath;
    
    /**
     * Creates a new code template description. It call the other constructor
     * passing <code>null</code> for the <code>contexts</code> parameter.
     * 
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     */
    public CodeTemplateDescription(String abbreviation, String description, String parametrizedText) {
        this(abbreviation, description, parametrizedText, null, null, null);
    }
    
    /**
     * Creates a new code template description.
     * 
     * <p>Usually clients do not need to create <code>CodeTemplateDescription</code>s
     * by themselvs. Instead they use <code>MimeLookup</code> and <code>CodeTemplateSettings</code>
     * to access code templates registered in the system.
     *
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     *   Can be <code>null</code>
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     * @param contexts The list of context ids that apply for this code template.
     *   Can be <code>null</code>
     * @param uniqueId The id uniquely identifying this template. If you pass
     *   non-<code>null</code> value, please make sure that it is really a unique
     *   id for this template. Can be <code>null</code>.
     */
    public CodeTemplateDescription(
        String abbreviation, 
        String description, 
        String parametrizedText, 
        List<String> contexts,
        String uniqueId
    ) {
        this(abbreviation, description, parametrizedText, contexts, uniqueId, null);
    }
    
    /**
     * Creates a new code template description. The same as {@link #CodeTemplateDescription(String, String, String, List, String)},
     * but with additional <code>mimePath</code> parameter.
     * 
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     *   Can be <code>null</code>
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     * @param contexts The list of context ids that apply for this code template.
     *   Can be <code>null</code>
     * @param uniqueId The id uniquely identifying this template. If you pass
     *   non-<code>null</code> value, please make sure that it is really a unique
     *   id for this template. Can be <code>null</code>.
     * @param mimePath The mime path where this code template description was registered for.
     * 
     * @since 1.15
     */
    public CodeTemplateDescription(
        String abbreviation, 
        String description, 
        String parametrizedText, 
        List<String> contexts,
        String uniqueId,
        String mimePath
    ) {
        assert abbreviation != null : "The abbreviation parameter can't be null"; //NOI18N
        assert parametrizedText != null : "The parametrizedText parameter can't be null"; //NOI18N
        
        this.abbreviation = abbreviation;
        this.description = description;
        this.parametrizedText = parametrizedText;
        this.contexts = contexts == null ? 
            Collections.<String>emptyList() : 
            Collections.unmodifiableList(new ArrayList<String>(contexts));
        this.uniqueId = uniqueId;
        this.mimePath = mimePath;
    }
    
    /**
     * Gets the abbreviation text that triggers expansion of this code template.
     * 
     * <p>The abbreviation text should be unique among all code templates defined
     * for a one mime type so that each code template can be expanded individually.
     *
     * @return The abbreviation text that expands this code template.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Gets textual description of this code template. It's a display text
     * that can be shown in UI such as the code completion window or Tools-Options
     * dialog.
     *
     * @return The display text for this code template or <code>null</code> if this
     *   code template has no descriptions.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the code text of this code template.
     * 
     * This is the text that will be expanded when a user types the abbreviation
     * in the editor and presses the expansion key. The text can contain parameters
     * in the form of "${...}".
     *
     * @return The code text with parameters.
     */
    public String getParametrizedText() {
        return parametrizedText;
    }

    /**
     * Gets the list of contexts that apply for this code template. The contexts
     * are simply unique identifiers used by the infrastructure to filter out
     * code templates that are not suitable for the editor context, where a user
     * types.
     * 
     * <p>The actual identifiers are defined by each particular language (mime type)
     * and can be different for different languages. The language defines contexts
     * for its constructs such as loops, methods, classes, if-else blocks, etc. and
     * than tags each code template available for that language with a context,
     * where it is meaningful to apply the template.
     * 
     * @return The contexts for this code template.
     */
    public List<String> getContexts() {
        return contexts;
    }

    /**
     * Gets an id that can be used for identifying this template. A code template
     * does not generally have to have a unique id, but if it has one it is
     * guaranteed to uniquely identify the template.
     * 
     * <p class="nonnormative">Unique ids can be useful for tools importing and
     * exporting code templates from other applications such as TextMate, etc.
     * 
     * @return The unique id or <code>null</code>.
     * @since 1.11
     */
    public String getUniqueId() {
        return uniqueId;
    }
    
    /**
     * Gets the mime path where this code template was registered.
     * 
     * @return The mime path string or <code>null</code> if the registration mime
     *   path is unknown.
     * @since 1.15
     */
    public String getMimePath() {
        return mimePath;
    }
    
    public @Override String toString() {
        return "abbrev='" + getAbbreviation() + "', parametrizedText='" + getParametrizedText() + "'"; // NOI18N
    }

    /**
     * Checks whether this code template is equal with a code template passed in
     * as the <code>obj</code> parameter. By definition two code templates are
     * equal if all their fields are equal - ie. all abbreviation, description,
     * parametrizedText, contexts, uniqueId and mimePath fields are equal.
     * 
     * @param obj The code template to compare with.
     * 
     * @return <code>true</code> if and only if this code template is equal to
     *   the <code>obj</code> code template.
     */
    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CodeTemplateDescription other = (CodeTemplateDescription) obj;
        if ((this.abbreviation == null && other.abbreviation != null) || 
            (this.abbreviation != null && other.abbreviation == null) || 
            (this.abbreviation != null && !this.abbreviation.equals(other.abbreviation))
        ) {
            return false;
        }
        if ((this.description == null && other.description != null) || 
            (this.description != null && other.description == null) || 
            (this.description != null && !this.description.equals(other.description))
        ) {
            return false;
        }
        if ((this.parametrizedText == null && other.parametrizedText != null) ||
            (this.parametrizedText != null && other.parametrizedText == null) ||
            (this.parametrizedText != null && !this.parametrizedText.equals(other.parametrizedText))
        ) {
            return false;
        }
        if (this.contexts != other.contexts && (this.contexts == null || !this.contexts.equals(other.contexts))) {
            return false;
        }
        if ((this.uniqueId == null && other.uniqueId != null) ||
            (this.uniqueId != null && other.uniqueId == null) ||
            (this.uniqueId != null && !this.uniqueId.equals(other.uniqueId))
        ) {
            return false;
        }
        if ((this.mimePath == null && other.mimePath != null) ||
            (this.mimePath != null && other.mimePath == null) ||
            (this.mimePath != null && !this.mimePath.equals(other.mimePath))
        ) {
            return false;
        }
        return true;
    }

    public @Override int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.abbreviation != null ? this.abbreviation.hashCode() : 0);
        hash = 59 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 59 * hash + (this.parametrizedText != null ? this.parametrizedText.hashCode() : 0);
        hash = 59 * hash + (this.contexts != null ? this.contexts.hashCode() : 0);
        hash = 59 * hash + (this.uniqueId != null ? this.uniqueId.hashCode() : 0);
        hash = 59 * hash + (this.mimePath != null ? this.mimePath.hashCode() : 0);
        return hash;
    }
    
}
