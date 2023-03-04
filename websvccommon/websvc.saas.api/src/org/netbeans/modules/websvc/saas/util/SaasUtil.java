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
package org.netbeans.modules.websvc.saas.util;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.ParamStyle;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.spi.MethodNodeActionsProvider;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author nam
 */
public class SaasUtil {
    public static final String APPLICATION_WADL = "resources/application.wadl"; // NOI18N
    public static final String DEFAULT_SERVICE_NAME = "Service"; // NOI18N
    public static final String CATALOG = "catalog"; // NOI18N
    
    private static final ReadInputStream IS_READER = new InputStreamJaxbReader(); 
    
    public static <T> T loadJaxbObject(FileObject input, Class<T> type, 
            boolean includeAware) throws IOException 
    {
        return loadJaxbObject(input, type, includeAware, IS_READER) ;
    }
    
    public static <T> T loadJaxbObject(FileObject input, Class<T> type, 
            boolean includeAware, ReadInputStream reader) throws IOException 
    {
        if (input == null) {
            return null;
        }
        InputStream in = null;
        try {
            Exception jbex = null;
            try {
                in = input.getInputStream();
                T t = reader.loadJaxbObject(in, type, includeAware);
                if (t != null) {
                    return t;
                }
            } catch (JAXBException ex) {
                jbex = ex;
            } catch (IOException ioe) {
                jbex = ioe;
            }
            String msg = NbBundle.getMessage(SaasUtil.class, "MSG_ErrorLoadingJaxb", type.getName(), input.getPath()); // NOI18N
            IOException ioe = new IOException(msg);
            if (jbex != null) {
                ioe.initCause(jbex);
            }
            throw ioe;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static <T> T loadJaxbObject(InputStream in, Class<T> type) throws JAXBException {
        return loadJaxbObject(in, type, false);
    }

    public static <T> T loadJaxbObject(Reader reader, Class<T> type, 
            boolean includeAware) throws JAXBException 
    {
        Unmarshaller unmarshaller = getUnmarshaller(type);
        Object object = unmarshaller.unmarshal(reader);
        return cast(type, object);
    }
    
    public static <T> T loadJaxbObject(InputStream in, Class<T> type, 
            boolean includeAware) throws JAXBException 
    {
        Unmarshaller unmarshaller = getUnmarshaller(type);
        Object object = unmarshaller.unmarshal(in);
        return cast(type, object);
    }

    private static <T> T cast( Class<T> type, Object o ) {
        if (type.equals(o.getClass())) {
            return type.cast(o);
        } else if (o instanceof JAXBElement) {
            JAXBElement<?> e = (JAXBElement<?>) o;
            return type.cast(e.getValue());
        }

        throw new IllegalArgumentException("Expect: " + type.getName() + " get: " + o.getClass().getName()); // NOI18N
    }

    private static <T> Unmarshaller getUnmarshaller( Class<T> type )
            throws JAXBException
    {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( SaasUtil.class.getClassLoader());
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(type.getPackage().getName(), original);
        }
        finally {
            Thread.currentThread().setContextClassLoader( original);
        }
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return unmarshaller;
    }

    public static SAXSource getSAXSourceWithXIncludeEnabled(InputStream in) {
        return getSAXSourceWithXIncludeEnabled(new InputSource(in));
    }
    
    public static SAXSource getSAXSourceWithXIncludeEnabled(Reader reader) {
        return getSAXSourceWithXIncludeEnabled(new InputSource(reader));
    }
    
    public static SAXSource getSAXSourceWithXIncludeEnabled(InputSource source) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
//TODO: fix classpath http://www.jroller.com/navanee/entry/unsupportedoperationexception_this_parser_does_not
            spf.setXIncludeAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            return new SAXSource(xmlReader, source);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static SaasGroup loadSaasGroup(FileObject input) throws IOException {
        if (input == null) {
            return null;
        }
        Group g = loadJaxbObject(input, Group.class, false);
        return new SaasGroup(null, g);
    }

    public static SaasGroup loadSaasGroup(InputStream input) throws JAXBException {
        Group g = loadJaxbObject(input, Group.class);
        if (g != null) {
            return new SaasGroup((SaasGroup)null, g);
        }
        return null;
    }

    public static void saveSaasGroup(SaasGroup saasGroup, File outFile) throws IOException, JAXBException {
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            saveSaasGroup(saasGroup, out);
        } finally {
            out.close();
        }
    }

