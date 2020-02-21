/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.sendto.output;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 */
public final class OutputPatternsEditSupport {

    private static DataObject cfgFile = null;
    private static final ChangeListener fileChangeListener = new ChangeListener();

    public static void openEditor() {
        if (cfgFile == null) {
            cfgFile = createFile("SendTo-Output-Parsers"); // NOI18N
        }

        if (cfgFile == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        EditCookie cookie = cfgFile.getLookup().lookup(EditCookie.class);
        if (cookie != null) {
            cookie.edit();
        }
    }

    private static DataObject createFile(String name) {
        try {
            FileSystem tfs = FileUtil.createMemoryFileSystem();
            FileObject mroot = tfs.getRoot();
            FileObject afile = mroot.createData(name);
            afile.addFileChangeListener(fileChangeListener);

            List<OutputPattern> patterns = OutputPatterns.getPatterns();
            OutputPatterns.storeToFile(afile, patterns);

            DataObject dob = DataObject.find(afile);
            SaveCookie saveCookie = dob.getLookup().lookup(SaveCookie.class);
            if (saveCookie != null) {
                saveCookie.save();
            }
            return dob;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static class ChangeListener extends FileChangeAdapter {

        @Override
        public void fileChanged(FileEvent fe) {
            try {
                OutputPatterns.setPatterns(OutputPatterns.loadFromStream(fe.getFile().getInputStream()));
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParseException ex) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message("Syntax error in line " + ex.getMessage(),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }
}
