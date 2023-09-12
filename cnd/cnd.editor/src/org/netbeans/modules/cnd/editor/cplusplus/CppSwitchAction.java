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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class CppSwitchAction extends BaseAction {

    private static final String ACTION_NAME = "cpp-switch-header-source"; // NOI18N
    private static final String ICON = "org/netbeans/modules/cnd/editor/resources/cplusplus/header_source_icon.png"; // NOI18N
    private static CppSwitchAction instance;
    private static final RequestProcessor RP = new RequestProcessor(CppSwitchAction.class.getName(), 1);

    public static synchronized CppSwitchAction getInstance() {
        if (instance == null) {
            instance = new CppSwitchAction();
        }
        return instance;
    }

    private CppSwitchAction() {
        super(ACTION_NAME);
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        putValue(BaseAction.ICON_RESOURCE_PROPERTY, ICON);
        putValue(SHORT_DESCRIPTION, getDefaultShortDescription());
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent txt) {
        DataObject activatedDataObject = getActivatedDataObject();
        if (activatedDataObject != null) {
            FileObject res = findToggleFile(activatedDataObject);
            boolean isToggled = false;
            if (res != null) {
                doToggle(res);
                isToggled = true;
            }
            if (!isToggled) {
                String status;
                switch (getTargetNodeKind(activatedDataObject)) {
                    case HEADER:
                        status = getMessage("cpp-switch-source-not-found"); //NOI18N
                        break;
                    case SOURCE:
                        status = getMessage("cpp-switch-header-not-found"); //NOI18N
                        break;
                    default:
                        status = getMessage("cpp-switch-file-not-found");
                }
                StatusDisplayer.getDefault().setStatusText(status); // NOI18N
            }
        }
    }

    public @Override String getPopupMenuText(JTextComponent target) {
        String trimmedNameKey = "goto-cpp-switch-file"; //NOI18N
        switch (getTargetNodeKind(getActivatedDataObject())) {
            case HEADER:
                trimmedNameKey = "goto-cpp-header-file"; //NOI18N
                break;
            case SOURCE:
                trimmedNameKey = "goto-cpp-source-file"; //NOI18N
                break;
        }
        return getMessage(trimmedNameKey);
    }

    protected @Override Object getDefaultShortDescription() {
        return getMessage("cpp-switch-header-source"); //NOI18N
    }

    @Override
    protected boolean asynchonous() {
        return true;
    }

    // File search functionality

    private enum NodeKind {

        HEADER, SOURCE, UNKNOWN
    }

    private DataObject getActivatedDataObject(){
        DataObject dob = null;
        Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1) {
            dob = activatedNodes[0].getLookup().lookup(DataObject.class);
        }
        if (dob == null) {
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null && WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                dob = activated.getLookup().lookup(DataObject.class);
            }
        }
        return dob;
    }

    private static NodeKind getTargetNodeKind(DataObject dobj) {
        if (dobj != null) {
            FileObject fo = dobj.getPrimaryFile();
            String mime = (fo == null) ? "" : fo.getMIMEType();
            if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
                return NodeKind.SOURCE;
            } else if (MIMENames.isCppOrC(mime)) {
                return NodeKind.HEADER;
            }
        }
        return NodeKind.UNKNOWN;
    }

    private static void doToggle(final DataObject toggled) {
        // check if the data object has possibility to be opened in editor
        final OpenCookie oc = toggled.getLookup().lookup(OpenCookie.class);
        if (oc != null) {
            // remember current caret position
            JTextComponent textComponent = EditorRegistry.lastFocusedComponent();
            JumpList.checkAddEntry(textComponent);
            // try to open ASAP, but better not in EQ
            RP.post(new Runnable() {

                @Override
                public void run() {
                    // open component
                    oc.open();
                }
            }, 0, Thread.MAX_PRIORITY);
        }
    }

    private static void doToggle(FileObject fo) {
        assert (fo != null);
        try {
            // find a data object for the input file object
            DataObject toggled = DataObject.find(fo);
            if (toggled != null) {
                doToggle(toggled);
            }
        } catch (DataObjectNotFoundException ex) {
            // may be error message?
        }
    }

    private static FileObject findToggleFile(DataObject dob) {
        FileObject res = null;
        // check whether current file is C++ Source file
        FileObject fo = dob.getPrimaryFile();
        if (fo != null) {
            String mimeType = FileUtil.getMIMEType(fo, MIMENames.HEADER_MIME_TYPE, MIMENames.CPLUSPLUS_MIME_TYPE, MIMENames.C_MIME_TYPE);
            if (MIMENames.isCppOrC(mimeType)) {
                // it was Source file, find Header
                res = findBrother(dob, MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
            } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
                // check whether current file is Header file
                // try to find C++ Source file
                res = findBrother(dob, MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
                if (res == null) {
                    // try to find C Source file
                    res = findBrother(dob, MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
                }
            }
        }
        return res;
    }

    private static FileObject findBrother(DataObject dob, Collection<String> extensions) {
        assert (dob != null);
        assert (dob.getPrimaryFile() != null);
        if (!extensions.isEmpty()) {
            // get a file object associated with the data object
            FileObject fo = dob.getPrimaryFile();
            FileObject[] childs = fo.getParent().getChildren();

            // try to find a file with the same name and one of passed extensions
            for (String ext : extensions) {
                // use FileUtilities to find brother of the file object
                // FileObject res = FileUtil.findBrother(fo, ext[i]);

                // IZ117750. Netbeans don't recognize MAC FS as case-insensitive
                // so FileObject.getFileObject(name, extension) can create
                // separate FileObjects for name.h and name.H although they are names
                // of the same file. So FileUtil.findBrother can't be used for now.

                String ne = fo.getName() + '.' + ext;
                for (int j = 0; j < childs.length; j++) {
                    FileObject fileObject = childs[j];
                    if ( CndFileUtils.areFilenamesEqual( fileObject.getNameExt(), ne )) {
                        return fileObject;
                    }
                }
            }
        }
        return null;
    }

    // Utility

    private static String getMessage(String key) {
        return NbBundle.getMessage(CppSwitchAction.class, key);
    }
}
