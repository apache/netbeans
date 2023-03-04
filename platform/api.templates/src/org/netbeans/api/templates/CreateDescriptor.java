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
package org.netbeans.api.templates;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Describes file creation request. The description is produced by the
 * {@link FileBuilder} and is sent out to
 * {@link CreateFromTemplateAttributes} and {@link CreateFromTemplateHandler} 
 * SPIs as the context for their work.
 * <p>
 * The class is not thread-safe. Do not access the descriptor from a thread other
 * than executing the {@link CreateFromTemplateHandler} callbacks.
 * 
 * @author sdedic
 */
public final class CreateDescriptor {
    /**
     * Parameter to enable free file extension mode.
     * By default, the extension of the newly created file will be inherited
     * from the template. But if {@link FileBuilder#createFromTemplate(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, java.lang.String, java.util.Map, org.netbeans.api.templates.FileBuilder.Mode)} is called with this
     * parameter set to {@link Boolean#TRUE}
     * and the file name already seems to
     * include an extension (<code>*.*</code>), the handler should not append
     * any extension from the template.
     * @since org.openide.loaders 7.16
     * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/templates/support/Templates.SimpleTargetChooserBuilder.html#freeFileExtension--"><code>Templates.SimpleTargetChooserBuilder.freeFileExtension</code></a>
     */
    public static final String FREE_FILE_EXTENSION = "freeFileExtension"; // NOI18N
    
    /**
     * Specifies that no formatting or indentation should be performed on the template. 
     * The parameter can be specified as parameter to the template (possibly through layer registration
     * of the template. Value is kept for backwards compatibility, but the attribute does not apply
     * just to java templates.
     * <p>
     * It's responsibility of {@link CreateFromTemplateHandler} to pay attention to this value.
     */
    public static final String PREFORMATTED_TEMPLATE = "org-netbeans-modules-java-preformattedSource"; // NOI18N


    private final FileObject      template;
    private final FileObject      target;

    /**
     * Lookup that may carry additional information for handlers or decorators.
     */
    private  Lookup       contextLookup = Lookup.EMPTY;
    
    /**
     * The originally specified name for the new file
     */
    @SuppressWarnings("PackageVisibleField")
    String                name;
    
    /**
     * The proposed name - either specified, or computed
     */
    @SuppressWarnings("PackageVisibleField")
    String                proposedName;
    
    /**
     * Template parameters
     */
    @SuppressWarnings("PackageVisibleField")
    Map<String, Object>   parameters;

    /**
     * The locale used for file creation
     */
    Locale                locale = Locale.getDefault();
    
    /**
     * The extension is supplied as a part of the name
     */
    boolean               freeExtension;
    
    /**
     * If true, the template is preformatted and no indentation should
     * take place.
     */
    boolean               preformatted;
    
    /* package private */
    CreateDescriptor(FileObject template, FileObject target) {
        this.template = template;
        this.target = target;
    }
    
    /**
     * @return the template file
     */
    public @NonNull FileObject getTemplate() {
        return template;
    }
    
    /**
     * @return the target folder
     */
    public @NonNull FileObject getTarget() {
        return target;
    }
    
    /**
     * Provides the desired name for the created file. {@code null} can be
     * returned to indicate the filename should be derived automatically.
     * @return name for the created file
     */
    public @CheckForNull String getName() {
        return name;
    }
    
    /**
     * Provides a name proposed for the file. If the caller specified the name,
     * the value will be the same as {@link #getName}. A handler is encouraged
     * to use the proposed name if it does not require a certain naming scheme.
     * @return proposed name for the created file
     */
    public @NonNull String getProposedName() {
        return proposedName != null ? proposedName : name;
    }
    
    /**
     * Provides the desired user locale for creating the template
     * @return locale
     */
    public @NonNull Locale getLocale() {
        return locale;
    }
    
    /**
     * Provides value for the named key. Values are originally provided by
     * the caller, or the template itself; values can be provided also by
     * {@link CreateFromTemplateAttributes} implementors.
     * 
     * @param <T> value type.
     * @param n key name
     * @return named value or {@code null} if the key does not exist/has no value.
     */
    @CheckForNull
    public <T> T getValue(String n) {
        return (T)parameters.get(n);
    }

    /**
     * Provides access to the complete parameter map.
     * @return readonly string-value map.
     */
    public @NonNull Map<String, Object> getParameters() {
        return parameters == null ? Collections.<String, Object>emptyMap() : 
                Collections.unmodifiableMap(parameters);
    }
    
    /**
     * Specifies whether the extension should be taken from the specified name,
     * or the extension is fixed to the template's one.
     * 
     * @return true, if the name contains already the extension.
     */
    public boolean hasFreeExtension() {
        return freeExtension;
    }
    
    public boolean isPreformatted() {
        return preformatted;
    }

    void setLookup(Lookup contextLookup) {
        this.contextLookup = contextLookup;
    }

    /**
     * Lookup that may contain additional services or instructions for
     * the template creation.
     * @since 1.23
     */
    public Lookup getLookup() {
        return contextLookup;
    }
}
