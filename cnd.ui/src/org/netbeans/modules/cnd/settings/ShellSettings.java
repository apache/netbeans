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
