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
package org.netbeans.modules.php.dbgp.breakpoints;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.util.FileUtils;

import org.netbeans.modules.php.dbgp.ui.DbgpMethodBreakpointPanel;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
public class DbgpFunctionBreakpointType extends BreakpointType {
    private static final String PHP_BREAKPOINT_TYPE = "LBL_PhpType"; // NOI18N
    private static final String METHOD_TYPE = "LBL_MethodBreakpointType"; // NOI18N

    @Override
    public String getCategoryDisplayName() {
        return NbBundle.getMessage(DbgpFunctionBreakpointType.class,
                PHP_BREAKPOINT_TYPE);
    }

    @Override
    public JComponent getCustomizer() {
        return new DbgpMethodBreakpointPanel();
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(DbgpFunctionBreakpointType.class,
                METHOD_TYPE);
    }

    @Override
    public boolean isDefault() {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        if (lastFocusedComponent == null) {
            return false;
        }
        FileObject fileObject = NbEditorUtilities.getFileObject(lastFocusedComponent.getDocument());
        if (fileObject == null) {
            return false;
        }
        return FileUtils.isPhpFile(fileObject);
    }

}
