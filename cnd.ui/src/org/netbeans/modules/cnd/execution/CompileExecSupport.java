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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
