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

import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.MapFormat;
import org.openide.util.Parameters;

/**
 * Fluent interface for file creation. The Builder is first parametrized. After
 * everything is set up, call {@link #build} to materialize the template using
 * the supplied parameters/settings.
 * <p>
 * The create file(s) get names derived from the template and the existing target folder
 * contents (so that the filenames do not conflict with existing files). A desired
 * filename can be set up by {@link #name}.
 * <p>
 * The file build request will be forwarded to {@link CreateFromTemplateHandler}s; if none
 * {@link CreateFromTemplateHandler#accept}s the request, the default procedure takes place,
 * depending on the {@link #defaultMode} setting (default: {@link Mode#COPY}).
 * <p>
 * There are several values predefined:<ul>
 * <li>name - the created filename without extension
 * <li>nameAndExt - the created filename including the extension
 * <li>date - date of creation, printed in the default date format
 * <li>dateTime - {@link Date} object representing the creation time
 * <li>time - time of creation, printed in the default time format
 * <li>user - the user id of the user creating the file
 * </ul>
 * 
 * @author sdedic
 */
public final class FileBuilder {
    /**
     * A specific {@link CreateFromTemplateHandler} to be used with this particular template.
     * If present on a template, will be used as a default. Still can be overriden by a lookup-registered
     * registered {@link CreateFromTemplateHandler}.
     * @since 1.23
     */
    public static final String ATTR_TEMPLATE_HANDLER = "template.createTemplateHandler"; // NOI18N

    /**
     * A flag used by the default template processing to create files from template structures in <b>existing</b> folders
     * rather than create unique folder names. By default, folders in template will create unique folders on the disk,
     * if the same-named folder already exists. The attribute, if defined, should have a boolean value true/false.
     * @since 1.23
     */
    public static final String ATTR_TEMPLATE_MERGE_FOLDERS = "template.mergeFolders"; // NOI18N
    
    /**
     * Marks file(s) in the template, that should be opened in the editor. If the template file attribute
     * is set to {@link Boolean#TRUE}, the template handler should the file created from the file template from
     * the {@link #build} method. Note the attribute serves as a hint, so the Handler may choose to ignore
     * the instruction.
     * @since 1.23
     */
    public static final String ATTR_TEMPLATE_OPEN_FILE = "template.openFile"; // NOI18N
    
    /**
     * Determines the default procedure for copying the template in {@link #createFromTemplate}.
     */
    public static enum Mode {
        /**
         * The template will be formatted using formatter.
         */
        FORMAT, 
        /**
         * The template will be just copied.
         */
        COPY, 
        /**
         * The template will not be processed if no custom {@link CreateFromTemplateHandler} handles it.
         */
        FAIL
    }
    
    /**
     * Creates a Builder based on the CreateDescriptor. The FileBuilder inherits 
     * all parameters of the original {@link CreateDescriptor}. The client may change the attributes.
     * The method may be useful when creating secondary files; for example target and all attributes
     * are retained. During {@link #build()}, attributes may be redefined as needed for the 
     * additional file, just like in normal Builder operation.
     * <p>
     * The new FileBuilder instance is completely indepenent of the original Descriptor. If the CreateDescriptor
     * supports additional properties in the future, using this method guarantees that they will be
     * transferred to the FileBuilder copy.
     * 
     * @param desc the original descriptor
     * @return new FileBuilder
     * @since 1.5
     */
    public static @NonNull FileBuilder fromDescriptor(@NonNull CreateDescriptor desc) {
        Parameters.notNull("desc", desc);
        return new FileBuilder(desc.getTemplate(), desc.getTarget()).
                name(desc.getProposedName()).
                useLocale(desc.getLocale()).
                withParameters(desc.getParameters());
    }
    
    /**
     * Creates a new FileBuilder for a specific template and target folder.
     * @param template the template to use.
     * @param target the target folder; must already exist.
     */
    public FileBuilder(@NonNull FileObject template, @NonNull FileObject target) {
        descriptor = new CreateDescriptor(template, target);
    }

