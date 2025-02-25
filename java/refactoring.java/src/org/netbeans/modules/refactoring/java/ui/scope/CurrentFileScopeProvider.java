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
package org.netbeans.modules.refactoring.java.ui.scope;

import java.beans.BeanInfo;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@ScopeProvider.Registration(displayName = "#LBL_CurrentFile", id = "current-file", position = 400, iconBase = "org/netbeans/modules/refactoring/java/resources/newFile.png")
@ScopeReference(path="org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
@Messages(value = {"# {0} - Filename", "LBL_CurrentFile=Current File"})
public final class CurrentFileScopeProvider extends ScopeProvider {

    private Scope scope;
    private Icon icon;
    private String detail;

    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        FileObject file = context.lookup(FileObject.class);
        if (file == null || file.isFolder()) {
            return false;
        }

        DataObject currentFileDo = null;
        try {
            currentFileDo = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
        } // Not important, only for Icon.
        icon = currentFileDo != null ? ImageUtilities.image2Icon(currentFileDo.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)) : null;
        detail = file.getNameExt();
        scope = Scope.create(null, null, Arrays.asList(file));

        return true;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public Icon getIcon() {
        return icon != null ? icon : super.getIcon();
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
