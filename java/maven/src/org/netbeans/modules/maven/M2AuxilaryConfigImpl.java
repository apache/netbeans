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

package org.netbeans.modules.maven;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NonNull;
import static org.netbeans.modules.maven.Bundle.*;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.ProjectProblem;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * implementation of AuxiliaryConfiguration that relies on FileObject's attributes
 * for the non shared elements and on ${basedir}/nb-configuration file for share ones.
 * @author mkleint
 */
public class M2AuxilaryConfigImpl implements AuxiliaryConfiguration {
    public static final String BROKEN_NBCONFIG = "BROKENNBCONFIG"; //NOI18N

    private static final String AUX_CONFIG = "AuxilaryConfiguration"; //NOI18N
    public static final String CONFIG_FILE_NAME = "nb-configuration.xml"; //NOI18N

    private static final Logger LOG = Logger.getLogger(M2AuxilaryConfigImpl.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(M2AuxilaryConfigImpl.class);
    private static final int SAVING_DELAY = 100;
    private RequestProcessor.Task savingTask;
    private Document scheduledDocument;
    private Document cachedDoc;
    private static final Document DELETED_FILE_DOCUMENT = XMLUtil.createDocument(AUX_CONFIG, null, null, null);
    private static final Document BROKEN_DOCUMENT = XMLUtil.createDocument(AUX_CONFIG, null, null, null);
    private final Object configIOLock = new Object();
    private final FileObject projectDirectory;
    private ProblemProvider pp;
    private final FileChangeAdapter fileChange;
    private final AtomicBoolean fileChangeSet = new AtomicBoolean(false);
    
    public M2AuxilaryConfigImpl(FileObject dir, boolean longtermInstance) {
        this.projectDirectory = dir;
        
        if (longtermInstance) {
            pp = new ProblemProvider();
            fileChange = new FileChangeAdapter() {

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    if (CONFIG_FILE_NAME.equals(fe.getName() + "." + fe.getExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    if (CONFIG_FILE_NAME.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    if (CONFIG_FILE_NAME.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    if (CONFIG_FILE_NAME.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }
            };
            
        savingTask = RP.create(new Runnable() {
            public @Override void run() {
                try {
                    projectDirectory.getFileSystem().runAtomicAction(new AtomicAction() {
                        public @Override void run() throws IOException {
                            Document doc;
                            synchronized (M2AuxilaryConfigImpl.this) {
                                doc = scheduledDocument;
                                if (doc == null) {
                                    return;
                                }
                                scheduledDocument = null;
                            }
                            synchronized (configIOLock) {
                                FileObject config = projectDirectory.getFileObject(CONFIG_FILE_NAME);
                                if (doc.getDocumentElement().getElementsByTagName("*").getLength() > 0) {
                                    OutputStream out = config == null ? projectDirectory.createAndOpen(CONFIG_FILE_NAME) : config.getOutputStream();
                                    LOG.log(Level.FINEST, "Write configuration file for {0}", projectDirectory);
                                    try {
                                        XMLUtil.write(doc, out, "UTF-8"); //NOI18N
                                    } finally {
                                        out.close();
                                    }
                                } else if (config != null) {
                                    LOG.log(Level.FINEST, "Delete empty configuration file for {0}", projectDirectory);
                                    config.delete();
                                }
                            }
                        }
                    });
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "IO Error while saving " + projectDirectory.getFileObject(CONFIG_FILE_NAME), ex);
                }
            }
        });
        } else {
            fileChange = null;
            fileChangeSet.set(true);
        }
    }
    
    private synchronized void resetCache() {
        cachedDoc = null;
    }
    
    
    public ProjectProblemsProvider getProblemProvider() {
        return pp;
    }

    private Document loadConfig(FileObject config) throws IOException, SAXException {
        synchronized (configIOLock) {
            return XMLUtil.parse(new InputSource(config.toURL().toString()), false, true, null, null);
        }
    }

    public @Override Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        Element e = doGetConfigurationFragment(elementName, namespace, shared);
        return e != null ? cloneSafely(e) : null;
    }
    // Copied from AntProjectHelper.
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Element cloneSafely(Element el) { // #190845
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            Document dummy = db.newDocument();
            return (Element) dummy.importNode(el, true);
        }
    }
    @Messages({
        "TXT_Problem_Broken_Config=Broken nb-configuration.xml file.",
        "# {0} - parser error message", 
        "DESC_Problem_Broken_Config=The $project_basedir/nb-configuration.xml file cannot be parsed. "
            + "The information contained in the file will be ignored until fixed. "
            + "This affects several features in the IDE that will not work properly as a result.\n\n "
            + "The parsing exception follows:\n{0}",
        "TXT_Problem_Broken_Config2=Duplicate entries found in nb-configuration.xml file.",
        "DESC_Problem_Broken_Config2=The $project_basedir/nb-configuration.xml file contains some elements multiple times. "
            + "That can happen when concurrent changes get merged by version control for example. The IDE however cannot decide which one to use. "
            + "So until the problem is resolved manually, the affected configuration will be ignored."
    })
    private synchronized Element doGetConfigurationFragment(final String elementName, final String namespace, boolean shared) {
        lazyAttachListener();
        if (shared) {
            //first check the document schedule for persistence
            if (scheduledDocument != null) {
                try {
                    Element el = XMLUtil.findElement(scheduledDocument.getDocumentElement(), elementName, namespace);
                    if (el != null) {
                        el = (Element) el.cloneNode(true);
                    }
                    return el;
                } catch (IllegalArgumentException iae) {
                    //thrown from XmlUtil.findElement when more than 1 equal elements are present.
                    LOG.log(Level.INFO, iae.getMessage(), iae);
                }
            }
            if (cachedDoc == null) {
                final FileObject config = projectDirectory.getFileObject(CONFIG_FILE_NAME);
                if (config != null) {
                    // we need to re-read the config file..
                    try {
                        Document doc = loadConfig(config);
                        cachedDoc = doc;
                        if (pp != null) {
                            pp.setProblem(null);
                            findDuplicateElements(doc.getDocumentElement(), pp, config);
                        }
                        return XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
                    } catch (final SAXException ex) {
                        if (pp != null) {
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    pp.setProblem(ProjectProblem.createWarning(
                                        TXT_Problem_Broken_Config(),
                                        DESC_Problem_Broken_Config(ex.getMessage()),
                                        new ProblemReporterImpl.MavenProblemResolver(ProblemReporterImpl.createOpenFileAction(config), BROKEN_NBCONFIG)));
                                }
                            });
                        }
                        LOG.log(Level.INFO, ex.getMessage(), ex);
                        cachedDoc = BROKEN_DOCUMENT;
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "IO Error while loading " + config.getPath(), ex);
                        cachedDoc = BROKEN_DOCUMENT;
                    } catch (IllegalArgumentException iae) {
                        //thrown from XmlUtil.findElement when more than 1 equal elements are present.
                        LOG.log(Level.INFO, iae.getMessage(), iae);
                    }
                    return null;
                } else {
                    // no file.. remove possible cache
                    cachedDoc = DELETED_FILE_DOCUMENT;
                    return null;
                }
            } else {
                if (cachedDoc == DELETED_FILE_DOCUMENT || cachedDoc == BROKEN_DOCUMENT) {
                    return null;
                }
                //reuse cached value if available;
                try {
                    return XMLUtil.findElement(cachedDoc.getDocumentElement(), elementName, namespace);
                } catch (IllegalArgumentException iae) {
                    //thrown from XmlUtil.findElement when more than 1 equal elements are present.
                    LOG.log(Level.INFO, iae.getMessage(), iae);
                }
            }
            return null;
        } else {
            String str = (String) projectDirectory.getAttribute(AUX_CONFIG);
            if (str != null) {
                Document doc;
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                    return XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
                } catch (SAXException ex) {
                    LOG.log(Level.FINE, "cannot parse", ex);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error reading private auxiliary configuration", ex);
                }
            }
            return null;
        }
    }

    private void lazyAttachListener() {
        if (fileChangeSet.compareAndSet(false, true)) {
            projectDirectory.addFileChangeListener(FileUtil.weakFileChangeListener(fileChange, projectDirectory));
        }
    }

    public @Override synchronized void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
        lazyAttachListener();
        Document doc = null;
        if (shared) {
            if (scheduledDocument != null) {
                doc = scheduledDocument;
            } else {
                FileObject config = projectDirectory.getFileObject(CONFIG_FILE_NAME);
                if (config != null) {
                    try {
                        doc = loadConfig(config);
                    } catch (SAXException ex) {
                        LOG.log(Level.INFO, "Cannot parse file " + config.getPath(), ex);
                        if (config.getSize() == 0) {
                            //something got wrong in the past..
                            doc = createNewSharedDocument();
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "IO Error with " + config.getPath(), ex);
                    }
                } else {
                    doc = createNewSharedDocument();
                }
            }
        } else {
            String str = (String) projectDirectory.getAttribute(AUX_CONFIG);
            if (str != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                } catch (SAXException ex) {
                    LOG.log(Level.FINE, "cannot parse", ex);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error reading private auxiliary configuration", ex);
                }
            }
            if (doc == null) {
                String element = "project-private"; // NOI18N
                doc = XMLUtil.createDocument(element, null, null, null);
            }
        }
        if (doc != null) {
            Element el = XMLUtil.findElement(doc.getDocumentElement(), fragment.getNodeName(), fragment.getNamespaceURI());
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            doc.getDocumentElement().appendChild(doc.importNode(fragment, true));

            if (shared) {
                if (scheduledDocument == null) {
                    scheduledDocument = doc;
                }
                LOG.log(Level.FINEST, "Schedule saving of configuration fragment for " + projectDirectory, new Exception());
                savingTask.schedule(SAVING_DELAY);
            } else {
                try {
                    ByteArrayOutputStream wr = new ByteArrayOutputStream();
                    XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                    projectDirectory.setAttribute(AUX_CONFIG, wr.toString("UTF-8"));
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error writing private auxiliary configuration", ex);
                }
            }
        }

    }

    public @Override synchronized boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        lazyAttachListener();
        Document doc = null;
        FileObject config = projectDirectory.getFileObject(CONFIG_FILE_NAME);
        if (shared) {
            if (scheduledDocument != null) {
                doc = scheduledDocument;
            } else {
                if (config != null) {
                    try {
                        try {
                            doc = loadConfig(config);
                        } catch (SAXException ex) {
                            LOG.log(Level.INFO, "Cannot parse file " + config.getPath(), ex);
                            if (config.getSize() == 0) {
                                //just delete the empty file, something got wrong a while back..
                                config.delete();
                            }
                            return true;
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "IO Error with " + config.getPath(), ex);
                    }
                } else {
                    return false;
                }
            }
        } else {
            String str = (String) projectDirectory.getAttribute(AUX_CONFIG);
            if (str != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                return false;
            }
        }
        if (doc != null) {
            Element el = XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            if (shared) {
                if (scheduledDocument == null) {
                    scheduledDocument = doc;
                }
                LOG.log(Level.FINEST, "Schedule saving of configuration fragment for " + projectDirectory, new Exception());
                savingTask.schedule(SAVING_DELAY);
            } else {
                try {
                    ByteArrayOutputStream wr = new ByteArrayOutputStream();
                    XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                    projectDirectory.setAttribute(AUX_CONFIG, wr.toString("UTF-8"));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return true;
    }

    private Document createNewSharedDocument() throws DOMException {
        String element = "project-shared-configuration";
        Document doc = XMLUtil.createDocument(element, null, null, null);
        doc.getDocumentElement().appendChild(doc.createComment(
                "\nThis file contains additional configuration written by modules in the NetBeans IDE.\n" +
                "The configuration is intended to be shared among all the users of project and\n" +
                "therefore it is assumed to be part of version control checkout.\n" +
                "Without this configuration present, some functionality in the IDE may be limited or fail altogether.\n"));
        return doc;
    }
    
    static void findDuplicateElements(@NonNull Element parent, @NonNull ProblemProvider pp, FileObject config) {
        NodeList l = parent.getChildNodes();
        int nodeCount = l.getLength();
        Set<String> known = new HashSet<String>();
        for (int i = 0; i < nodeCount; i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Node node = l.item(i);
                String localName = node.getLocalName();
                localName = localName == null ? node.getNodeName() : localName;
                String id = localName + "|" + node.getNamespaceURI();
                if (!known.add(id)) {
                    //we have a duplicate;
                    pp.setProblem(ProjectProblem.createWarning(
                                    TXT_Problem_Broken_Config2(), 
                                    DESC_Problem_Broken_Config2(), 
                                    new ProblemReporterImpl.MavenProblemResolver(ProblemReporterImpl.createOpenFileAction(config), BROKEN_NBCONFIG)));
                }
            }
        }
    }    
    
    private class ProblemProvider implements ProjectProblemsProvider {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private ProjectProblem pp;

        public ProblemProvider() {
        }
        
        void setProblem(ProjectProblem pp) {
            this.pp = pp;
            if (pp == null && this.pp == null) {
                return; //ignore this case, dont' fire change..
            }
            pcs.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public Collection<? extends ProjectProblem> getProblems() {
            if (pp != null) {
                return Collections.singleton(pp);
            } else {
                return Collections.emptyList();
            }
        }
        
    }
}
