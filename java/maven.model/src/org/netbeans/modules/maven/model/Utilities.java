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

package org.netbeans.modules.maven.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsModelFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 * Utility class to create ModelSource (environment) for the
 * to be created models.
 *
 * copied from xml.retriever and customized.
 * @author mkleint
 */
public class Utilities {

    private Utilities() {}

    private static final Logger logger = Logger.getLogger(Utilities.class.getName());

    /**
     * 
     * @param file
     * @param editable
     * @param skeleton current content of the document
     * @param mimeType
     * @return
     */
    public static ModelSource createModelSourceForMissingFile(File file, boolean editable, String skeleton, String mimeType) {
        try {
            BaseDocument doc = new BaseDocument(false, mimeType);
            doc.insertString(0, skeleton, null);
            InstanceContent ic = new InstanceContent();
            Lookup lookup = new AbstractLookup(ic);
            ic.add(file);
            ic.add(doc);
            ModelSource ms = new ModelSource(lookup, editable);
            return ms;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        assert false : "Failed to load the model for non-existing file";
        return null;
    }
    
    private static BaseDocument getDocument(final DataObject modelSourceDataObject) throws IOException {
        if (modelSourceDataObject != null && modelSourceDataObject.isValid()) {
            EditorCookie ec = modelSourceDataObject.getLookup().lookup(EditorCookie.class);
            assert ec != null : "Data object "+modelSourceDataObject.getPrimaryFile().getPath()+" has no editor cookies.";
            Document doc;
            try {
                doc = ec.openDocument();
            } catch (UserQuestionException uce) {
                // this exception is thrown if the document is to large
                // lets just confirm that it is ok
                uce.confirmed();
                doc = ec.openDocument();
            }
            if (doc instanceof BaseDocument) {
                return (BaseDocument) doc;
            } else {
                logger.log(Level.FINER, "Got document of unexpected {0} from {1}", new Object[] {doc.getClass(), modelSourceDataObject});
                // Replace with a BaseDocument. Mostly useful for unit test.
                final BaseDocument doc2 = new GuardedDocument("text/xml");
                try {
                    String str = doc.getText(0, doc.getLength());
                    doc2.insertString(0, str, null);
                } catch (BadLocationException x) {
                    throw new IOException(x);
                }
                final Document orig = doc;
                doc2.addDocumentListener(new DocumentListener() {
                    public @Override void insertUpdate(DocumentEvent e) {
                        try {
                            orig.insertString(e.getOffset(), doc2.getText(e.getOffset(), e.getLength()), null);
                        } catch (BadLocationException x) {
                            assert false : x;
                        }
                    }
                    public @Override void removeUpdate(DocumentEvent e) {
                        try {
                            orig.remove(e.getOffset(), e.getLength());
                        } catch (BadLocationException x) {
                            assert false : x;
                        }
                    }
                    public @Override void changedUpdate(DocumentEvent e) {}
                });
                doc2.putProperty(Document.StreamDescriptionProperty, doc.getProperty(Document.StreamDescriptionProperty));
                return doc2;
            }
        } else {
            return null;
        }
    }
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     * @param thisFileObj 
     * @return 
     */
    public static ModelSource createModelSource(final FileObject thisFileObj) {
        return createModelSource(thisFileObj, null, null);

    }
    
    public static ModelSource createModelSource(final FileObject thisFileObj, final Document document) {
        if (!(document instanceof BaseDocument)) {
            throw new IllegalArgumentException();
        }
        return createModelSource(thisFileObj, null, (BaseDocument)document);
    }
    
    public static ModelSource createModelSource(final FileObject thisFileObj, final DataObject dobject, final BaseDocument document) {
        assert thisFileObj != null : "Null file object.";
        final File fl = FileUtil.toFile(thisFileObj);
        boolean editable = (fl != null);// && thisFileObj.canWrite();

        Lookup proxyLookup = Lookups.proxy(new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                List<Object> items = new ArrayList<Object>();
                items.add(thisFileObj);
                try {
                    DataObject dobj = dobject != null ? dobject : DataObject.find(thisFileObj);
                    items.add(dobj);
                    BaseDocument doc = document != null ? document : getDocument(dobj);
                    if (doc != null) {
                        items.add(doc);
                    } else {
                        logger.log(Level.WARNING, "no Document found for {0}", dobj);
                    }
                } catch (IOException x) {
                    logger.log(Level.SEVERE, x.getMessage());
                }
                if (fl != null) {
                    items.add(fl);
                }
                return Lookups.fixed(items.toArray());
            }
        });
        return new ModelSource(proxyLookup, editable);
    }

    /**
     * attempts to save the document model to disk.
     * if model is in transaction, the transaction is ended first,
     * then dataobject's SaveCookie is called.
     *
     * @param model
     * @throws java.io.IOException if saving fails.
     */
    public static void saveChanges(AbstractDocumentModel<?> model) throws IOException {
        if (model.isIntransaction()) {
            // the ISE thrown from endTransction is handled in performPOMModelOperations.
            model.endTransaction();
        }
        model.sync();
        DataObject dobj = model.getModelSource().getLookup().lookup(DataObject.class);
        if (dobj == null) {
            final Document doc = model.getModelSource().getLookup().lookup(Document.class);
            final File file = model.getModelSource().getLookup().lookup(File.class);
            logger.log(Level.FINE, "saving changes in {0}", file);
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
                    public @Override void run() throws IOException {
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
                            os.write(text.getBytes(StandardCharsets.UTF_8));
                        } finally {
                            os.close();
                        }
                    }
                });
            } else {
                //TODO report
            }
        } else {
            SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
            if (save != null) {
                logger.log(Level.FINE, "saving changes in {0}", dobj);
                save.save();
            } else {
                logger.log(Level.FINE, "no changes in {0} where modified={1}", new Object[] {dobj, dobj.isModified()});
            }
        }
    }
    
    /**
     * performs model modifying operations on top of the POM model. After modifications,
     * the model is persisted to file.
     * @param pomFileObject
     * @param operations
     */
    public static void performPOMModelOperations(final FileObject pomFileObject, List<? extends ModelOperation<POMModel>> operations) {
        assert pomFileObject != null;
        ModelSource source = Utilities.createModelSource(pomFileObject);
        performPOMModelOperations(source, operations);
    }

    /**
     * performs model modifying operations on top of the POM model. After modifications,
     * the model is persisted to file.
     * @param source
     * @param operations
     * @since 1.36
     */
    public static void performPOMModelOperations(final ModelSource source, List<? extends ModelOperation<POMModel>> operations) {
        assert source != null;
        assert operations != null;
        
        if (source.getLookup().lookup(BaseDocument.class) == null) {
            logger.log(Level.WARNING, "#193187: no Document associated with {0}", getPathFromSource(source));
            return;
        }
        POMModel model = POMModelFactory.getDefault().getModel(source);
        if (model != null) {
            try {
                model.sync();
                if (Model.State.VALID != model.getState()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_POM", NbBundle.getMessage(Utilities.class,"ERR_INVALID_MODEL")), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                    return;
                }
                if (!model.startTransaction()) {
                    logger.log(Level.WARNING, "Could not start transaction on {0}", getPathFromSource(source));
                    return;
                }
                final AtomicBoolean modified = new AtomicBoolean();
                ComponentListener listener = new ComponentListener() {
                    private void change(ComponentEvent evt) {
                        logger.log(Level.FINE, "{0}: {1}", new Object[] {getPathFromSource(source), evt});
                        modified.set(true);
                    }
                    @Override public void valueChanged(ComponentEvent evt) {
                        change(evt);
                    }
                    @Override public void childrenAdded(ComponentEvent evt) {
                        change(evt);
                    }
                    @Override public void childrenDeleted(ComponentEvent evt) {
                        change(evt);
                    }
                };
                model.addComponentListener(listener);
                try {
                    for (ModelOperation<POMModel> op : operations) {
                        op.performOperation(model);
                    }
                    model.endTransaction();
                } finally {
                    model.removeComponentListener(listener);
                }
                if (modified.get()) {
                    Utilities.saveChanges(model);
                } else {
                    logger.log(Level.FINE, "no changes recorded in {0}", getPathFromSource(source));
                }
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_POM", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                logger.log(Level.INFO, "Cannot write POM", ex);
//                Exceptions.printStackTrace(ex);
            } catch (IllegalStateException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_POM", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                logger.log(Level.INFO, "Cannot write POM", ex);
            } finally {
                if (model.isIntransaction()) {
                    model.rollbackTransaction();
                }
            }
        } else {
            logger.log(Level.WARNING, "Cannot create model from current content of {0}", getPathFromSource(source));
        }
    }
    
    private static String getPathFromSource(ModelSource source) {
        File f = source.getLookup().lookup(File.class);
        if (f != null) {
            return f.getAbsolutePath();
        }
        DataObject dob = source.getLookup().lookup(DataObject.class);
        if (dob != null) {
            return dob.getPrimaryFile().getPath();
        }
        return source.toString();
    }

    /**
     * performs model modifying operations on top of the settings.xml model. After modifications,
     * the model is persisted to file.
     * @param settingsFileObject
     * @param operations
     */
    public static void performSettingsModelOperations(FileObject settingsFileObject, List<? extends ModelOperation<SettingsModel>> operations) {
        assert settingsFileObject != null;
        assert operations != null;
        ModelSource source = Utilities.createModelSource(settingsFileObject);
        SettingsModel model = SettingsModelFactory.getDefault().getModel(source);
        if (model != null) {
            try {
                model.sync();
                if (Model.State.VALID != model.getState()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_SETTINGS", NbBundle.getMessage(Utilities.class,"ERR_INVALID_MODEL")), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                    return;
                }
                if (!model.startTransaction()) {
                    logger.log(Level.WARNING, "Could not start transaction on {0}", settingsFileObject);
                    return;
                }
                for (ModelOperation<SettingsModel> op : operations) {
                    op.performOperation(model);
                }
                model.endTransaction();
                Utilities.saveChanges(model);
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_SETTINGS", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                Logger.getLogger(Utilities.class.getName()).log(Level.INFO, "Cannot write settings.xml", ex);
            } catch (IllegalStateException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_SETTINGS", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                Logger.getLogger(Utilities.class.getName()).log(Level.INFO, "Cannot write settings.xml", ex);
            } finally {
                if (model.isIntransaction()) {
                    model.rollbackTransaction();
                }
            }
        } else {
            //TODO report error.. what is the error?
        }
    }

    /**
     * Opens pom at given offset.
     * @param model the model to open
     * @param offset position to open at
     * @since 1.44
     */
    public static  void openAtPosition(final POMModel model, final int offset) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Line line = NbEditorUtilities.getLine(model.getBaseDocument(), offset, false);
                    line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                } catch (IndexOutOfBoundsException e) {
                    logger.log(Level.FINE, "document changed", e);
                }
            }
        });        
    }
    
}
