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
package org.netbeans.modules.refactoring.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class FileMovePlugin implements RefactoringPlugin {
    private MoveRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        URL targetUrl = ((MoveRefactoring) refactoring).getTarget().lookup(URL.class);        
        if(targetUrl != null) {
            for (FileObject o: refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
                elements.addFileChange(refactoring, new MoveFile(o, elements));
            }
        }
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }
        
    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
    }
    
    private class MoveFile extends SimpleRefactoringElementImplementation {
        
        private FileObject fo;
        public MoveFile(FileObject fo, RefactoringElementsBag session) {
            this.fo = fo;
        }
        @Override
        public String getText() {
            return NbBundle.getMessage(FileMovePlugin.class, "TXT_MoveFile", fo.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }
        
        DataFolder sourceFolder;
        DataObject source;
        @Override
        public void performChange() {
            try {
                FileObject target = FileHandlingFactory.getOrCreateFolder(refactoring.getTarget().lookup(URL.class));
                DataFolder targetFolder = DataFolder.findFolder(target);
                if (fo==null) {
                    Logger.getLogger(FileMovePlugin.class.getName()).severe("Invalid FileObject\n. File not found.");
                    return;
                } else if (!fo.isValid()) {
                    String path = FileUtil.getFileDisplayName(fo);
                    Logger.getLogger(FileMovePlugin.class.getName()).log(Level.FINE, "Invalid FileObject {0}.\n Trying to recreate...", path);
                    fo = FileUtil.toFileObject(FileUtil.toFile(fo));
                    if (fo==null) {
                        Logger.getLogger(FileMovePlugin.class.getName()).log(Level.SEVERE, "Invalid FileObject {0}.\n File not found.", path);
                        return;
                    }
                }
                source = DataObject.find(fo);
                sourceFolder = source.getFolder();
                source.move(targetFolder);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public void undoChange() {
            try {
                source.move(sourceFolder);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return fo;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }
    }
}
