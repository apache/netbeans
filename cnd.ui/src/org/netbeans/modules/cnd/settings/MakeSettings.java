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
package org.netbeans.modules.cnd.settings;

import java.io.File;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/** Settings for the C/C++/Fortran Module. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 *
 */
public class MakeSettings extends SharedClassObject {

    /** serial uid */
    static final long serialVersionUID = 1276277545941336641L;
    public static final String PROP_DEFAULT_BUILD_DIR = "defaultBuildDirectory"; //NOI18N
    public static final String PROP_EXECUTOR = "executor";	//NOI18N
    public static final String PROP_REUSE_OUTPUT = "reuseOutput";	//NOI18N
    public static final String PROP_SAVE_ALL = "saveAll";	//NOI18N
    /** The resource bundle for the form editor */
    private static ResourceBundle bundle;

    /**
     *  Initialize each property.
     */
    @Override
    protected void initialize() {

        super.initialize();

        setReuseOutput(false);
        setSaveAll(true);
    }

    /** 
     *  Get the display name.
     *
     *  @return value of OPTION_MAKE_SETTINGS_NAME
     */
    public String displayName() {
        return getString("OPTION_MAKE_SETTINGS_NAME");	    	//NOI18N
    }

    /**
     *  Return the singleton instance. Instantiate it if necessary.
     */
    public static MakeSettings getDefault() {
        return findObject(MakeSettings.class, true);
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakeSettings.class);
        }
        return bundle.getString(s);
    }

    /**
     *  Getter for the default build directory. This should be a relative path
     *  from the filesystem of the current
     *  {@link org.openide.filesystems.FileObject#FileObject() FileObject}.
     *
     *  @return the default build directory
     */
    public String getDefaultBuildDirectory() {
        String dir = (String) getProperty(PROP_DEFAULT_BUILD_DIR);
        if (dir == null) {
            return "."; // NOI18N
        } else {
            return dir;
        }
    }

    /**
     *  Set the default build directory. This cannot be an absolute path.
     *
     *  @param path Relative path to the build directory
     */
    public void setDefaultBuildDirectory(String dir) {
        if (!dir.startsWith(File.separator)) {
            String odir = getDefaultBuildDirectory();
            if (!odir.equals(dir)) {
                putProperty(PROP_DEFAULT_BUILD_DIR, dir, true);
            }
        } else {
            String message = getString("MSG_RelBuildPath"); //NOI18N
            if (CndUtils.isStandalone()) {
                System.err.println(message);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            }
        }
    }

    /**
     *  Getter for the default make(1) program.
     *
     *  @return the default name
     */
    public String getDefaultMakeCommand() {
        return "make"; // NOI18N
    }

    /** If true, Ant Execution uses always the same Output tab. */
    public boolean getReuseOutput() {
        return ((Boolean) getProperty(PROP_REUSE_OUTPUT)).booleanValue();
    }

    /** Sets the reuseOutput property. */
    public void setReuseOutput(boolean b) {
        putProperty(PROP_REUSE_OUTPUT, b ? Boolean.TRUE : Boolean.FALSE, true);
    }

    /** Getter for the SaveAll property */
    public boolean getSaveAll() {
        return ((Boolean) getProperty(PROP_SAVE_ALL)).booleanValue();
    }

    /** Setter for the SaveAll property */
    public void setSaveAll(boolean sa) {
        putProperty(PROP_SAVE_ALL, sa ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_building_make");  //NOI18N
    }
}