    /**
     * Use specific Lookup for creation. This allows to pass in additional
     * parameters or services. A GUI-aware caller may, for example, add
     * a ProgressHandle or a project ActionProgress interface to the lookup,
     * in a hope that template handler will use service to report progress.
     * @param contextLookup the Lookup passed to template creation subprocesses
     * @return this FileBuilder instance.
     * @since 1.23
     */
    public FileBuilder useLookup(Lookup contextLookup) {
        descriptor.setLookup(contextLookup);
        return this;
    }
    
    /**
     * Specifies the locale to be used during file creation.
     * The locale also applies to the standard parameters passed to the template (e.g. date and time representation).
     * @param l the locale
     * @return this FileBuilder instance.
     */
    public FileBuilder    useLocale(@NonNull Locale l) {
        descriptor.locale = l;
        return this;
    }
    
    /**
     * Sets the desired target file's name. 
     * 
     * @param n the name
     * @return this FileBuilder instance
     */
    public FileBuilder    name(String n) {
        descriptor.name = n;
        return this;
    }
    
    /**
     * Includes parameters for the template.
     * For backwards compatibility, special parameters {@link CreateDescriptor#FREE_FILE_EXTENSION} and
     * {@link CreateDescriptor#PREFORMATTED_TEMPLATE} are processed and appropriate properties modified on the
     * CreateDescriptor.
     * 
     * @param params the string-value pairs
     * @return this FileBuilder instance
     */
    public FileBuilder    withParameters(@NullAllowed Map<String, ?> params) {
        if (descriptor.parameters != null) {
            descriptor.parameters.putAll(params);
        } else {
            descriptor.parameters = params == null ? null : new HashMap<String,Object>(params);
        }
        if (params != null) {
            Object v = params.get(CreateDescriptor.FREE_FILE_EXTENSION);
            if (v instanceof Boolean) {
                boolean val = Boolean.TRUE.equals(v);
                descriptor.freeExtension = val;
            }
            v = params.get(CreateDescriptor.PREFORMATTED_TEMPLATE);
            if (v instanceof Boolean) {
                boolean val = Boolean.TRUE.equals(v);
                descriptor.preformatted = val;
            }
        }
        return this;
    }
    
    /**
     * Adds a parameter to the template.
     * For backwards compatibility, special parameters {@link CreateDescriptor#FREE_FILE_EXTENSION} and
     * {@link CreateDescriptor#PREFORMATTED_TEMPLATE} are processed and appropriate properties modified on the
     * CreateDescriptor.
     * 
     * @param n parameter name
     * @param v the value
     * @return this FileBuilder instance
     */
    public FileBuilder    param(@NonNull String n, Object v) {
        if (descriptor.parameters == null) {
            descriptor.parameters = new HashMap<>();
        }
        descriptor.parameters.put(n, v);
        if (v instanceof Boolean) {
            if (CreateDescriptor.FREE_FILE_EXTENSION.equals(n)) {
                boolean val = Boolean.TRUE.equals(v);
                descriptor.freeExtension = val;
            }
            if (CreateDescriptor.PREFORMATTED_TEMPLATE.equals(n)) {
                boolean val = Boolean.TRUE.equals(v);
                descriptor.preformatted = val;
            }
        }
        return this;
    }
    
    /**
     * Specifies the behaviour to be used when no {@link CreateFromTemplateHandler} accepts the template.
     * @param m the default processing mode
     * @return this FileBuilder instance
     * @see Mode for details
     */
    public FileBuilder    defaultMode(@NonNull Mode m) {
        this.defaultMode = m;
        return this;
    }
    
    /**
     * Uses the specified formatter for file creation. Also sets the default mode to
     * {@link Mode#FORMAT}. If the supplied Format instance <i>happens to be</i> a
     * {@link MapFormat}, the templating code will pass parameters produced by
     * {@link CreateFromTemplateAttributes} to the format when the target file
     * contents is generated.
     * 
     * @param def the format to use
     * @return  this FileBuilder instance
     * @see Mode for details on different modes
     */
    public FileBuilder    useFormat(@NonNull Format def) {
        this.format = def;
        return defaultMode(Mode.FORMAT);
    }
    
