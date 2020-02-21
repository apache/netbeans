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
package org.netbeans.modules.cnd.makeproject.api;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.options.ViewBinaryFiles;
import org.openide.util.NbBundle;

public final class MakeProjectOptions {
    public static final String VIEW_BINARY_FILES_EVENT_NAME = ViewBinaryFiles.VIEW_BINARY_FILES;

    public static enum PathMode {
        
        REL_OR_ABS(NbBundle.getMessage(MakeProjectOptions.class, "TXT_Auto")),
        REL(NbBundle.getMessage(MakeProjectOptions.class, "TXT_AlwaysRelative")),
        ABS(NbBundle.getMessage(MakeProjectOptions.class, "TXT_AlwaysAbsolute"));

        private final String displayName;

        private PathMode(String displayName) {
            this.displayName = displayName;
        }


        public String getDisplayName() {
            return displayName;
        }

        /** to be able to use in UI without writing renderer */
        @Override
        public String toString() {
            return displayName;
        }
    }

    private MakeProjectOptions() {
    }

    public static void setDefaultMakeOptions(String makeOptions) {
        MakeOptions.setDefaultMakeOptions(makeOptions);
    }

    public static String getMakeOptions() {
        return MakeOptions.getInstance().getMakeOptions();
    }

    public static void setMakeOptions(String options) {
        MakeOptions.getInstance().setMakeOptions(options);
    }

    public static String getPrefApplicationLanguage() {
        return MakeOptions.getInstance().getPrefApplicationLanguage();
    }

    public static void setPrefApplicationLanguage(String lang) {
        MakeOptions.getInstance().setPrefApplicationLanguage(lang);
    }

    public static boolean getResolveSymbolicLinks() {
         return MakeOptions.getInstance().getResolveSymbolicLinks();
    }

    public static boolean getDepencyChecking() {
        return MakeOptions.getInstance().getDepencyChecking();
    }

    public static boolean getRebuildPropChanged() {
        return MakeOptions.getInstance().getRebuildPropChanged();
    }

    public static MakeProjectOptions.PathMode getPathMode() {
        return MakeOptions.getInstance().getPathMode();
    }

    public static void setPathMode(PathMode pathMode) {
        MakeOptions.getInstance().setPathMode(pathMode);
    }

    public static void setShowConfigurationWarning(boolean val) {
        MakeOptions.getInstance().setShowConfigurationWarning(val);
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        MakeOptions.getInstance().addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        MakeOptions.getInstance().removePropertyChangeListener(l);
    }

    public static boolean getViewBinaryFiles() {
        return MakeOptions.getInstance().getViewBinaryFiles();
    }

    public static String getDefExePerm() {
        return MakeOptions.getInstance().getDefExePerm();
    }

    public static void setDefExePerm(String perm) {
        MakeOptions.getInstance().setDefExePerm(perm);
    }
    
    public static String getDefGroup() {
        return MakeOptions.getInstance().getDefGroup();
    }

    public static void setDefGroup(String group) {
        MakeOptions.getInstance().setDefGroup(group);
    }

    public static String getDefOwner() {
        return MakeOptions.getInstance().getDefOwner();
    }

    public static void setDefOwner(String owner) {
        MakeOptions.getInstance().setDefOwner(owner);
    }

    public static String getDefFilePerm() {
        return MakeOptions.getInstance().getDefFilePerm();
    }

    public static void setDefFilePerm(String rerm) {
        MakeOptions.getInstance().setDefFilePerm(rerm);
    }
}
