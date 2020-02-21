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
