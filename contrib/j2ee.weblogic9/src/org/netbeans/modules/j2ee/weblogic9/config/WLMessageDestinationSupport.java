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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.weblogic9.dd.model.BaseDescriptorModel;
import org.netbeans.modules.j2ee.weblogic9.dd.model.MessageModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class WLMessageDestinationSupport {

    private static final String JMS_FILE = "-jms.xml"; // NOI18N

    private static final String NAME_PATTERN = "message-"; // NOI18N

    private static final FileFilter JMS_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isDirectory() && pathname.getName().endsWith(JMS_FILE);
        }
    };

    private static final Logger LOGGER = Logger.getLogger(WLMessageDestinationSupport.class.getName());

    private final File resourceDir;
    
    private final Version version;

    public WLMessageDestinationSupport(File resourceDir, Version version) {
        assert resourceDir != null : "Resource directory can't be null"; // NOI18N
        this.resourceDir = FileUtil.normalizeFile(resourceDir);
        this.version = version;
    }

    static Set<WLMessageDestination> getMessageDestinations(File domain,
            FileObject inputFile, boolean systemDefault) throws ConfigurationException {
        if (inputFile == null || !inputFile.isValid() || !inputFile.canRead()) {
            if (LOGGER.isLoggable(Level.INFO) && inputFile != null) {
                LOGGER.log(Level.INFO, NbBundle.getMessage(WLMessageDestinationSupport.class, "ERR_WRONG_CONFIG_DIR", inputFile));
            }
            return Collections.emptySet();
        }
        // domain config
        if (inputFile.isData() && inputFile.hasExt("xml")) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                JmsHandler handler = new JmsHandler(domain);
                parser.parse(new BufferedInputStream(inputFile.getInputStream()), handler);

                Map<File, Boolean> confs = new HashMap<File, Boolean>();
                Set<String> nameOnly = new HashSet<String>();

                // load by path in config.xml
                for (JmsResource resource : handler.getResources()) {
                    // FIXME check target
                    if (resource.getFile() != null) {
                        File config = resource.resolveFile();
                        if (config != null) {
                            confs.put(config, resource.isSystem());
                        }
                    } else if (resource.getResourceName() != null && resource.isSystem()) {
                        nameOnly.add(resource.getResourceName());
                    }
                }

                Set<WLMessageDestination> result = new HashSet<WLMessageDestination>();
                result.addAll(getMessageDestinations(confs));

                // load those in config/jms by name
                if (!nameOnly.isEmpty()) {
                    Set<WLMessageDestination> configMessageDestinations =
                            getMessageDestinations(domain, inputFile.getParent().getFileObject("jms"), true); // NOI18N
                    for (WLMessageDestination ds : configMessageDestinations) {
                        if (nameOnly.contains(ds.getResourceName())) {
                            result.add(ds);
                        }
                    }
                }

                return result;
            } catch (IOException ex) {
                return Collections.emptySet();
            } catch (ParserConfigurationException ex) {
                return Collections.emptySet();
            } catch (SAXException ex) {
                return Collections.emptySet();
            }
        // directory project
        } else if (inputFile.isFolder()) {
            File file = FileUtil.toFile(inputFile);
            Map<File, Boolean> confs = new HashMap<File, Boolean>();
            for (File jdbcFile : file.listFiles(JMS_FILE_FILTER)) {
                confs.put(jdbcFile, systemDefault);
            }

            if (confs.isEmpty()) { // nowhere to search
                return Collections.emptySet();
            }

            return getMessageDestinations(confs);
        }
        return Collections.emptySet();
    }

    private static Set<WLMessageDestination> getMessageDestinations(Map<File, Boolean> confs) throws ConfigurationException {
        Set<WLMessageDestination> messageDestinations = new HashSet<WLMessageDestination>();

        for (Map.Entry<File, Boolean> entry : confs.entrySet()) {
            File jmsFile = entry.getKey();
            try {
                MessageModel messageModel = null;
                try {
                    messageModel = MessageModel.forFile(jmsFile);
                } catch (RuntimeException re) {
                    String msg = NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_NotParseableMessages", jmsFile.getAbsolutePath());
                    LOGGER.log(Level.INFO, msg);
                    continue;
                }

                // TODO should we offer local only MD ?
                // Does it makes sense?
                for (MessageModel.MessageDestination dest : messageModel.getMessageDestinations(Type.QUEUE, false)) {
                    messageDestinations.add(new WLMessageDestination(
                            dest.getResourceName(), dest.getJndiName(),
                            Type.QUEUE, jmsFile, entry.getValue()));
                }
                for (MessageModel.MessageDestination dest : messageModel.getMessageDestinations(Type.TOPIC, false)) {
                    messageDestinations.add(new WLMessageDestination(
                            dest.getResourceName(), dest.getJndiName(),
                            Type.TOPIC, jmsFile, entry.getValue()));
                }
            } catch (IOException ioe) {
                String msg = NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_CannotReadMessages", jmsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, ioe);
                throw new ConfigurationException(msg, ioe);
            } catch (RuntimeException re) {
                String msg = NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_NotParseableMessages", jmsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, re);
                throw new ConfigurationException(msg, re);
            }
        }

        return messageDestinations;
    }

    public Set<WLMessageDestination> getMessageDestinations() throws ConfigurationException {
        FileObject resource = FileUtil.toFileObject(resourceDir);

        return getMessageDestinations(null, resource, false);
    }

    public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(final String resourceName,
            final String jndiName, final Type type) throws ConfigurationException {

        WLMessageDestination destination = modifyMessageDestination(new MessageDestinationModifier() {

            @Override
            public ModifiedMessageDestination modify(Set<WLMessageDestination> destinations) throws ConfigurationException {
                for (WLMessageDestination destination : destinations) {
                    // TODO is jndi check correct and needed
                    if (resourceName.equals(destination.getResourceName()) || jndiName.equals(destination.getName())) {
                        throw new ConfigurationException(NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_MessageDestinationAlreadyExists"));
                    }
                }

                // create the datasource
                ensureResourceDirExists();

                // TODO use single file
                File candidate;
                int counter = 1;
                do {
                    candidate = new File(resourceDir, NAME_PATTERN
                            + counter + JMS_FILE);
                    counter++;
                } while (candidate.exists());

                MessageModel model = MessageModel.generate(version);
                model.addMessageDestination(
                        new MessageModel.MessageDestination(resourceName, jndiName, type));

                try {
                    writeFile(candidate, model);
                } catch (ConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return new ModifiedMessageDestination(
                        candidate, model, new WLMessageDestination(resourceName, jndiName, type, candidate, false));
            }
        });

        return destination;
    }

    private WLMessageDestination modifyMessageDestination(MessageDestinationModifier modifier)
            throws ConfigurationException {

        try {
            ensureResourceDirExists();

            FileObject resourceDirObject = FileUtil.toFileObject(resourceDir);
            assert resourceDirObject != null;

            Map<WLMessageDestination, DataObject> destinations = new LinkedHashMap<WLMessageDestination, DataObject>();
            for (FileObject dsFileObject : resourceDirObject.getChildren()) {
                if (dsFileObject.isData() && dsFileObject.getNameExt().endsWith(JMS_FILE)) {

                    DataObject datasourceDO = DataObject.find(dsFileObject);

                    EditorCookie editor = (EditorCookie) datasourceDO.getCookie(EditorCookie.class);
                    StyledDocument doc = editor.getDocument();
                    if (doc == null) {
                        doc = editor.openDocument();
                    }

                    MessageModel source = null;
                    try {  // get the up-to-date model
                        // try to create a graph from the editor content
                        byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                        source = MessageModel.forInputStream(new ByteArrayInputStream(docString));
                    } catch (RuntimeException e) {
                        InputStream is = new BufferedInputStream(dsFileObject.getInputStream());
                        try {
                            source = MessageModel.forInputStream(is);
                        } finally {
                            is.close();
                        }
                        if (source == null) {
                            // neither the old graph is parseable, there is not much we can do here
                            // we could skip it but we can't be sure whether there are duplicate
                            // entries
                            // TODO: should we notify the user?
                            throw new ConfigurationException(
                                    NbBundle.getMessage(WLDatasourceSupport.class, "MSG_datasourcesXmlCannotParse", dsFileObject.getNameExt()));
                        }
                        // current editor content is not parseable, ask whether to override or not
                        NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                                NbBundle.getMessage(WLDatasourceSupport.class, "MSG_datasourcesXmlNotValid", dsFileObject.getNameExt()),
                                NotifyDescriptor.YES_NO_OPTION);
                        Object result = DialogDisplayer.getDefault().notify(notDesc);
                        if (result == NotifyDescriptor.NO_OPTION) {
                            // keep the old content
                            return null;
                        }
                        File origin = FileUtil.toFile(dsFileObject);
                        for (MessageModel.MessageDestination dest : source.getMessageDestinations(Type.QUEUE, false)) {
                            destinations.put(new WLMessageDestination(
                                    dest.getResourceName(), dest.getJndiName(), Type.QUEUE, origin, false), datasourceDO);
                        }
                        for (MessageModel.MessageDestination dest : source.getMessageDestinations(Type.TOPIC, false)) {
                            destinations.put(new WLMessageDestination(
                                    dest.getResourceName(), dest.getJndiName(), Type.TOPIC, origin, false), datasourceDO);
                        }
                    }
                }
            }

            ModifiedMessageDestination modifiedDestination = modifier.modify(destinations.keySet());

            // TODO for now this code won't be called probably as there is no
            // real modify in our code just create
            DataObject datasourceDO = destinations.get(modifiedDestination.getMessageDestination());
            if (datasourceDO != null) {
                boolean modified = datasourceDO.isModified();
                EditorCookie editor = (EditorCookie) datasourceDO.getCookie(EditorCookie.class);
                StyledDocument doc = editor.getDocument();
                if (doc == null) {
                    doc = editor.openDocument();
                }
                replaceDocument(doc, modifiedDestination.getModel());

                if (!modified) {
                    SaveCookie cookie = (SaveCookie) datasourceDO.getCookie(SaveCookie.class);
                    cookie.save();
                }
            }

            return modifiedDestination.getMessageDestination();
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WLDatasourceSupport.class, "MSG_CannotUpdate");
            throw new ConfigurationException(msg, ioe);
        }

        return null;
    }

    private void writeFile(final File file, final BaseDescriptorModel bean) throws ConfigurationException {
        assert file != null : "File to write can't be null"; // NOI18N
        assert file.getParentFile() != null : "File parent folder can't be null"; // NOI18N

        try {
            FileObject cfolder = FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile()));
            if (cfolder == null) {
                try {
                    cfolder = FileUtil.createFolder(FileUtil.normalizeFile(file.getParentFile()));
                } catch (IOException ex) {
                    throw new ConfigurationException(NbBundle.getMessage(WLDatasourceSupport.class,
                            "MSG_FailedToCreateConfigFolder", file.getParentFile().getAbsolutePath()));
                }
            }

            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch(IOException ioe) {
                                LOGGER.log(Level.FINE, null, ioe);
                            }
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }
            });
            
            FileUtil.refreshFor(file);
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }

    private void replaceDocument(final StyledDocument doc, BaseDescriptorModel graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });
    }

    private void ensureResourceDirExists() {
        if (!resourceDir.exists()) {
            resourceDir.mkdir();
            FileUtil.refreshFor(resourceDir);
        }
    }

    private interface MessageDestinationModifier {

        @NonNull
        ModifiedMessageDestination modify(Set<WLMessageDestination> destinations) throws ConfigurationException;

    }

    private static class ModifiedMessageDestination {

        private final File file;

        private final MessageModel model;

        private final WLMessageDestination destination;

        public ModifiedMessageDestination(File file, MessageModel model, WLMessageDestination destination) {
            this.file = file;
            this.model = model;
            this.destination = destination;
        }

        public WLMessageDestination getMessageDestination() {
            return destination;
        }

        public MessageModel getModel() {
            return model;
        }

        public File getFile() {
            return file;
        }
    }

    private static class JmsSystemResourceHandler extends DefaultHandler {

        private final List<JmsResource> resources = new ArrayList<JmsResource>();

        private final File configDir;

        private final StringBuilder value = new StringBuilder();

        private JmsResource resource;

        public JmsSystemResourceHandler(File configDir) {
            this.configDir = configDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value.setLength(0);
            if ("jms-system-resource".equals(qName)) { // NOI18N
                resource = new JmsResource(configDir, true);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (resource == null) {
                return;
            }

            if ("jms-system-resource".equals(qName)) { // NOI18N
                resources.add(resource);
                resource = null; 
            } else if("name".equals(qName)) { // NOI18N
                resource.setResourceName(value.toString());
            } else if("jndi-name".equals(qName)) { // NOI18N
                resource.setJndiName(value.toString());
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value.toString());
            } else if ("descriptor-file-name".equals(qName)) { // NOI18N
                resource.setFile(value.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public List<JmsResource> getResources() {
            return resources;
        }
    }

    private static class JmsApplicationHandler extends DefaultHandler {

        private final List<JmsResource> resources = new ArrayList<JmsResource>();

        private final File domainDir;

        private final StringBuilder value = new StringBuilder();

        private JmsResource resource;

        private boolean isJms;

        public JmsApplicationHandler(File domainDir) {
            this.domainDir = domainDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value.setLength(0);
            if ("app-deployment".equals(qName)) { // NOI18N
                resource = new JmsResource(domainDir, false);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (resource == null) {
                return;
            }

            if ("app-deployment".equals(qName)) { // NOI18N
                if (isJms) {
                    resources.add(resource);
                }
                isJms = false;
                resource = null;
            } else if("name".equals(qName)) { // NOI18N
                resource.setResourceName(value.toString());
            } else if("jndi-name".equals(qName)) { // NOI18N
                resource.setJndiName(value.toString());
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value.toString());
            } else if ("source-path".equals(qName)) { // NOI18N
                resource.setFile(value.toString());
            } else if ("module-type".equals(qName)) { // NOI18N
                if ("jms".equals(value.toString())) {
                    isJms = true;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public List<JmsResource> getResources() {
            return resources;
        }
    }

    private static class JmsHandler extends DefaultHandler {

        private final JmsSystemResourceHandler system;

        private final JmsApplicationHandler application;

        public JmsHandler(File domainDir) {
            File configDir = domainDir != null ? new File(domainDir, "config") : null;
            system = new JmsSystemResourceHandler(configDir);
            application = new JmsApplicationHandler(domainDir);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            system.startElement(uri, localName, qName, attributes);
            application.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            system.endElement(uri, localName, qName);
            application.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            system.characters(ch, start, length);
            application.characters(ch, start, length);
        }

        public List<JmsResource> getResources() {
            List<JmsResource> resources = new ArrayList<JmsResource>();
            resources.addAll(system.getResources());
            resources.addAll(application.getResources());
            return resources;
        }
    }

    private static class JmsResource {

        private final File baseFile;

        private final boolean system;
        
        private String jndiName;

        private String resourceName;

        private String target;

        private String file;

        public JmsResource(File baseFile, boolean system) {
            this.baseFile = baseFile;
            this.system = system;
        }

        @CheckForNull
        public File resolveFile() {
            if (file == null) {
                return null;
            }

            File config = new File(file);
            if (!config.isAbsolute()) {
                if (baseFile != null) {
                    config = new File(baseFile, file);
                } else {
                    return null;
                }
            }
            if (config.exists() && config.isFile() && config.canRead()) {
                return config;
            }
            return null;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getJndiName() {
            return jndiName;
        }

        public void setJndiName(String name) {
            this.jndiName = name;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isSystem() {
            return system;
        }

    }
}
