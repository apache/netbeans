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
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Various model utilities methods.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JSFConfigModelUtilities {

    private static final Logger LOGGER = Logger.getLogger(JSFConfigModelUtilities.class.getName());

    private JSFConfigModelUtilities() {
    }

    /**
     * attempts to save the document model to disk.
     * if model is in transaction, the transaction is ended first,
     * then dataobject's SaveCookie is called.
     *
     * @param model
     * @throws java.io.IOException if saving fails.
     */
    public static void saveChanges(DocumentModel<?> model) throws IOException {
        if (model.isIntransaction()) {
            try {
                model.endTransaction();
            } catch (IllegalStateException ex) {
                IOException io = new IOException("Cannot save faces config", ex);
                throw Exceptions.attachLocalizedMessage(io,
                        NbBundle.getMessage(JSFConfigModelUtilities.class, "ERR_Save_FacesConfig",
                        Exceptions.findLocalizedMessage(ex)));
            }
        }
        model.sync();
        DataObject dobj = model.getModelSource().getLookup().lookup(DataObject.class);
        if (dobj == null) {
            final Document doc = model.getModelSource().getLookup().lookup(Document.class);
            final File file = model.getModelSource().getLookup().lookup(File.class);
            LOGGER.log(Level.FINE, "saving changes in {0}", file);
            File parent = file.getParentFile();
            FileObject parentFo = FileUtil.toFileObject(parent);
            if (parentFo == null) {
                parent.mkdirs();
                FileUtil.refreshFor(parent);
                parentFo = FileUtil.toFileObject(parent);
            }
            final FileObject fParentFo = parentFo;
            if (fParentFo != null) {
                FileSystem fs = parentFo.getFileSystem();
                fs.runAtomicAction(new FileSystem.AtomicAction() {

                    @Override
                    public void run() throws IOException {
                        String text;
                        try {
                            text = doc.getText(0, doc.getLength());
                        } catch (BadLocationException x) {
                            throw new IOException(x);
                        }
                        FileObject fo = fParentFo.getFileObject(file.getName());
                        if (fo == null) {
                            fo = fParentFo.createData(file.getName());
                        }
                        OutputStream os = fo.getOutputStream();
                        try {
                            os.write(text.getBytes(FileEncodingQuery.getEncoding(fo)));
                        } finally {
                            os.close();
                        }
                    }
                });
            }
        } else {
            SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
            if (save != null) {
                LOGGER.log(Level.FINE, "saving changes in {0}", dobj);
                save.save();
            } else {
                LOGGER.log(Level.FINE, "no changes in {0}", dobj);
            }
        }
    }

    /**
     * Do runnable within model transaction.
     *
     * @param model model where to run
     * @param job runnable to do
     * @return {@code true} if the run was successful, {@code false} otherwise
     */
    public static boolean doInTransaction(JSFConfigModel model, Runnable job) {
        model.startTransaction();
        try {
            job.run();
        } finally {
            try {
                model.endTransaction();
            } catch (IllegalStateException ex) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(JSFConfigModelUtilities.class, "ERR_UpdateFacesConfigModel", //NOI18N
                        Exceptions.findLocalizedMessage(ex)));
                return false;
            }
        }
        return true;
    }

}
