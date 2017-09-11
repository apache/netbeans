/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