    public static final QName QNAME_GROUP = new QName(Saas.NS_SAAS, "group"); // NOI18N
    public static final QName QNAME_SAAS_SERVICES = new QName(Saas.NS_SAAS, "saas-services"); // NOI18N

    public static void saveSaasGroup(SaasGroup saasGroup, OutputStream output) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Group.class.getPackage().getName());
        Marshaller marshaller = jc.createMarshaller();
        JAXBElement<Group> jbe = new JAXBElement<Group>(QNAME_GROUP, Group.class, saasGroup.getDelegate());
        marshaller.marshal(jbe, output);
    }

    public static void saveSaas(Saas saas, FileObject file) throws IOException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance(SaasServices.class.getPackage().getName());
        Marshaller marshaller = jc.createMarshaller();
        JAXBElement<SaasServices> jbe = new JAXBElement<SaasServices>(QNAME_SAAS_SERVICES, SaasServices.class, saas.getDelegate());
        OutputStream out = null;
        FileLock lock = null;
        try {
            lock = file.lock();
            out = file.getOutputStream(lock);
            marshaller.marshal(jbe, out);
        } finally {
            if (out != null) {
                out.close();
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    public static Application loadWadl(FileObject wadlFile) throws IOException {
        return loadJaxbObject(wadlFile, Application.class, true);
    }

    public static Application loadWadl(InputStream in) throws JAXBException {
        Reader reader = getWadlReader(in);
        if ( reader == null ){
            return null;
        }
        return loadJaxbObject(reader, Application.class, true);
    }
    
    public static SaasServices loadSaasServices(FileObject wadlFile) throws IOException {
        return loadJaxbObject(wadlFile, SaasServices.class, true);
    }

    public static SaasServices loadSaasServices(InputStream in) throws JAXBException {
        return loadJaxbObject(in, SaasServices.class, true);
    }

    private static Lookup.Result<SaasNodeActionsProvider> extensionsResult = null;
    public static Collection<? extends SaasNodeActionsProvider> getSaasNodeActionsProviders() {
        if (extensionsResult == null) {
            extensionsResult = Lookup.getDefault().lookupResult(SaasNodeActionsProvider.class);
        }
        return extensionsResult.allInstances();
    }

    private static Lookup.Result<MethodNodeActionsProvider> methodsResult = null;
    public static Collection<? extends MethodNodeActionsProvider> getMethodNodeActionsProviders() {
        if (methodsResult == null) {
            methodsResult = Lookup.getDefault().lookupResult(MethodNodeActionsProvider.class);
        }
        return methodsResult.allInstances();
    }

    public static Resource getParentResource(Application app, Method wm) {
        for(org.netbeans.modules.websvc.saas.model.wadl.Resources wadlResources : 
            app.getResources())
        {
            for (Resource base : wadlResources.getResource()) {
                Resource r = findParentResource(base, wm);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    static Resource findParentResource(Resource base, Method wm) {
        for (Object o : base.getMethodOrResource()) {
            if (o instanceof Method) {
                Method m = (Method)o;
                if (m == wm) {
                    return base;
                }
            } else if (o instanceof Resource) {
                Resource r = findParentResource((Resource)o, wm);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    public static Method wadlMethodFromIdRef(Application app, String methodIdRef) {
        String methodId = methodIdRef;
        if (methodId.charAt(0) == '#') {
            methodId = methodId.substring(1);
        }
        for (Object o : app.getResourceTypeOrMethodOrRepresentation()) {
            if (o instanceof Method) {
                Method m = (Method) o;
                if (methodId.equals(m.getId())) {
                    return m;
                }
            }
        }
        for (org.netbeans.modules.websvc.saas.model.wadl.Resources wadlResources : app
                .getResources())
        {
            for (Resource base : wadlResources.getResource()) {
                Method result = findMethodById(base, methodId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    static Method findMethodById(Resource base, String methodId) {
        for (Object o : base.getMethodOrResource()) {
            if (o instanceof Method) {
                Method m = (Method)o;
                if (methodId.equals(m.getId())) {
                    return m;
                }
            } else {
                Method m = findMethodById((Resource)o, methodId);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }

    public static Method wadlMethodFromXPath(Application app, String xpath) {
        String paths[] = xpath.split("/"); // NOI18N
        Resource current = null;
        for (int pathIndex = 0; pathIndex < paths.length; pathIndex++) {
            String path = paths[pathIndex];
            if ("application".equals(path) || path.length() == 0 || "resources".equals(path)) { // NOI18N
            } else if (path.startsWith("resource[")) { // NOI18N
                int i = getIndex(path);
                if (i > -1) {
                    List<Resource> resources = getCurrentResources(app, current);
                    if (i < resources.size()) {
                        current = resources.get(i);
                        continue;
                    }
                }
                return null;
            } else if (path.startsWith("method[")) { // NOI18N
                int iTarget = getIndex(path);
                if (iTarget > -1) {
                    int i = 0;
                    for (Object o : current.getMethodOrResource()) {
                        if (o instanceof Method) {
                            if (i == iTarget) {
                                if (pathIndex == (paths.length -1)) {
                                    return (Method) o;
                                } else {
                                    return null;
                                }
                            }
                            if (i < iTarget) {
                                i++;
                            } else {
                                return null;
                            }
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    static List<Resource> getCurrentResources(Application app, Resource current) {
        if (current == null) {
            List<Resource> result = new LinkedList<Resource>();
            for (org.netbeans.modules.websvc.saas.model.wadl.Resources wadlResources : app
                    .getResources())
            {
                result.addAll(wadlResources.getResource());
            }
            return result;
        }
        List<Resource> result = new ArrayList<Resource>();
        for (Object o : current.getMethodOrResource()) {
            if (o instanceof Resource) {
                result.add((Resource)o);
            }
        }
        return result;
    }

    static int getIndex(String path) {
        int iOpen = path.indexOf('[');
        int iClose = path.indexOf(']');
        if (iOpen < 0 || iClose < 0 || iClose <= iOpen) {
            return -1;
        }
        try {
            return Integer.valueOf(path.substring(iOpen+1, iClose)) - 1; //xpath index is 1-based
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Set<String> getMediaTypesFromJAXBElement(List<JAXBElement<Representation>> repElements) {
        Set<String> result = new HashSet<String>();
        for (JAXBElement<Representation> repElement : repElements) {
            result.add(repElement.getValue().getMediaType());
        }
        return result;
    }

    public static Set<String> getMediaTypes(List<Representation> repTypes) {
        Set<String> result = new HashSet<String>();
        for (Representation repType : repTypes) {
            result.add(repType.getMediaType());
        }
        return result;
    }

    public static String getSignature(WadlSaasMethod method) {
        WadlSaas saas = method.getSaas();
        Resource[] paths = method.getResourcePath();
        Method m = method.getWadlMethod();

        StringBuilder sb = new StringBuilder();
        sb.append(m.getName());
        sb.append(" : "); // NOI18N
        sb.append(saas.getBaseURL());
        for (Resource r : paths) {
            sb.append(r.getPath());
            sb.append('/');
        }
        if (m.getRequest() != null && m.getRequest().getParam() != null) {
            Param[] params = m.getRequest().getParam().toArray(new Param[m.getRequest().getParam().size()]);
            if (params.length > 0) {
                sb.append(" ("); // NOI18N
            }
            for (int i=0 ; i < params.length; i++) {
                Param p = params[i];
                if (i > 0) {
                    sb.append(","); // NOI18N
                }
                if (p.getStyle() == ParamStyle.TEMPLATE) {
                    sb.append('{');
                    sb.append(p.getName());
                    sb.append('}');
                } else if (p.getStyle() == ParamStyle.QUERY) {
                    sb.append('?');
                    sb.append(p.getName());
                } else if (p.getStyle() == ParamStyle.MATRIX) {
                    sb.append('[');
                    sb.append(p.getName());
                    sb.append(']');
                } else if (p.getStyle() == ParamStyle.HEADER) {
                    sb.append('<');
                    sb.append(p.getName());
                    sb.append('>');
                } else {
                    sb.append(p.getName());
                }
            }
            if (params.length > 0) {
                sb.append(" )"); // NOI18N
            }
        }
        return sb.toString();
    }

    public static Image loadIcon(SaasGroup saasGroup, int type) {
        String path = saasGroup.getIcon16Path();
        if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            path =  saasGroup.getIcon32Path();
        }
        if (path != null) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            return ImageUtilities.loadImage(path);
        }
        return null;
    }

    public static String deriveFileName(String path) {
        String name = null;
        try {
            URL url = new URL(path);
            name = url.getPath();

        } catch(MalformedURLException e) {
        }
        if (name == null) {
            name = path;
        }
        name = name.substring(name.lastIndexOf('/')+1);
        return name;
    }

    public static FileObject extractWadlFile(WadlSaas saas) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(saas.getUrl());
        if (in == null) {
            return null;
        }
        OutputStream out = null;
        FileObject wadlFile;
        try {
            FileObject dir = saas.getSaasFolder();
            FileObject catalogDir = dir.getFileObject("catalog"); // NOI18N
            if (catalogDir == null) {
                catalogDir = dir.createFolder(CATALOG);
            }
            String wadlFileName = deriveFileName(saas.getUrl());
            wadlFile = catalogDir.getFileObject(wadlFileName);
            if (wadlFile == null) {
                wadlFile = catalogDir.createData(wadlFileName);
            }
            out = wadlFile.getOutputStream();
            FileUtil.copy(in, out);
        } finally {
            in.close();
            if (out != null) {
                out.close();
            }
        }
        return wadlFile;
    }

    public static Saas getServiceByUrl(SaasGroup group, String url) {
        for (Saas s : group.getServices()) {
            if (s.getUrl().equals(url)) {
                return s;
            }
        }
        return null;
    }

    public static String getWadlServiceDirName(String wadlUrl) {
            String urlPath = wadlUrl.replace('\\', '/');
            if (urlPath.endsWith(APPLICATION_WADL)) {
                urlPath = urlPath.substring(0, urlPath.length() - APPLICATION_WADL.length() - 1);
            }
            int start = urlPath.lastIndexOf("/") + 1; //NOI18N
            String name = urlPath.substring(start);
            if (name.endsWith(".wadl") || name.endsWith(".WADL")) { // NOI18N
                name = name.substring(0, name.length()- 5);
            }
            name = name.replace('.', '-');

            return name;
    }

    public static String ensureUniqueServiceDirName(String name) {
        String result = name;
        for (int i=0 ; i<1000 ; i++) {
            FileObject websvcHome = SaasServicesModel.getWebServiceHome();
            if (i > 0) {
                result = name + i;
            }
            if (websvcHome.getFileObject(result) == null) {
                try {
                    websvcHome.createFolder(result);
                } catch(IOException e) {
                    //ignore
                }
                break;
            }
        }
        return result;
    }

    public static FileObject retrieveWadlFile(WadlSaas saas) {
        try {
            FileObject saasFolder = saas.getSaasFolder();
            File catalogFile = new File(FileUtil.toFile(saasFolder), CATALOG);
            URI catalog  = catalogFile.toURI();
            URI wadlUrl = new URI(saas.getUrl());

            return getRetriever().retrieveResource(saasFolder, catalog, wadlUrl);

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private static Retriever getRetriever() {
        Retriever r = Lookup.getDefault().lookup(Retriever.class);
        if (r != null) {
            return r;
        }
        return Retriever.getDefault();
    }

    public static String getSaasType(String url) {
        String urlLowerCase = url.toLowerCase();
        if (urlLowerCase.endsWith(Saas.WSDL_EXT) || urlLowerCase.endsWith(Saas.ASMX_EXT)) {
            return Saas.NS_WSDL;
        }

        if (urlLowerCase.endsWith(Saas.NS_WADL)) {
            return Saas.NS_WADL;
        }
        else if (urlLowerCase.endsWith(Saas.NS_WADL_09)) {
            return Saas.NS_WADL_09;
        }

        try {
            InputStream is = new URI(url).toURL().openStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;

            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }

            String doc = os.toString("UTF-8");      //NOI18N
            if (doc.contains(Saas.NS_WSDL)) {
                return Saas.NS_WSDL;
            } 
            else if (doc.contains(Saas.NS_WADL)) {
                return Saas.NS_WADL;
            }
            else if (doc.contains(Saas.NS_WADL_09)) {
                return Saas.NS_WADL_09;
            }
        } catch (Exception ex) {
        }

        return null;
    }

    public static String filenameFromPath(String path) {
        return path.substring(path.lastIndexOf('/')+1);
    }

    public static String dirOnlyPath(String path) {
        int i = path.lastIndexOf('/');
        if (i > -1) {
            return path.substring(0, i);
        }
        return "";
    }

    public static  FileObject saveResourceAsFile(FileObject baseDir, String destPath, String resourcePath) throws IOException {
        FileObject destDir = FileUtil.createFolder(baseDir, destPath);
        return saveResourceAsFile(destDir, resourcePath);
    }

    public static FileObject saveResourceAsFile(FileObject destDir, String resourcePath) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        String filename = filenameFromPath(resourcePath);
        FileObject outFile = destDir.getFileObject(filename);
        if (outFile == null) {
            outFile = destDir.createData(filename);
        }
        OutputStream out = outFile.getOutputStream();
        if (in != null && out != null) {
            try {
                FileUtil.copy(in, out);
                return outFile;
            } finally {
                in.close();
                out.close();
            }
        }
        return null;
    }

    public static String toValidJavaName(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        if (Character.isJavaIdentifierStart(name.charAt(0))) {
            sb.append(name.charAt(0));
        }
        for (int i=1; i<name.length(); i++) {
            if (Character.isJavaIdentifierPart(name.charAt(i))) {
                sb.append(name.charAt(i));
            }
        }
        return sb.toString();
    }

    public static String deriveDefaultPackageName(Saas saas) {
        String pack1 = toValidJavaName(saas.getTopLevelGroup().getName());
        String pack2 = toValidJavaName(saas.getDisplayName());
        return (pack1 + "." + pack2).toLowerCase(); // NOI18N
    }
    
    private static Reader getWadlReader( InputStream inputStream ) 
            throws JAXBException
    {
        StringReader iReader = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            String xml = builder.toString();
            if (xml.contains(Saas.NS_WADL)) {
                xml = xml.replace(Saas.NS_WADL, Saas.NS_WADL_09);
            }
            iReader = new StringReader(xml);
        }
        catch (IOException e) {
            Logger.getLogger(SaasUtil.class.getName()).log(Level.WARNING, null,e);
            throw new JAXBException(e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                }
            }
        }
        return iReader;
    }
    
    public static interface ReadInputStream {
        
        <T> T loadJaxbObject(InputStream inputStream,  Class<T> type, 
                boolean includeAware) throws JAXBException;
    }
    
    private static class InputStreamJaxbReader implements ReadInputStream {

        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.saas.util.SaasUtil.ReadInputStream#loadJaxbObject(java.io.InputStream, java.lang.Class, boolean)
         */
        @Override
        public <T> T loadJaxbObject( InputStream inputStream, Class<T> type,
                boolean includeAware ) throws JAXBException
        {
            return SaasUtil.loadJaxbObject(inputStream, type, includeAware);
        }
        
    }
}