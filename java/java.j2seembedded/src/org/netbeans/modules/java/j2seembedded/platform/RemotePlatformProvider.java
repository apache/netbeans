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
package org.netbeans.modules.java.j2seembedded.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Zezula
 */
public final class RemotePlatformProvider implements Lookup.Provider, InstanceCookie.Of, InstanceContent.Convertor<Class<Node>, Node>, PropertyChangeListener, Runnable {
    
    private static final String PLATFORM_STOREGE = "Services/Platforms/org-netbeans-api-java-Platform"; //NOI18N
    private static final Logger LOG = Logger.getLogger(RemotePlatformProvider.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(RemotePlatformProvider.class);
    private static final int SLIDING_WINDOW = 2000;    
    
    private final XMLDataObject store;
    private final RequestProcessor.Task task;
    private final Lookup lkp;
    //@GuardedBy("this")
    private Reference<RemotePlatform> platformRef;
    
    private RemotePlatformProvider(@NonNull final XMLDataObject store) {
        Parameters.notNull("store", store); //NOI18N
        this.store = store;
        this.store.getPrimaryFile().addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileDeleted(@NonNull final FileEvent fe) {
                final String systemName = fe.getFile().getName();
                try {
                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction<Void> () {
                        @Override
                        public Void run () throws IOException {
                            final String propPrefix =  String.format("platforms.%s.", systemName);   //NOI18N
                            boolean changed = false;
                            final EditableProperties props = PropertyUtils.getGlobalProperties();
                            for (Iterator<String> it = props.keySet().iterator(); it.hasNext(); ) {
                                final String key = it.next ();
                                if (key.startsWith(propPrefix)) {
                                    it.remove();
                                    changed =true;
                                }
                            }
                            if (changed) {
                                PropertyUtils.putGlobalProperties(props);
                            }
                            return null;
                        }
                    });
                } catch (MutexException e) {
                    Exceptions.printStackTrace(e);
                }
                ConnectionMethod.Authentification.clear(systemName);
            }
        });
        final InstanceContent c = new InstanceContent();
        c.add(Node.class, this);
        c.add(this);
        this.lkp = new AbstractLookup(c);
        task = RP.create(this);
    }

    @Override
    public Lookup getLookup() {
        return this.lkp;
    }

    @Override
    public boolean instanceOf(@NonNull final Class<?> type) {
        return type.isAssignableFrom(JavaPlatform.class);
    }

    @Override
    public String instanceName() {
        return store.getName();
    }

    @NonNull
    @Override
    public Class<?> instanceClass() throws IOException, ClassNotFoundException {
        return JavaPlatform.class;
    }

    @Override
    @NonNull
    public synchronized RemotePlatform instanceCreate() throws IOException, ClassNotFoundException {
        RemotePlatform remotePlatform = platformRef == null ? null : platformRef.get();
        if (remotePlatform == null) {
            final SAXHandler handler = new SAXHandler();
            try (InputStream in = store.getPrimaryFile().getInputStream()) {
                final XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new InputSource(in);
                is.setSystemId(store.getPrimaryFile().toURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(handler);
                reader.parse(is);
            } catch (SAXException ex) {
                final Exception x = ex.getException();
                if (x instanceof java.io.IOException) {
                    throw (IOException)x;
                } else {
                    throw new java.io.IOException(ex);
                }
            }
            remotePlatform = RemotePlatform.create(
                    handler.name,
                    handler.properties,
                    handler.sysProperties);
            remotePlatform.addPropertyChangeListener(this);
            platformRef = new WeakReference<>(remotePlatform);
        }
        return remotePlatform;
    }
    
    @Override
    public Node convert(Class<Node> clz) {
        try {
            final RemotePlatform rp = instanceCreate();
            return new RemotePlatformNode(rp, store);
        } catch (IOException | ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public String displayName(Class<Node> clz) {
        return id(clz);
    }

    @Override
    public String id(Class<Node> clz) {
        return clz.getName();
    }

    @Override
    public Class<? extends Node> type(Class<Node> clz) {
        return clz;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        task.schedule(SLIDING_WINDOW);
    }

    @Override
    public void run() {
        final RemotePlatform platform;
        synchronized (this) {
            platform = platformRef == null ?
                null :
                platformRef.get();
        }
        if (platform != null) {
            try {
                FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        store(platform, store.getPrimaryFile());
                    }
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    @NonNull
    public static RemotePlatform createNewPlatform (
        @NonNull final RemotePlatform prototype) throws IOException {
        Parameters.notNull("prototype", prototype);   //NOI18N
        final String antName = prototype.getProperties().get(RemotePlatform.PLAT_PROP_ANT_NAME);
        if (antName == null) {
            throw new IllegalArgumentException("Platform has no " + RemotePlatform.PLAT_PROP_ANT_NAME + " attribute.");    //NOI18N
        }
        final RemotePlatform[] res = new RemotePlatform[1];
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {                
                final FileObject platformsFolder = FileUtil.getConfigFile(PLATFORM_STOREGE);
                if (platformsFolder.getFileObject(antName,"xml")!=null) {   //NOI18N
                    throw new IllegalArgumentException("Platform with ant name: " + antName + "already exists.");   //NOI18N
                }
                final FileObject target = platformsFolder.createData(antName, "xml");   //NOI18N
                store(prototype, target);
                final DataObject dobj = DataObject.find(target);
                res[0] =  dobj.getNodeDelegate().getLookup().lookup(RemotePlatform.class);
            }
        });
        assert res[0] != null;
        return res[0];
    }

    public static boolean isValidPlatformAntName(@NonNull String name) {
        Parameters.notNull("name", name);   //NOI18N
        final FileObject platformsFolder = FileUtil.getConfigFile(PLATFORM_STOREGE);
        return platformsFolder.getFileObject(name, "xml") == null; //NOI18N
    }

    private static void store(
        @NonNull final RemotePlatform platform,
        @NonNull final FileObject target) throws IOException {
        storePlatformDefinition(platform, target);
        updateBuildProperties(platform);
    }

    private static void storePlatformDefinition(
        @NonNull final RemotePlatform platform,
        @NonNull final FileObject target) throws IOException {
        final FileLock lock = target.lock();
        try {
            try (OutputStream out = target.getOutputStream(lock)) {
                SAXHandler.write(out, platform);
            }
        } finally {
            lock.releaseLock();
        }
    }

    static void updateBuildProperties(@NonNull final RemotePlatform platform)  throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    final Map<String,String> props = platform.getProperties();
                    final String antPlatformName = props.get(RemotePlatform.PLAT_PROP_ANT_NAME);
                    final EditableProperties ep = PropertyUtils.getGlobalProperties();                    
                    for (String key : platform.getGlobalPropertyNames()) {
                        final String value = props.get(key);
                        if (value != null) {
                            ep.setProperty(
                                createPropertyName(antPlatformName, key),
                                value);
                        } else {
                            ep.remove(createPropertyName(antPlatformName, key));
                        }
                    }
                    PropertyUtils.putGlobalProperties(ep);
                    return null;
                }
            });
        } catch (MutexException me) {
            if (me.getCause() instanceof IOException) {
                throw (IOException) me.getCause();
            } else {
                throw new IOException(me);
            }
        }
    }

    @NonNull
    static String createPropertyName(
            @NonNull final String antPlatformName,
            @NonNull final String propName) {
        return String.format(
            "platforms.%s.%s",          //NOI18N
             antPlatformName,
             propName);
    }

    private static final class SAXHandler extends DefaultHandler implements EntityResolver {

        private static final String REMOTE_PLATFORM_DTD_ID = "-//NetBeans//DTD Remote Java Platform Definition 1.0//EN"; // NOI18N
        private static final String REMOTE_PLATFORM_SYSTEM_ID = "http://www.netbeans.org/dtds/remote-java-platformdefinition-1_0.dtd";  //NOI18N
        private static final String ELM_PLATFORM = "platform";  //NOI18N
        private static final String ELM_PROPERTIES = "properties"; // NOI18N
        private static final String ELM_SYSPROPERTIES = "sysproperties"; // NOI18N
        private static final String ELM_PROPERTY = "property"; // NOI18N
        private static final String ATTR_NAME = "name"; // NOI18N
        private static final String ATTR_VALUE = "value"; // NOI18N

        Map<String,String> properties = Collections.<String,String>emptyMap();
        Map<String,String> sysProperties = Collections.<String,String>emptyMap();
        String  name;

        private Map<String,String> active;


        @Override
        public void startDocument () throws org.xml.sax.SAXException {
        }
        
        @Override
        public void endDocument () throws org.xml.sax.SAXException {
        }
        
        @Override
        public void startElement (
            @NonNull final String uri,
            @NonNull final String localName,
            @NonNull final String qName,
            @NonNull final Attributes attrs) throws org.xml.sax.SAXException {
            if (ELM_PLATFORM.equals(qName)) {
                name = attrs.getValue(ATTR_NAME);
                if (name == null || name.isEmpty()) {
                    throw new SAXException("Missing mandatory platform name."); //NOI18N
                }
            } else if (ELM_PROPERTIES.equals(qName)) {                
                properties = new HashMap<>();
                active = properties;
            } else if (ELM_SYSPROPERTIES.equals(qName)) {                
                sysProperties = new HashMap<>();
                active = sysProperties;
            } else if (ELM_PROPERTY.equals(qName)) {
                if (active == null) {
                    throw new SAXException("property outside properties or sysproperties"); //NOI18N
                }
                String name = attrs.getValue(ATTR_NAME);
                if (name == null || name.isEmpty()) {
                    throw new SAXException("Missing mandatory property name."); //NOI18N
                }
                String val = attrs.getValue(ATTR_VALUE);
                active.put(name, val);
            }
        }
        
        @Override
        public void endElement (String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if (ELM_PROPERTIES.equals(qName) || ELM_SYSPROPERTIES.equals(qName)) {
                active = null;
            }
        }        
        
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            if (REMOTE_PLATFORM_DTD_ID.equals (publicId)) {
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }

        static void write(
            @NonNull final OutputStream out,
            @NonNull final RemotePlatform platform) throws IOException {
            Parameters.notNull("out", out); //NOI18N
            Parameters.notNull("platform", platform);   //NOI18N

            final Document doc = XMLUtil.createDocument(
                    ELM_PLATFORM,
                    null,
                    REMOTE_PLATFORM_DTD_ID,
                    REMOTE_PLATFORM_SYSTEM_ID);
            final Element platformElement = doc.getDocumentElement();
            platformElement.setAttribute(ATTR_NAME, platform.getDisplayName());

            final Map<String,String> props = platform.getProperties();
            final Element propsElement = doc.createElement(ELM_PROPERTIES);
            writeProperties(props, propsElement, doc);
            platformElement.appendChild(propsElement);

            final Map<String,String> sysProps = platform.getSystemProperties();
            final Element sysPropsElement = doc.createElement(ELM_SYSPROPERTIES);
            writeProperties(sysProps, sysPropsElement, doc);
            platformElement.appendChild(sysPropsElement);
            XMLUtil.write(doc, out, "UTF8");
        }

        private static void writeProperties(
                @NonNull final Map<String,String> props,
                @NonNull final Element element,
                @NonNull final Document doc) throws IOException {
            final Collection<String> sortedProps = new TreeSet<>(props.keySet());
            for (String name : sortedProps) {
                final String val = props.get(name);
                try {
                    XMLUtil.toAttributeValue(name);
                    XMLUtil.toAttributeValue(val);
                    final Element propElement = doc.createElement(ELM_PROPERTY);
                    propElement.setAttribute(ATTR_NAME,name);
                    propElement.setAttribute(ATTR_VALUE,val);
                    element.appendChild(propElement);
                } catch (CharConversionException e) {
                    LOG.log(
                        Level.WARNING,
                        "Cannot store property: {0} value: {1}",       //NOI18N
                        new Object[] {
                            name,
                            val
                        });
                }
            }
        }
        
    }

    public static class Env implements Environment.Provider {

        private Env() {}

        @Override
        public Lookup getEnvironment(DataObject obj) {
            return new RemotePlatformProvider((XMLDataObject)obj).getLookup();
        }

        public static Env create(FileObject def) {
            return new Env();
        }
    }
}
