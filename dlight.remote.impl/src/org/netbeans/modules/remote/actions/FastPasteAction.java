/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.RemoteExceptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 */
public class FastPasteAction extends CookieAction {

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class<?>[]{ DataFolder.class };
    }

    @Override
    protected void performAction(Node[] nodes) {
        Set<FileObject> fileObjects = new HashSet<>();
        Transferable rf = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        DataFlavor[] flavors = rf.getTransferDataFlavors();
        for (DataFlavor fl : flavors) {
            if (fl.getRepresentationClass().isAssignableFrom(Node.class)) {
                try {
                    Object transferData = rf.getTransferData(fl);
                    if (transferData instanceof Node) {
                        DataObject dao = ((Node) transferData).getLookup().lookup(DataObject.class);
                        if (dao != null) {
                            FileObject fo = dao.getPrimaryFile();
                            if (fo != null) {
                                fileObjects.add(fo);
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    RemoteLogger.fine(ex);
                }
            }
        }
        if (!fileObjects.isEmpty()) {
            for (Node n : nodes) {
                DataFolder df = n.getLookup().lookup(DataFolder.class);
                if (df != null) {
                    FileObject targetFolder = df.getPrimaryFile();
                    for (FileObject fo : fileObjects) {
                        try {
                            testNesting(fo, targetFolder);
                            String suffix = existInFolder(fo, targetFolder);
                            String newName = fo.getName() + suffix;
                            fo.copy(targetFolder, newName, fo.getExt());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }

    // copy-paste from MultyDataObject.existInFolder
    // XXX does nothing of the sort --jglick
    /** Check if in specific folder exists fileobject with the same name.
    * If it exists user is asked for confirmation to rewrite, rename or cancel operation.
    * @param folder destination folder
    * @return the suffix which should be added to the name or null if operation is cancelled
    */
    private String existInFolder(FileObject fo, FileObject folder) {
        String orig = fo.getName ();
        String name = FileUtil.findFreeFileName(folder, orig, fo.getExt ());
        if (name.length () <= orig.length ()) {
            return ""; // NOI18N
        } else {
            return name.substring (orig.length ());
        }
    }
    
    private void testNesting(FileObject folder, FileObject targetFolder) throws IOException {
        if (targetFolder.equals(folder)) {
            throw RemoteExceptions.createIOException(
                    NbBundle.getMessage(FastPasteAction.class, "EXC_CannotCopyTheSame", folder.getNameExt()));
        } else {
            FileObject testFolder = targetFolder.getParent();
            while (testFolder != null) {
                if (testFolder.equals(folder)) {
                    throw RemoteExceptions.createIOException(
                            NbBundle.getMessage(FastPasteAction.class, "EXC_CannotCopySubfolder", folder.getNameExt()));
                }
                testFolder = testFolder.getParent();
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return super.enable(activatedNodes) && clipboardHasFolders();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && clipboardHasFolders();
    }

    private boolean clipboardHasFolders() {
        Transferable tfr = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        DataFlavor[] flavors = tfr.getTransferDataFlavors();
        for (DataFlavor fl : flavors) {
            if (fl.getRepresentationClass().isAssignableFrom(Node.class)) {
                try {
                    Object transferData = tfr.getTransferData(fl);
                    if (transferData instanceof Node) {
                        DataObject dao = ((Node) transferData).getLookup().lookup(DataObject.class);
                        if (dao != null) {
                            FileObject fo = dao.getPrimaryFile();
                            if (fo.isFolder()) {
                                return true;
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    RemoteLogger.fine(ex);
                }
            }
        }
        return false;
    }


    @Override
    public String getName() {
        return NbBundle.getMessage(FastPasteAction.class, "LAB_FastPaste");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.remote.actions.PasteAction"); //NOI18N
    }

}
