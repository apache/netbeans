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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vita Stejskal
 */
@MimeLocation(subfolderName=ToolbarActionsProvider.TOOLBAR_ACTIONS_FOLDER_NAME, instanceProviderClass=ToolbarActionsProvider.class)
public final class ToolbarActionsProvider extends ActionsList implements InstanceProvider<ToolbarActionsProvider> {

    private static final Logger LOG = Logger.getLogger(ToolbarActionsProvider.class.getName());
    
    static final String TOOLBAR_ACTIONS_FOLDER_NAME = "Toolbars/Default"; //NOI18N
    private static final String TEXT_BASE_PATH = "Editors/text/base/"; //NOI18N
    
    public static List getToolbarItems(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        ActionsList provider;
        if (mimeType.equals("text/base")) { //NOI18N
            provider = MimeLookup.getLookup(mimePath).lookup(LegacyToolbarActionsProvider.class);
        } else {
            provider = MimeLookup.getLookup(mimePath).lookup(ToolbarActionsProvider.class);
        }
        return provider == null ? Collections.emptyList() : provider.getAllInstances();
    }
    
    public ToolbarActionsProvider() {
        super(null, false, false);
    }

    private ToolbarActionsProvider(List<FileObject> keys) {
        super(keys, true, false);
    }
    
    public ToolbarActionsProvider createInstance(List<FileObject> fileObjectList) {
        return new ToolbarActionsProvider(fileObjectList);
    }
    
    // XXX: This is here to help NbEditorToolbar to deal with legacy code
    // that registered toolbar actions in text/base. The artificial text/base
    // mime type is deprecated and should not be used anymore.
    @MimeLocation(subfolderName=TOOLBAR_ACTIONS_FOLDER_NAME, instanceProviderClass=LegacyToolbarActionsProvider.class)
    public static final class LegacyToolbarActionsProvider extends ActionsList implements InstanceProvider<LegacyToolbarActionsProvider> {

        public LegacyToolbarActionsProvider() {
            this(null);
        }

        private LegacyToolbarActionsProvider(List<FileObject> keys) {
            super(keys, false, false);
        }

        public LegacyToolbarActionsProvider createInstance(List<FileObject> fileObjectList) {
            ArrayList<FileObject> textBaseFilesList = new ArrayList<FileObject>();

            for(Object o : fileObjectList) {
                FileObject fileObject = null;

                if (o instanceof DataObject) {
                    fileObject = ((DataObject) o).getPrimaryFile();
                } else if (o instanceof FileObject) {
                    fileObject = (FileObject) o;
                } else {
                    continue;
                }

                String fullPath = fileObject.getPath();
                int idx = fullPath.lastIndexOf(TOOLBAR_ACTIONS_FOLDER_NAME);
                assert idx != -1 : "Expecting files with '" + TOOLBAR_ACTIONS_FOLDER_NAME + "' in the path: " + fullPath; //NOI18N

                String path = fullPath.substring(0, idx);
                if (TEXT_BASE_PATH.equals(path)) {
                    textBaseFilesList.add(fileObject);
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("The 'text/base' mime type is deprecated, please move your file to the root. Offending file: " + fullPath); //NOI18N
                    }
                }
            }

            return new LegacyToolbarActionsProvider(textBaseFilesList);
        }
    } // End of LegacyToolbarActionsProvider class
}
