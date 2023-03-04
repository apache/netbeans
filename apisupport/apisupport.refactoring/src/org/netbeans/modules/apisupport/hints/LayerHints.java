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

package org.netbeans.modules.apisupport.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.xml.EntityCatalog;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

@MimeRegistration(mimeType="text/x-netbeans-layer+xml", service=UpToDateStatusProviderFactory.class)
public class LayerHints implements UpToDateStatusProviderFactory {

    private static final RequestProcessor RP = new RequestProcessor(LayerHints.class);
    private static final Logger LOG = Logger.getLogger(LayerHints.class.getName());

    public @Override UpToDateStatusProvider createUpToDateStatusProvider(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty); // avoid dep on NbEditorUtilities.getFileObject if possible
        DataObject xml;
        if (sdp instanceof DataObject) {
            xml = (DataObject) sdp;
        } else if (sdp instanceof FileObject) {
            try {
                xml = DataObject.find((FileObject) sdp);
            } catch (DataObjectNotFoundException x) {
                LOG.log(Level.INFO, null, x);
                return null;
            }
        } else {
            return null;
        }
        if (xml.getPrimaryFile().getNameExt().equals("generated-layer.xml")) { // NOI18N
            return null;
        }
        LayerHandle handle = xml.getLookup().lookup(LayerHandle.class);
        if (handle == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(xml.getPrimaryFile());
        if (project == null || project.getLookup().lookup(NbModuleProvider.class) == null) {
            return null;
        }
        return new Prov(doc, xml, handle);
    }

    private static class Prov extends UpToDateStatusProvider implements Runnable {

        private final Document doc;
        private final DataObject xml;
        private final LayerHandle handle;
        private boolean processed;
        private final RequestProcessor.Task task;
        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                change();
            }
        };
        private final PropertyChangeListener pcl = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                    change();
                }
            }
        };
        private void change() {
            processed = false;
            task.schedule(0);
            firePropertyChange(PROP_UP_TO_DATE, null, null);
        }

        Prov(Document doc, DataObject xml, LayerHandle handle) {
            this.doc = doc;
            this.xml = xml;
            this.handle = handle;
            xml.getPrimaryFile().addFileChangeListener(FileUtil.weakFileChangeListener(listener, xml.getPrimaryFile()));
            xml.addPropertyChangeListener(WeakListeners.propertyChange(pcl, xml));
            task = RP.post(this);
        }

        public @Override UpToDateStatus getUpToDate() {
            if (processed) {
                return UpToDateStatus.UP_TO_DATE_OK;
            }
            return processed ? UpToDateStatus.UP_TO_DATE_OK : UpToDateStatus.UP_TO_DATE_PROCESSING;
        }

        private void cancelAll() {
            HintsController.setErrors(doc, LayerHints.class.getName(), Collections.<ErrorDescription>emptyList());
            processed = true;
            firePropertyChange(PROP_UP_TO_DATE, null, null);
        }

        public @Override void run() {
            if (xml.isModified()) {
                cancelAll();
                return;
            }
            FileSystem fs = handle.layer(false);
            if (fs == null) {
                cancelAll();
                return;
            }
            final URL layerURL = handle.getLayerFile().toURL();
            String expectedLayers = "[" + layerURL + "]";
            List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
            RunnableFuture<Map<String,Integer>> linesFuture = new FutureTask<Map<String,Integer>>(new Callable<Map<String,Integer>>() {
                public @Override Map<String,Integer> call() throws Exception {
                    // Adapted from OpenLayerFilesAction.openLayerFileAndFind:
                    final Map<String,Integer> lines = new HashMap<String,Integer>();
                    LOG.log(Level.FINE, "parsing {0}", layerURL);
                    XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                    reader.setContentHandler(new DefaultHandler2() {
                        private Locator locator;
                        private String path;
                        public @Override void setDocumentLocator(Locator l) {
                            locator = l;
                        }
                        public @Override void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                            if (!qname.matches("file|folder")) { // NOI18N
                                return;
                            }
                            String n = attr.getValue("name"); // NOI18N
                            path = path == null ? n : path + '/' + n;
                            lines.put(path, locator.getLineNumber());
                        }
                        public @Override void endElement(String uri, String localname, String qname) throws SAXException {
                            if (!qname.matches("file|folder")) { // NOI18N
                                return;
                            }
                            int slash = path.lastIndexOf('/');
                            path = slash == -1 ? null : path.substring(0, slash);
                        }
                    });
                    reader.setEntityResolver(EntityCatalog.getDefault());
                    reader.parse(layerURL.toString());
                    return lines;
                }
            });
            // Compare AbstractRefactoringPlugin.checkFileObject:
            for (FileObject file : NbCollections.iterable(fs.getRoot().getChildren(true))) {
                if (!expectedLayers.equals(Arrays.toString((URL[]) file.getAttribute("layers")))) {
                    LOG.log(Level.FINE, "skipping {0}", file);
                    continue; // part of generated-layer.xml
                }
                for (Hinter hinter : Lookup.getDefault().lookupAll(Hinter.class)) {
                    try {
                        hinter.process(new Hinter.Context(doc, handle, file, linesFuture, errors));
                    } catch (Exception x) {
                        LOG.log(Level.WARNING, null, x);
                    }
                }
            }
            HintsController.setErrors(doc, LayerHints.class.getName(), errors);
            processed = true;
            firePropertyChange(PROP_UP_TO_DATE, null, null);
        }
        
    }

}
