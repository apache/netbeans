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
package org.netbeans.modules.cnd.execution;

import java.io.IOException;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/** 
 * Support for compile and execution of a source file.
 * Looks for the class with the same base name as the primary file,
 * locates a main method in it, and starts it.
 * 
 */
public final class CompileExecSupport extends ExecutionSupport {

    private static final String PROP_LANG_STANDARD = "standard"; // NOI18N
    private static final String PROP_RUN_DIRECTORY = "rundirectory"; // NOI18N
    private static final String PROP_COMPILE_FLAGS = "compileflags"; // NOI18N
    private static final String PROP_LINK_FLAGS = "linkflags"; // NOI18N

    /**
     * Support of compile/run standalone files.
     * 
     * @param entry
     */
    public CompileExecSupport(Entry entry) {
        super(entry);
    }

    @Override
    public void addProperties(Sheet.Set set) {
        //set.put(createParamsProperty(PROP_FILE_PARAMS, getString("PROP_fileParams"), getString("HINT_fileParams"))); // NOI18N;
        //set.put(createRunDirectoryProperty());
        set.put(createLanguageStandardProperty());
        set.put(createCompileFlagsProperty());
        //set.put(createLinkFlagsProperty());
    }

    
    /**
     *  Create language standard property.
     *
     *  @return language standard property
     */
    private PropertySupport<CndLanguageStandard> createLanguageStandardProperty() {

        return new PropertySupport.ReadWrite<CndLanguageStandard>(PROP_LANG_STANDARD, CndLanguageStandard.class,
                getString("PROP_LANG_STANDARD"), // NOI18N
                getString("HINT_LANG_STANDARD")) { // NOI18N

            @Override
            public CndLanguageStandard getValue() {
                return getStandard();
            }

            @Override
            public void setValue(CndLanguageStandard val) {
                setStandard(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setStandard(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get standard.
     *
     *  @return language standard
     */
    public CndLanguageStandard getStandard() {
        String standard = (String) getEntry().getFile().getAttribute(PROP_LANG_STANDARD);
        if (standard == null) {
            FileObject fo = getEntry().getFile();
            if (fo != null) {
                String mimeType = FileUtil.getMIMEType(fo);
                MIMEExtensions mime = MIMEExtensions.get(mimeType);
                if (mime != null) {
                    CndLanguageStandard defaultStandard = mime.getDefaultStandard();
                    if (defaultStandard != null) {
                        standard = defaultStandard.getID();
                    }
                }
            }
        }
        return CndLanguageStandards.StringToLanguageStandard(standard);
    }

    /**
     *  Set standard
     *
     * @param standard
     */
    public void setStandard(CndLanguageStandard standard) {
        try {
            if (standard == null) {
                getEntry().getFile().setAttribute(PROP_LANG_STANDARD, null);
            } else {
                getEntry().getFile().setAttribute(PROP_LANG_STANDARD, standard.getID());
            }
        } catch (IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace(System.err);
            }
        }
    }
    
    /**
     *  Create the run directory property.
     *
     *  @return The run directory property
     */
    private PropertySupport<String> createRunDirectoryProperty() {

        return new PropertySupport.ReadWrite<String>(PROP_RUN_DIRECTORY, String.class,
                getString("PROP_RUN_DIRECTORY"), // NOI18N
                getString("HINT_RUN_DIRECTORY")) { // NOI18N

            @Override
            public String getValue() {
                return getRunDirectory();
            }

            @Override
            public void setValue(String val) {
                setRunDirectory(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get the the run directory, the directory to invoke make from.
     *
     *  @return the run directory
     */
    public String getRunDirectory() {
        String dir = (String) getEntry().getFile().getAttribute(PROP_RUN_DIRECTORY);

        if (dir == null) {
            dir = "."; // NOI18N
        }

        return dir;
    }

    /**
     *  Set the run directory
     *
     *  @param dir the run directory
     */
    public void setRunDirectory(String dir) {
        try {
            getEntry().getFile().setAttribute(PROP_RUN_DIRECTORY, dir);
        } catch (IOException ex) {
            //String msg = MessageFormat.format("INTERNAL ERROR: Cannot set run directory", // NOI18N
            //        new Object[]{FileUtil.toFile(getEntry().getFile()).getPath()});

            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     *  Create compile flags property.
     *
     *  @return compile flags property
     */
    private PropertySupport<String> createCompileFlagsProperty() {

        return new PropertySupport.ReadWrite<String>(PROP_COMPILE_FLAGS, String.class,
                getString("PROP_COMPILE_FLAGS"), // NOI18N
                getString("HINT_COMPILE_FLAGS")) { // NOI18N

            @Override
            public String getValue() {
                return getCompileFlags();
            }

            @Override
            public void setValue(String val) {
                setCompileFlags(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get compile flags.
     *
     *  @return compile flags
     */
    public String getCompileFlags() {
        String flags = (String) getEntry().getFile().getAttribute(PROP_COMPILE_FLAGS);
        if (flags == null) {
            flags = "-g"; // NOI18N
        }

        return flags;
    }

    /**
     *  Set compile flags
     *
     *  @param flags compile flags
     */
    public void setCompileFlags(String flags) {
        try {
            getEntry().getFile().setAttribute(PROP_COMPILE_FLAGS, flags);
        } catch (IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace(System.err);
            }
        }
    }
    
    /**
     *  Create link flags property.
     *
     *  @return link flags property
     */
    private PropertySupport<String> createLinkFlagsProperty() {

        return new PropertySupport.ReadWrite<String>(PROP_LINK_FLAGS, String.class,
                getString("PROP_LINK_FLAGS"), // NOI18N
                getString("HINT_LINK_FLAGS")) { // NOI18N

            @Override
            public String getValue() {
                return getLinkFlags();
            }

            @Override
            public void setValue(String val) {
                setLinkFlags(val);
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() {
                setValue(null);
            }

            @Override
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }

    /**
     *  Get link flags.
     *
     *  @return link flags
     */
    public String getLinkFlags() {
        String flags = (String) getEntry().getFile().getAttribute(PROP_LINK_FLAGS);
        if (flags == null) {
            flags = ""; // NOI18N
            setLinkFlags(flags);
        }

        return flags;
    }

    /**
     *  Set link flags
     *
     *  @param flags link flags
     */
    public void setLinkFlags(String flags) {
        try {
            getEntry().getFile().setAttribute(PROP_LINK_FLAGS, flags);
        } catch (IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace(System.err);
            }
        }
    }
    
    
    
    private String getString(String s) {
        return NbBundle.getMessage(CompileExecSupport.class, s);
    }
}
