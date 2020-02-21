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

import java.util.ResourceBundle;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/** Settings for the C/C++/Fortran. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 */
public class ShellSettings extends SharedClassObject {

    /** The singleton instance */
    //static ShellSettings cppSettings;
    /** serial uid */
    static final long serialVersionUID = -2942465353463577336L;

    // Option labels
    public static final String PROP_DEFSHELLCOMMAND = "defaultShellCommand";//NOI18N
    public static final String PROP_SAVE_ALL = "saveAll";        //NOI18N
    /** The resource bundle for the form editor */
    private static ResourceBundle bundle;

    /**
     *  Initialize each property.
     */
    @Override
    protected void initialize() {
        super.initialize();
    }

    /** Return the signleton cppSettings */
    public static ShellSettings getDefault() {
        return findObject(ShellSettings.class, true);
    }

    /**
     * Get the display name.
     *
     *  @return value of OPTION_CPP_SETTINGS_NAME
     */
    public String displayName() {
        return getString("OPTION_SHELL_SETTINGS_NAME");		        //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_shell_settings");	        //NOI18N // FIXUP
    }

    /**
     * Default Shell Command
     */
    public void setDefaultShellCommand(String dsc) {
        putProperty(PROP_DEFSHELLCOMMAND, dsc, true);
    }

    /**
     * Default Shell Command
     */
    public String getDefaultShellCommand() {
        String dsc = (String) getProperty(PROP_DEFSHELLCOMMAND);
        if (dsc == null) {
            return "/bin/sh"; // NOI18N
        } else {
            return dsc;
        }
    }

    /** Getter for the SaveAll property */
    public boolean getSaveAll() {
        Boolean dsc = (Boolean) getProperty(PROP_SAVE_ALL);
        if (dsc == null) {
            return true;
        }
        return dsc.booleanValue();
    }

    /** Setter for the SaveAll property */
    public void setSaveAll(boolean sa) {
        putProperty(PROP_SAVE_ALL, sa ? Boolean.TRUE : Boolean.FALSE, true);
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ShellSettings.class);
        }
        return bundle.getString(s);
    }
}