    /**
     * Creates the file(s) from template.
     * @return list of created files. If some file is 'master' or otherwise of high importance and represents
     * the file set, it should be placed first in the list.
     * @throws IOException if the creation fails
     */
    public @CheckForNull List<FileObject> build() throws IOException {
        return CreateFromTemplateImpl.build(this);
    }
    
    CreateDescriptor    getDescriptor() {
        return descriptor;
    }
    
    /**
     * Creates a descriptor from the current Builder's state. 
     * If `collectAttributes' is false, the descriptor
     * will have no additional parameters set from {@link CreateFromTemplateAttributes} providers;
     * the caller must process the providers, if it wishes to get additional attributes.
     * The Descriptor can be used to collect information from attribute providers or manually
     * trigger file creation in template handler.
     * <p>
     * The operation changes the FileBuilder state.
     * 
     * @param collectAttributes if true, attribute providers are asked to add their attributes
     * to the builder/descriptor.
     * @return descriptor
     * @since 1.5
     */
    public @NonNull CreateDescriptor createDescriptor(boolean collectAttributes) {
        if (collectAttributes) {
            CreateFromTemplateImpl.collectAttributes(this);
        }
        CreateFromTemplateImpl.computeEffectiveName(descriptor);
        return descriptor;
    }
    
    private final CreateDescriptor descriptor;
    
    @SuppressWarnings("PackageVisibleField")
    Mode    defaultMode;
    
    @SuppressWarnings("PackageVisibleField")
    Format  format;
    

    /**
     * Creates a new file based on the template. This convenience method is intended for easier
     * migration of clients using DataLoader templating API before {@link FileBuilder} introduction.
     * The method will collect parameters
     * tied to the template using registered {@link CreateFromTemplateAttributes} providers,
     * and will try to locate a willing {@link CreateFromTemplateHandler} that will create
     * the target file. If no such handler exists, and the {@code defaultCopy} parameter is true,
     * the file contents is just copied to the target location.
     * <p>
     * If the {@code name} parameter is null, the function attempts to compute a suitable name
     * from the file.
     * <p>
     * The default copy algorithm uses the supplied {@link Mode#FORMAT} to
     * process tokens.
     * <p>
     * If the passed {@code name} is {@code null}, the implementation will pick a free name based on
     * the template's own name (see {@link FileUtil#findFreeFileName}).
     * @param f the template file
     * @param folder the target folder, must exist
     * @param name the desired name. If {@code null}, the implementation will choose the name.
     * @param attributes values to apply on the template. May be {@code null} = no values.
     * @param defaultMode the mode of operations to use
     * @return The created file, or {@code null} if no creation handler is located.
     * @throws IOException when an I/O operation fails
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    @CheckForNull
    public static FileObject createFromTemplate(@NonNull FileObject f, @NonNull FileObject folder, 
            @NullAllowed String name, @NullAllowed Map<String, ?> attributes,
            Mode defaultMode) 
    throws IOException {
        Format frm = null;
        
        switch (defaultMode) {
            case FORMAT:
                MapFormat mf = new MapFormat(new HashMap<>());
                mf.setExactMatch(false);
                mf.setLeftBrace("__");
                mf.setRightBrace("__");
                frm = mf;
                break;
                
            case COPY:
                frm = new Format() {
                    @Override
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                        toAppendTo.append(obj);
                        return toAppendTo;
                    }

                    @Override
                    public Object parseObject(String source, ParsePosition pos) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };                    
                break;
        }
        FileBuilder fb = new FileBuilder(f, folder).
                            name(name).
                            withParameters(attributes).
                            useFormat(frm).
                            defaultMode(defaultMode);
        
        List<FileObject> fos = fb.build();
        if (fos == null || fos.isEmpty()) {
            return null;
        } else {
            return fos.iterator().next();
        }
    }
}
