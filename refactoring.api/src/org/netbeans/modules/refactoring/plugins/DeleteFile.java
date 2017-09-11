/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import org.netbeans.modules.refactoring.api.impl.CannotUndoRefactoring;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class DeleteFile extends SimpleRefactoringElementImplementation {
    private final URL res;
    private final String filename;
    private final RefactoringElementsBag session;

    private BackupFacility.Handle id;

    /**
     * 
     * @param fo
     * @param session
     */
    public DeleteFile(FileObject fo, RefactoringElementsBag session) {
        this.res = fo.toURL();
        this.filename = fo.getNameExt();
        this.session = session;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeleteFile", filename);
    }

    @Override
    public String getDisplayText() {
        return getText();
    }

    @Override
    public void performChange() {
        try {
            FileObject fo = URLMapper.findFileObject(res);
            if (fo == null) {
                throw new IOException(res.toString());
            }
            id = BackupFacility.getDefault().backup(fo);
            DataObject.find(fo).delete();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void undoChange() {
        try {
            try {
                File f = Utilities.toFile(res.toURI());
                if (f.exists()) {
                    throw new CannotUndoRefactoring(Collections.singleton(f.getPath()));
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
            id.restore();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public FileObject getParentFile() {
        return URLMapper.findFileObject(res);
    }

    @Override
    public PositionBounds getPosition() {
        return null;
    }
    
}
