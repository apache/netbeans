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

package org.netbeans.modules.refactoring.java;

import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.plugins.RenameRefactoringPlugin;
import org.netbeans.spi.java.project.support.ui.PackageRenameHandler;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.project.support.ui.PackageRenameHandler.class)
public class PackageRenameHandlerImpl implements PackageRenameHandler {

    @Override
    public void handleRename(Node node, String newName) {
        DataFolder dob = (DataFolder) node.getCookie(DataObject.class);
        FileObject fo = dob.getPrimaryFile();
        if (node.isLeaf()) {
            //rename empty package and don't try to do any refactoring
            try {
                if (!RefactoringUtils.isValidPackageName(newName)) {
                    String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class,"ERR_InvalidPackage")).format(
                            new Object[] {newName}
                    );
                    
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.INFORMATION_MESSAGE));
                    return;
                }
                ClassPath sourcepath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (sourcepath == null) {
                    throw new IOException("no sourcepath for " + fo);
                }
                FileObject root = sourcepath.findOwnerRoot(fo);
                if (root == null) {
                    throw new IOException(fo + " not in its own sourcepath " + sourcepath);
                }
                FileObject newFolder = FileUtil.createFolder(root, newName.replace('.','/'));
                while (dob.getChildren().length == 0 && dob.isDeleteAllowed() && !dob.getPrimaryFile().equals(newFolder)) {
                    DataFolder parent = dob.getFolder();
                    dob.delete();
                    dob = parent;
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            return;
        }

        InstanceContent ic = new InstanceContent();
        ic.add(node);
        ExplorerContext d = new ExplorerContext();
        d.setNewName(newName);
        ic.add(d);
        final Lookup l = new AbstractLookup(ic);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
                if (Boolean.TRUE.equals(a.getValue("applicable"))) { //NOI18N
                    a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                }
            }
        });
    }
}
