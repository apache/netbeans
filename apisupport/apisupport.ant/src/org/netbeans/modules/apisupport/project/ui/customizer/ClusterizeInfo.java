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
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.beans.PropertyEditorSupport;
import java.io.File;
import javax.swing.Action;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class ClusterizeInfo extends FilterNode
implements Comparable<ClusterizeInfo> {

    final String path;
    final ManifestManager mm;
    final File jar;
    private ClusterizeAction state = ClusterizeAction.IGNORE;
    private Sheet sheet;

    private static Node findNode(File f) {
        try {
            if (f != null) {
                final FileObject fo = FileUtil.toFileObject(f);
                if (fo != null) {
                    return DataObject.find(fo).getNodeDelegate();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Node.EMPTY;
    }

    ClusterizeInfo(String path, ManifestManager mm, File jar) {
        super(findNode(jar), mm == null ? new Children.Array() : Children.LEAF);
        disableDelegation(DELEGATE_SET_DISPLAY_NAME | DELEGATE_SET_NAME | DELEGATE_SET_SHORT_DESCRIPTION | DELEGATE_SET_VALUE);
        enableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_GET_NAME | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_GET_VALUE);
        this.path = path;
        this.mm = mm;
        this.jar = jar;
    }

    @Override
    public Action[] getActions(boolean context) {
        return null;
    }

    public String getCodeName() {
        return mm == null ? "" : mm.getCodeNameBase(); // NOI18N
    }

    public ClusterizeAction getAction() {
        return state;
    }
    public void setAction(ClusterizeAction a) {
        state = a;
        firePropertyChange("action", null, null); // NOI18N
        for (Node n : getChildren().getNodes()) {
            if (n instanceof ClusterizeInfo) {
                ClusterizeInfo ci = (ClusterizeInfo)n;
                ci.setAction(a);
            }
        }
        ClusterizeInfo ci = this;
        while (ci.getParentNode() instanceof ClusterizeInfo) {
            ci = (ClusterizeInfo)ci.getParentNode();
        }
        ci.firePropertyChange("selectedFilesCount", null, null); // NOI18N
    }

    @Override
    public PropertySet[] getPropertySets() {
        if (sheet != null) {
            return sheet.toArray();
        }
        try {
            sheet = Sheet.createDefault();
            Set ss = sheet.get(Sheet.PROPERTIES);
            final Reflection<String> cnb = new PropertySupport.Reflection<String>(this, String.class, "getCodeName", null); // NOI18N
            cnb.setDisplayName(NbBundle.getMessage(ClusterizeInfo.class, "MSG_ClusterizeCodeNameBase"));
            cnb.setName("cnb"); // NOI18N
            ss.put(cnb); // NOI18N
            final Reflection<ClusterizeAction> act = new PropertySupport.Reflection<ClusterizeAction>(this, ClusterizeAction.class, "action"); // NOI18N
            act.setDisplayName(NbBundle.getMessage(ClusterizeInfo.class, "MSG_ClusterizeActivateAs"));
            act.setName("action"); // NOI18N
            ss.put(act); // NOI18N
            final Reflection<Integer> count = new PropertySupport.Reflection<Integer>(this, Integer.class, "getSelectedFilesCount", null); // NOI18N
            count.setDisplayName(NbBundle.getMessage(ClusterizeInfo.class, "MSG_ClusterizeNumberOfModules"));
            count.setName("selectedFilesCount"); // NOI18N
            ss.put(act); // NOI18N
            return sheet.toArray();
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }


    @Override
    public int compareTo(ClusterizeInfo o) {
        return jar.compareTo(o.jar);
    }

    public int getSelectedFilesCount() {
        if (Children.LEAF == getChildren()) {
            return state == ClusterizeAction.IGNORE ? 0 : 1;
        } else {
            int cnt = 0;
            for (Node n : getChildren().getNodes()) {
                ClusterizeInfo ci = (ClusterizeInfo)n;
                cnt += ci.getSelectedFilesCount();
            }
            return cnt;
        }
    }

    void categorize(java.util.Set<String> autoload, java.util.Set<String> eager, java.util.Set<String> enabled) {
        if (Children.LEAF == getChildren()) {
            switch (state) {
                case ENABLED: enabled.add(path); return;
                case AUTOLOAD: autoload.add(path); return;
                case EAGER: eager.add(path); return;
            }
        } else {
            for (Node n : getChildren().getNodes()) {
                ClusterizeInfo ci = (ClusterizeInfo) n;
                ci.categorize(autoload, eager, enabled);
            }
        }
    }
}
