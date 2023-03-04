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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author mkuchtiak
 */
public class TransformerUtils {
    /** jax-ws.xml: configuration file for web services (JAX-WS)
     */
    public static final String JAX_WS_XML_PATH = "nbproject/jax-ws.xml"; // NOI18N
    /** jaxws-build.xml: build script containing wsimport/wsgen tasks, that is included to build-impl.xml
     */    
    public static final String JAXWS_BUILD_XML_PATH = "nbproject/jaxws-build.xml"; // NOI18N
    
    static final String GENFILES_PROPERTIES_PATH = "nbproject/genfiles.properties"; // NOI18N
    static final String KEY_SUFFIX_JAXWS_BUILD_CRC = ".stylesheet.CRC32";   // NOI18N

    static final String JAXWS_20_LIB = "jaxws20lib";                        // NOI18N
    static final String JAXWS_VERSION = "jaxwsversion";                     // NOI18N
    static final String XJC_ENCODING ="xjcencoding";                        // NOI18N
    
    /** xsl transformation utility for generating jaxws-build.xml script
    */ 
    public static void transformClients(final FileObject projectDirectory,
                                        final String jaxws_stylesheet_resource) throws java.io.IOException {
        transformClients(projectDirectory,jaxws_stylesheet_resource,false);
    }
    /** xsl transformation utility for generating jaxws-build.xml script
    */ 
    public static void transformClients(final FileObject projectDirectory,
            final String jaxws_stylesheet_resource,boolean setJaxWsVersion) 
            throws java.io.IOException 
    {
        final FileObject jaxws_xml = projectDirectory.getFileObject(JAX_WS_XML_PATH);
        if ( jaxws_xml == null ){
            return;
        }
        final FileObject jaxWsBuildScriptXml = FileUtil.createData(projectDirectory, 
                JAXWS_BUILD_XML_PATH);
        byte[] projectXmlData;
        
        try {
            projectXmlData = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<byte[]>() {
                public byte[] run() throws IOException {
                    InputStream is = jaxws_xml.getInputStream();
                    try {
                        return load(is);
                    } finally {
                        is.close();
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
        
        URL stylesheet = TransformerUtils.class.getResource(jaxws_stylesheet_resource);
        byte[] stylesheetData;
        InputStream is = stylesheet.openStream();
        try {
            stylesheetData = load(is);
        } finally {
            is.close();
        }
        
        final byte[] resultData;

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            StreamSource stylesheetSource = new StreamSource(
                    new ByteArrayInputStream(stylesheetData), stylesheet.toExternalForm());
            Transformer t = tf.newTransformer(stylesheetSource);
            if (setJaxWsVersion) {
                if(!isJAXWS21(projectDirectory)) {
                    t.setParameter(JAXWS_VERSION, JAXWS_20_LIB );
                }
            }
            if ( hasEncoding(FileOwnerQuery.getOwner(projectDirectory))){
                t.setParameter(XJC_ENCODING, Boolean.TRUE.toString());
            }
            File jaxws_xml_F = FileUtil.toFile(jaxws_xml);
            assert jaxws_xml_F != null;
            StreamSource jaxWsSource = new StreamSource(
                    new ByteArrayInputStream(projectXmlData), jaxws_xml_F.toURI().toString());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            t.transform(jaxWsSource, new StreamResult(result));
            resultData = result.toByteArray();
        } catch (TransformerException e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    FileLock lock1 = jaxWsBuildScriptXml.lock();
                    OutputStream os = null;
                    try {
                        os = jaxWsBuildScriptXml.getOutputStream(lock1);
                        os.write(resultData);
                    } finally {
                        lock1.releaseLock();
                        if (os!=null) os.close();
                    }
                    return Boolean.TRUE;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }        
    }
            
    /**
     * Load data from a stream into a buffer.
     */
    static byte[] load(InputStream is) throws IOException {
        int size = Math.max(1024, is.available()); // #46235
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
    
    private static boolean isJAXWS21(FileObject projectDirectory){
        Project project = FileOwnerQuery.getOwner(projectDirectory);
        if(project != null){
            JAXWSVersionProvider jvp = project.getLookup().lookup(JAXWSVersionProvider.class);
            if (jvp != null) {
                String version = jvp.getJAXWSVersion();
                if (version != null && !isVersionOK(version, "2.1")) { //NOI18N
                    return false;
                }
            }
        }
        // By default return true
        return true;
    }
    
    private static boolean hasEncoding(Project project){
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        boolean hasXjc = false;
        if (srcGroups != null ) {
            for(SourceGroup group : srcGroups){
                ClassPath classpath = ClassPath.getClassPath(
                        group.getRootFolder(), ClassPath.COMPILE);
                if ( classpath!= null && classpath.findResource(
                        "com/sun/tools/xjc/XJCTask.class")!= null)
                {
                    hasXjc = true;
                }
                if ( classpath== null){
                    return false;
                }
                FileObject fo = classpath.findResource(
                        "com/sun/tools/xjc/generator/bean/field/MessageBundle_it.properties"); //NOI18N    
                if ( fo!= null){
                    return true;
                }
            }
        }
        if ( hasXjc ){
            /* JEE server based project with XJC in classpath. This task has no 
             *  "encoding" attribute.
             */
            return false;    
        }
        else {
            /* 
             * Non JEE server based project ( J2SE ). It uses libs.jaxws21.classpath
             * property to define XJC classpath task. Project's classpath 
             * doesn't contain XJC class.
             */
            String jaxWs21 = PropertyUtils.getGlobalProperties().
                    getProperty("libs.jaxws21.classpath");
            ClassPath classPath = ClassPathSupport.createClassPath(jaxWs21);
            FileObject fo = classPath.findResource(
                    "com/sun/tools/xjc/generator/bean/field/MessageBundle_it.properties"); //NOI18N    
            return fo!= null;
        }
    }
    
    /** Find (maybe cached) CRC for a URL, using a preexisting input stream (not closed by this method). */
    static String getCrc32(InputStream is) throws IOException {
        return  computeCrc32(is);
    }
    
    /**
     * Compute the CRC-32 of the contents of a stream.
     * \r\n and \r are both normalized to \n for purposes of the calculation.
     */
    private static String computeCrc32(InputStream is) throws IOException {
        Checksum crc = new CRC32();
        int last = -1;
        int curr;
        while ((curr = is.read()) != -1) {
            if (curr != '\n' && last == '\r') {
                crc.update('\n');
            }
            if (curr != '\r') {
                crc.update(curr);
            }
            last = curr;
        }
        if (last == '\r') {
            crc.update('\n');
        }
        int val = (int)crc.getValue();
        String hex = Integer.toHexString(val);
        while (hex.length() < 8) {
            hex = "0" + hex; // NOI18N
        }
        return hex;
    }
    
    private static boolean isVersionOK(String version, String requiredVersion) {
        int len1 = version.length();
        int len2 = requiredVersion.length();
        for (int i=0;i<Math.min(len1, len2);i++) {
            if (version.charAt(i) < requiredVersion.charAt(i)) {
                return false;
            } else if (version.charAt(i) > requiredVersion.charAt(i)) {
                return true;
            }
        }
        if (len1 > len2) return true;
        else if (len1 < len2) return false;
        return true;
    }

}
