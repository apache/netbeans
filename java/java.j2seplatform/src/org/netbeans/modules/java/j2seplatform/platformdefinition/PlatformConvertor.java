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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.openide.modules.SpecificationVersion;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.*;

import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.java.j2seplatform.wizard.J2SEWizardIterator;

/**
 * Reads and writes the standard platform format implemented by PlatformImpl2.
 *
 * @author Svata Dedic
 */
public class PlatformConvertor implements Environment.Provider, InstanceCookie.Of, PropertyChangeListener, Runnable, InstanceContent.Convertor<Class<Node>,Node> {

    private static final Logger LOG = Logger.getLogger(PlatformConvertor.class.getName());

    private static final String CLASSIC = "classic";        //NOI18N
    private static final String MODERN = "modern";          //NOI18N
    private static final String JAVAC13 = "javac1.3";       //NOI18N
    public static final String[] IMPORTANT_TOOLS = {
        // Used by j2seproject:
        "javac", // NOI18N
        "java", // NOI18N
        // Might be used, though currently not (cf. #46901):
        "javadoc", // NOI18N
    };

    private static final String PLATFORM_STOREGE = "Services/Platforms/org-netbeans-api-java-Platform"; //NOI18N
    private static final String PLATFORM_DTD_ID = "-//NetBeans//DTD Java PlatformDefinition 1.0//EN"; // NOI18N
    private static final String URL_EMBEDDING = "!/";   //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PlatformConvertor.class.getName(), 1, false, false);

    private PlatformConvertor() {}

    public static PlatformConvertor createProvider(FileObject reg) {
        return new PlatformConvertor();
    }

