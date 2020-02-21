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
