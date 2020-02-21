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

package org.netbeans.modules.cnd.navigation.switchfiles;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
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

    private static final String actionName = "cpp-switch-header-source"; // NOI18N
    private static final String ICON = "org/netbeans/modules/cnd/navigation/resources/header_source_icon.png"; // NOI18N
    private static CppSwitchAction instance;
    private static final RequestProcessor RP = new RequestProcessor(CppSwitchAction.class.getName(), 1);

    public static synchronized CppSwitchAction getInstance() {
        if (instance == null) {
            instance = new CppSwitchAction();
        }
        return instance;
    }

    public CppSwitchAction() {
        super(actionName);
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
            } else {
                CsmFile target = getTarget(activatedDataObject);
                if (target != null) {
                    DataObject dob = CsmUtilities.getDataObject(target);
                    if (dob != null) {
                        doToggle(dob);
                        isToggled = true;
                    }
                }
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

    private static CsmFile getTarget(DataObject dobj) {
        CsmFile f = CsmUtilities.getCsmFile(dobj, false, false);
        CsmFile target = null;
        if (f != null) {
            if (f.isHeaderFile()) {
                target = findSource(f);
            } else if (f.isSourceFile()) {
                target = findHeader(f);
            }
        }
        return target;
    }

    /*package*/ static CsmFile findHeader(CsmFile source) {
        String name = getName(source.getAbsolutePath().toString());
        // first look at the list of includes
        for (CsmInclude h : source.getIncludes()) {
            if (h.getIncludeFile() != null 
                    && CndFileUtils.areFilenamesEqual( name, getName(h.getIncludeFile().getAbsolutePath().toString())) ) {
                return h.getIncludeFile();
            }
        }

        String path = trimExtension(source.getAbsolutePath().toString());
        CsmFile namesake = null;
        for (CsmFile f : source.getProject().getHeaderFiles()) {
            if (CndFileUtils.areFilenamesEqual( getName(f.getAbsolutePath().toString()), name)) {
                if (CndFileUtils.areFilenamesEqual( path, trimExtension(f.getAbsolutePath().toString()) )) {
                    // we got namesake in the same directory. Best hit.
                    // TODO: actually this is pretty common issue, should we
                    // make a special check for such files?
                    return f;
                }
                if (namesake == null) {
                    // we don't care which namesake to take if it's not in
                    // the same directory
                    namesake = f;
                }
            }
        }
        if (namesake == null) {
            Collection<CsmFile> includers = CsmIncludeHierarchyResolver.getDefault().getFiles(source);

            for (CsmFile f : includers) {
                if (CndFileUtils.areFilenamesEqual( getName(f.getAbsolutePath().toString()), name)) {
                    if (CndFileUtils.areFilenamesEqual( path, trimExtension(f.getAbsolutePath().toString()) )) {
                        return f;
                    }
                    if (namesake == null) {
                        namesake = f;
                    }
                }
            }
        }
        return namesake;
    }

    /*package*/ static CsmFile findSource(CsmFile header) {
        String name = getName(header.getAbsolutePath().toString());

        Collection<CsmFile> includers = CsmIncludeHierarchyResolver.getDefault().getFiles(header);

        for (CsmFile f : includers) {
            if (CndFileUtils.areFilenamesEqual( getName(f.getAbsolutePath().toString()), name )) {
                // we found source file with the same name
                // as header and with dependency to it. Best shot.
                return f;
            }
        }

        // look for random namesake
        for (CsmFile f : header.getProject().getSourceFiles()) {
            if (CndFileUtils.areFilenamesEqual( getName(f.getAbsolutePath().toString()), name )) {
                return f;
            }
        }

        return null;
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
    
    private static String getName(String path) {
        String name = CndPathUtilities.getBaseName(path);
        int idxDot = name.lastIndexOf('.');
        return name.substring(0, idxDot == -1 ? name.length() : idxDot);
    }

    private static String trimExtension(String path) {
        // I hardly believe we can meet file w/o extension here but lets play safe
        int idxDot = path.lastIndexOf('.');
        return idxDot == -1 ? path : path.substring(0, idxDot);
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(CppSwitchAction.class, key);
    }
}