    @Override
    public Lookup getEnvironment(DataObject obj) {
        if (obj instanceof XMLDataObject) {
            return new PlatformConvertor((XMLDataObject)obj).getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }

    private InstanceContent cookies = new InstanceContent();

    private XMLDataObject   holder;

    private boolean defaultPlatform;

    private Lookup  lookup;

    private RequestProcessor.Task    saveTask;

    private Reference<JavaPlatform>   refPlatform = new WeakReference<>(null);

    private LinkedList<PropertyChangeEvent> keepAlive = new LinkedList<>();

    private PlatformConvertor(@NonNull final XMLDataObject  object) {
        Parameters.notNull("object", object);
        this.holder = object;
        this.holder.getPrimaryFile().addFileChangeListener( new FileChangeAdapter () {
            @Override
            public void fileDeleted (final FileEvent fe) {
                if (!defaultPlatform) {
                    try {
                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction<Void> () {
                        @Override
                        public Void run () throws IOException {
                            String systemName = fe.getFile().getName();
                            String propPrefix =  "platforms." + systemName + ".";   //NOI18N
                            boolean changed = false;
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            for (Iterator<String> it = props.keySet().iterator(); it.hasNext(); ) {
                                String key = it.next ();
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
                }
            }
        });
        cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
        cookies.add(Node.class, this);
    }

    Lookup getLookup() {
        return lookup;
    }

    @Override
    public Class instanceClass() {
        return JavaPlatform.class;
    }

    @Override
    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refPlatform.get();
            if (o != null)
                return o;
            H handler = new H();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new org.xml.sax.InputSource(
                    holder.getPrimaryFile().getInputStream());
                is.setSystemId(holder.getPrimaryFile().toURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(handler);

                reader.parse(is);
            } catch (SAXException ex) {
                final Exception cause = ex.getException();
                if (cause instanceof java.io.IOException) {
                    throw (IOException)cause;
                } else {
                    throw new java.io.IOException(cause);
                }
            }
            JavaPlatform inst = createPlatform(handler);
            refPlatform = new WeakReference<>(inst);
            return inst;
        }
    }

    JavaPlatform createPlatform(H handler) throws IOException {
        JavaPlatform p;

        if (handler.isDefault) {
            p = DefaultPlatformImpl.create (handler.properties, handler.sources, handler.javadoc);
            defaultPlatform = true;
        } else {
            p = new J2SEPlatformImpl(
                    handler.name,
                    handler.installFolders,
                    handler.properties,
                    Util.filterProbe(handler.sysProperties, null),
                    handler.sources,
                    handler.javadoc);
            defaultPlatform = false;
        }
        validate(p);
        p.addPropertyChangeListener(this);
        return p;
    }

    @Override
    public String instanceName() {
        return holder.getName();
    }

    @Override
    public boolean instanceOf(Class<?> type) {
        return (type.isAssignableFrom(JavaPlatform.class));
    }

    private static final int DELAY = 2000;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            if (saveTask == null)
                saveTask = RP.create(this);
        }
        synchronized (this) {
            keepAlive.add(evt);
        }
        saveTask.schedule(DELAY);
    }

    @Override
    public void run() {
        PropertyChangeEvent e;

        synchronized (this) {
            e = keepAlive.removeFirst();
        }
        J2SEPlatformImpl plat = (J2SEPlatformImpl)e.getSource();
        try {
            holder.getPrimaryFile().getFileSystem().runAtomicAction(
                new W(plat, holder, defaultPlatform));
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.INFO));
        }
    }

    @Override
    public Node convert(Class<Node> key) {
        try {
            J2SEPlatformImpl p = (J2SEPlatformImpl) instanceCreate();
            return new J2SEPlatformNode (p,this.holder);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public String displayName(Class<Node> key) {
        return key.getName();
    }

    @Override
    public String id(Class<Node> key) {
        return key.getName();
    }

    @Override
    public Class<Node> type(Class<Node> key) {
        return key;
    }

    public static JavaPlatform create (final J2SEPlatformImpl prototype) throws IOException, IllegalArgumentException {
        Parameters.notNull("prototype", prototype);
        final String systemName = prototype.getProperties().get(J2SEPlatformImpl.PLAT_PROP_ANT_NAME);
        if (systemName == null) {
            throw new IllegalArgumentException(J2SEPlatformImpl.PLAT_PROP_ANT_NAME);
        }
        final FileObject platformsFolder = FileUtil.getConfigFile(PLATFORM_STOREGE);
        if (platformsFolder.getFileObject(systemName,"xml")!=null) {   //NOI18N
            throw new IllegalArgumentException(systemName);
        }
        final DataObject dobj = create(prototype, DataFolder.findFolder(platformsFolder),systemName);
        return dobj.getNodeDelegate().getLookup().lookup(JavaPlatform.class);
    }

    @NonNull
    public static String getFreeAntName (@NonNull final String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException ();
        }
        final FileObject platformsFolder = FileUtil.getConfigFile(PLATFORM_STOREGE);
        String antName = PropertyUtils.getUsablePropertyName(name);
        if (platformsFolder.getFileObject(antName,"xml") != null) { //NOI18N
            String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString (index);
            while (platformsFolder.getFileObject(antName,"xml") != null) {  //NOI18N
                index ++;
                antName = baseName + Integer.toString (index);
            }
        }
        return antName;
    }

    public static void generatePlatformProperties (JavaPlatform platform, String systemName, EditableProperties props) throws IOException {
        String homePropName = createName(systemName,"home");      //NOI18N
        String bootClassPathPropName = createName(systemName,"bootclasspath");    //NOI18N
        String compilerType= createName (systemName,"compiler");  //NOI18N
        if (props.getProperty(homePropName) != null || props.getProperty(bootClassPathPropName) != null
                || props.getProperty(compilerType)!=null) {
            //Already defined warn user
            final String msg = NbBundle.getMessage(J2SEWizardIterator.class,"ERROR_InvalidName"); //NOI18N
            throw Exceptions.attachLocalizedMessage(
                    new IllegalStateException(msg),
                    msg);
        }
        Collection installFolders = platform.getInstallFolders();
        if (installFolders.size()>0) {
            File jdkHome = FileUtil.toFile ((FileObject)installFolders.iterator().next());
            props.setProperty(homePropName, jdkHome.getAbsolutePath());
            ClassPath bootCP = platform.getBootstrapLibraries();
            StringBuilder sbootcp = new StringBuilder();
            for (ClassPath.Entry entry : bootCP.entries()) {
                URL url = entry.getURL();
                String pathInArchive = "";  //NOI18N
                boolean wasFolder = false;
                if (FileUtil.isArchiveArtifact(url)) {
                    String path = url.getPath();
                    int index = path.lastIndexOf(URL_EMBEDDING); //NOI18N
                    if (index >= 0) {
                        wasFolder = index > 0 && path.charAt(index-1) == '/';   //NOI18N
                        pathInArchive = path.substring(index+URL_EMBEDDING.length());
                    }
                    url = FileUtil.getArchiveFile(url);
                }
                String rootPath = BaseUtilities.toFile(URI.create(url.toExternalForm())).getAbsolutePath();
                if (!pathInArchive.isEmpty()) {
                    final StringBuilder rpb = new StringBuilder(
                            rootPath.length() + File.separator.length() + URL_EMBEDDING.length() + pathInArchive.length());
                    rpb.append(rootPath);
                    if (wasFolder && !rootPath.endsWith(File.separator)) {
                        rpb.append(File.separator);
                    }
                    rpb.append(URL_EMBEDDING);
                    rpb.append(pathInArchive);
                    rootPath = rpb.toString();
                }
                if (sbootcp.length()>0) {
                    sbootcp.append(File.pathSeparator);
                }
                sbootcp.append(normalizePath(rootPath, jdkHome, homePropName));
            }
            if (sbootcp != null) {
                props.setProperty(bootClassPathPropName,sbootcp.toString());
            }
            props.setProperty(compilerType,getCompilerType(platform));
            for (int i = 0; i < IMPORTANT_TOOLS.length; i++) {
                String name = IMPORTANT_TOOLS[i];
                FileObject tool = platform.findTool(name);
                if (tool != null) {
                    if (!isDefaultLocation(tool, platform.getInstallFolders())) {
                        String toolName = createName(systemName, name);
                        props.setProperty(toolName, normalizePath(getToolPath(tool), jdkHome, homePropName));
                    }
                } else {
                    throw new BrokenPlatformException (name);
                }
            }
        }
    }

    public static String createName (String platName, String propType) {
        return "platforms." + platName + "." + propType;        //NOI18N
    }

    private void validate(@NonNull JavaPlatform plat) throws IOException {
        final SpecificationVersion ver = plat.getSpecification().getVersion();
        if (ver.compareTo(SourceLevelQuery.MINIMAL_SOURCE_LEVEL) < 0) {
            final IOException veto = new IOException(String.format(
                "Unsupported platform source level: %s",
                ver));
            throw Exceptions.attachSeverity(veto, Level.FINEST);
        }
    }

    private static DataObject create(final J2SEPlatformImpl plat, final DataFolder f, final String idName) throws IOException {
        W w = new W(plat, f, idName);
        f.getPrimaryFile().getFileSystem().runAtomicAction(w);
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Void> () {
                        @Override
                        public Void run () throws Exception {
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            generatePlatformProperties(plat, idName, props);
                            PropertyUtils.putGlobalProperties (props);
                            return null;
                        }
                    });
        } catch (MutexException me) {
            Exception originalException = me.getException();
            if (originalException instanceof RuntimeException) {
                throw (RuntimeException) originalException;
            }
            else if (originalException instanceof IOException) {
                throw (IOException) originalException;
            }
            else
            {
                throw new IllegalStateException (); //Should never happen
            }
        }
        return w.holder;
    }

    private static String getCompilerType (JavaPlatform platform) {
        assert platform != null;
        String prop = platform.getSystemProperties().get("java.specification.version"); //NOI18N
        if (prop == null) {
            LOG.log(
               Level.INFO,
               "Broken platform system properties, no java.specification.version",    //NOI18N
               new IllegalArgumentException(
                    String.format("platform: %s System Properties: %s",               //NOI18N
                        platform.getDisplayName(),
                        platform.getSystemProperties())));
            return MODERN;
        }
        SpecificationVersion specificationVersion = new SpecificationVersion (prop);
        SpecificationVersion jdk13 = new SpecificationVersion("1.3");   //NOI18N
        int c = specificationVersion.compareTo (jdk13);
        if (c<0) {
            return CLASSIC;
        }
        else if (c == 0) {
            return JAVAC13;
        }
        else {
            return MODERN;
        }
    }

    private static boolean isDefaultLocation (FileObject tool, Collection<FileObject> installFolders) {
        assert tool != null && installFolders != null;
        if (installFolders.size()!=1) {
            return false;
        }
        FileObject root = installFolders.iterator().next();
        String relativePath = FileUtil.getRelativePath(root,tool);
        if (relativePath == null) {
            return false;
        }
        StringTokenizer tk = new StringTokenizer(relativePath, "/");
        return (tk.countTokens()== 2 && "bin".equals(tk.nextToken()));
    }


    private static File getToolPath (FileObject tool) {
        assert tool != null;
        return Utilities.toFile(URI.create(tool.toURL().toExternalForm()));
    }

    private static String normalizePath (File path,  File jdkHome, String propName) {
        return normalizePath(path.getAbsolutePath(), jdkHome, propName);
    }

    private static String normalizePath (String absolutePath,  File jdkHome, String propName) {
        String jdkLoc = jdkHome.getAbsolutePath();
        if (!jdkLoc.endsWith(File.separator)) {
            jdkLoc = jdkLoc + File.separator;
        }
        if (absolutePath.startsWith(jdkLoc)) {
            return "${"+propName+"}"+File.separator+absolutePath.substring(jdkLoc.length());           //NOI18N
        } else {
            return absolutePath;
        }
    }

    public static class BrokenPlatformException extends IOException {

        private final String toolName;

        public BrokenPlatformException (final String toolName) {
            super ("Cannot locate " + toolName + " command");   //NOI18N
            this.toolName = toolName;
        }

        public String getMissingTool () {
            return this.toolName;
        }

    }

    private static final class W implements FileSystem.AtomicAction {
        J2SEPlatformImpl instance;
        MultiDataObject holder;
        String name;
        DataFolder f;
        boolean defaultPlatform;

        W(J2SEPlatformImpl instance, MultiDataObject holder, boolean defaultPlatform) {
            this.instance = instance;
            this.holder = holder;
            this.defaultPlatform = defaultPlatform;
        }

        W(J2SEPlatformImpl instance, DataFolder f, String n) {
            this.instance = instance;
            this.name = n;
            this.f = f;
            this.defaultPlatform = false;
        }

        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;


            final ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
            try {
                write (buffer);
            } finally {
                buffer.close();
            }
            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = f.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, name, "xml");
                data = folder.createData(fn, "xml");
                lck = data.lock();
            }
            try (OutputStream out = data.getOutputStream(lck)) {
                out.write(buffer.toByteArray());
                out.flush();
            } finally {
                lck.releaseLock();
            }
            if (holder == null) {
                holder = (MultiDataObject)DataObject.find(data);
            }
        }

        void write(final  OutputStream out) throws IOException {
            final Map<String,String> props = instance.getProperties();
            final Map<String,String> sysProps = instance.getSystemProperties();
            final Document doc = XMLUtil.createDocument(ELEMENT_PLATFORM,null,PLATFORM_DTD_ID,"http://www.netbeans.org/dtds/java-platformdefinition-1_0.dtd"); //NOI18N
            final Element platformElement = doc.getDocumentElement();
            platformElement.setAttribute(ATTR_PLATFORM_NAME,instance.getDisplayName());
            platformElement.setAttribute(ATTR_PLATFORM_DEFAULT,defaultPlatform ? "yes" : "no"); //NOI18N
            if (!defaultPlatform) {
                final Element jdkHomeElement = doc.createElement(ELEMENT_JDKHOME);
                for (Iterator<FileObject> it = instance.getInstallFolders().iterator(); it.hasNext();) {
                    URL url = it.next ().toURL();
                    final Element resourceElement = doc.createElement(ELEMENT_RESOURCE);
                    resourceElement.appendChild(doc.createTextNode(url.toExternalForm()));
                    jdkHomeElement.appendChild(resourceElement);
                }
                platformElement.appendChild(jdkHomeElement);
            }
            final Element propsElement = doc.createElement(ELEMENT_PROPERTIES);
            writeProperties(props, propsElement, doc);
            platformElement.appendChild(propsElement);
            if (!defaultPlatform) {
                final Element sysPropsElement = doc.createElement(ELEMENT_SYSPROPERTIES);
                writeProperties(sysProps, sysPropsElement, doc);
                platformElement.appendChild(sysPropsElement);
            }
            final List<ClassPath.Entry> psl = this.instance.getSourceFolders().entries();
            if (psl.size()>0 && shouldWriteSources ()) {
                final Element sourcesElement = doc.createElement (ELEMENT_SOURCEPATH);
                for (Iterator<ClassPath.Entry> it = psl.iterator(); it.hasNext();) {
                    URL url = it.next ().getURL();
                    final Element resourceElement = doc.createElement (ELEMENT_RESOURCE);
                    resourceElement.appendChild(doc.createTextNode(url.toExternalForm()));
                    sourcesElement.appendChild(resourceElement);
                }
                platformElement.appendChild(sourcesElement);
            }
            final List<URL> pdl = this.instance.getJavadocFolders();
            if (pdl.size()>0 && shouldWriteJavadoc ()) {
                final Element javadocElement = doc.createElement(ELEMENT_JAVADOC);
                for (URL url : pdl) {
                    final Element resourceElement = doc.createElement(ELEMENT_RESOURCE);
                    resourceElement.appendChild(doc.createTextNode(url.toExternalForm()));
                    javadocElement.appendChild(resourceElement);
                }
                platformElement.appendChild(javadocElement);
            }
            XMLUtil.write(doc, out, "UTF8");                                                    //NOI18N
        }

        void writeProperties(final Map<String,String> props, final Element element, final Document doc) throws IOException {
            final Collection<String> sortedProps = new TreeSet<>(props.keySet());
            for (Iterator<String> it = sortedProps.iterator(); it.hasNext(); ) {
                final String n = it.next();
                final String val = props.get(n);
                try {
                    XMLUtil.toAttributeValue(n);
                    XMLUtil.toAttributeValue(val);
                    final Element propElement = doc.createElement(ELEMENT_PROPERTY);
                    propElement.setAttribute(ATTR_PROPERTY_NAME,n);
                    propElement.setAttribute(ATTR_PROPERTY_VALUE,val);
                    element.appendChild(propElement);
                } catch (CharConversionException e) {
                    LOG.log(
                        Level.WARNING,
                        "Cannot store property: {0} value: {1}",       //NOI18N
                        new Object[]{
                            n,
                            val
                        });
                }
            }
        }

        private boolean shouldWriteSources () {
            final List<URL> roots = new ArrayList<>();
            for (ClassPath.Entry entry : instance.getSourceFolders().entries()) {
                roots.add(entry.getURL());
            }
            return !roots.equals(instance.defaultSources());
        }

        private boolean shouldWriteJavadoc () {
            return !instance.getJavadocFolders().equals(instance.defaultJavadoc());
        }
    }

    static final String ELEMENT_PROPERTIES = "properties"; // NOI18N
    static final String ELEMENT_SYSPROPERTIES = "sysproperties"; // NOI18N
    static final String ELEMENT_PROPERTY = "property"; // NOI18N
    static final String ELEMENT_PLATFORM = "platform"; // NOI18N
    static final String ELEMENT_JDKHOME = "jdkhome";    //NOI18N
    static final String ELEMENT_SOURCEPATH = "sources";  //NOI18N
    static final String ELEMENT_JAVADOC = "javadoc";    //NOI18N
    static final String ELEMENT_RESOURCE = "resource";  //NOI18N
    static final String ATTR_PLATFORM_NAME = "name"; // NOI18N
    static final String ATTR_PLATFORM_DEFAULT = "default"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N

    private static final class H extends org.xml.sax.helpers.DefaultHandler implements EntityResolver {
        Map<String,String> properties;
        Map<String,String> sysProperties;
        List<URL> sources;
        List<URL> javadoc;
        List<URL> installFolders;
        String  name;
        boolean isDefault;

        private Map<String,String> propertyMap;
        private StringBuffer buffer;
        private List<URL> path;


        @Override
        public void startDocument () throws org.xml.sax.SAXException {
        }

        @Override
        public void endDocument () throws org.xml.sax.SAXException {
        }

        @Override
        public void startElement (String uri, String localName, String qName, org.xml.sax.Attributes attrs)
        throws org.xml.sax.SAXException {
            if (qName != null) {
                switch (qName) {
                    case ELEMENT_PLATFORM:
                        name = attrs.getValue(ATTR_PLATFORM_NAME);
                        isDefault = "yes".equals(attrs.getValue(ATTR_PLATFORM_DEFAULT));
                        break;
                    case ELEMENT_PROPERTIES:
                        if (properties == null) {
                            properties = new HashMap<>(17);
                        }   propertyMap = properties;
                        break;
                    case ELEMENT_SYSPROPERTIES:
                        if (sysProperties == null) {
                            sysProperties = new HashMap<>(17);
                        }   propertyMap = sysProperties;
                        break;
                    case ELEMENT_PROPERTY:{
                        if (propertyMap == null) {
                            throw new SAXException("property w/o properties or sysproperties");
                        }
                        String name = attrs.getValue(ATTR_PROPERTY_NAME);
                        if (name == null || "".equals(name)) {
                            throw new SAXException("missing name");
                        }
                        String val = attrs.getValue(ATTR_PROPERTY_VALUE);
                        propertyMap.put(name, val);
                        break;
                    }
                    case ELEMENT_SOURCEPATH:
                        this.sources = new ArrayList<> ();
                        this.path = this.sources;
                        break;
                    case ELEMENT_JAVADOC:
                        this.javadoc = new ArrayList<> ();
                        this.path = this.javadoc;
                        break;
                    case ELEMENT_JDKHOME:
                        this.installFolders = new ArrayList<> ();
                        this.path =  this.installFolders;
                        break;
                    case ELEMENT_RESOURCE:
                        this.buffer = new StringBuffer ();
                        break;
                }
            }
        }

        @Override
        public void endElement (String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if (qName != null) {
                switch (qName) {
                    case ELEMENT_PROPERTIES:
                    case ELEMENT_SYSPROPERTIES:
                        propertyMap = null;
                        break;
                    case ELEMENT_SOURCEPATH:
                    case ELEMENT_JAVADOC:
                        path = null;
                        break;
                    case ELEMENT_RESOURCE:
                        try {
                            this.path.add (new URL(this.buffer.toString()));
                        } catch (MalformedURLException mue) {
                            Exceptions.printStackTrace(mue);
                        }   this.buffer = null;
                        break;
                }
            }
        }

        @Override
        public void characters(char chars[], int start, int length) throws SAXException {
            if (this.buffer != null) {
                this.buffer.append(chars, start, length);
            }
        }

        @Override
        public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
            if (PLATFORM_DTD_ID.equals (publicId)) {
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }

    }

}
