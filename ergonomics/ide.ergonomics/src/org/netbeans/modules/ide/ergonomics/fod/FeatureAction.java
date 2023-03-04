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
package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.ide.ergonomics.Utilities;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class FeatureAction implements ActionListener {
    private FileObject fo;
    private ProgressHandle handle;
    private JDialog dialog;

    private FeatureAction(FileObject fo) {
        this.fo = fo;
    }

    public static ActionListener create(FileObject fo) {
        return new FeatureAction(fo);
    }

    public void actionPerformed(ActionEvent e) {
        FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(fo);
        String n = Actions.cutAmpersand((String)fo.getAttribute("displayName")); // NOI18N
        boolean success = Utilities.featureDialog(info, n, n);
        if (!success) {
            return;
        }
        
        FileObject newFile = FileUtil.getConfigFile(fo.getPath());
        if (newFile == null) {
            throw new IllegalStateException("Cannot find file: " + fo.getPath());
        }
        
        Object obj = newFile.getAttribute("instanceCreate"); // NOI18N
        if (obj instanceof ActionListener) {
            ((ActionListener)obj).actionPerformed(e);
        }
    }
}
