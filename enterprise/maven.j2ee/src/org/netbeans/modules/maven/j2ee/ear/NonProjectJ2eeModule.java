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

package org.netbeans.modules.maven.j2ee.ear;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * a j2eemodule implementation that is not tied to a particular project but
 *  works only on top of ear's modules' artifacts.. will this work?
 * @author mkleint
 */
public class NonProjectJ2eeModule implements J2eeModuleImplementation2 {

    private static final String WAR = "war"; //NOI18N
    private static final String EAR = "ear"; //NOI18N
    private static final String EJB = "ejb"; //NOI18N
    private String moduleVersion;
    private Artifact artifact;


    public NonProjectJ2eeModule(Artifact art, String modVer) {
        artifact = art;
        moduleVersion = modVer;
    }
    
    @Override
    public String getModuleVersion() {
        return moduleVersion;
    }
    
    @Override
    public J2eeModule.Type getModuleType() {
        String type = artifact.getType();
        if (WAR.equals(type)) {
            return J2eeModule.Type.WAR;
        }
        if (EJB.equals(type)) {
            return J2eeModule.Type.EJB;
        }
        if (EAR.equals(type)) {
            return J2eeModule.Type.EAR;
        }
        //TODO what to do here?
        return J2eeModule.Type.CAR;
    }
    
    @Override
    public String getUrl() {
        return artifact.getFile().getName();
    }
    
    @Override
    public FileObject getArchive() throws IOException {
        return FileUtil.toFileObject(FileUtil.normalizeFile(artifact.getFile()));
    }
    
    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws IOException {
        return new ContentIterator(FileUtil.getArchiveRoot(getArchive()));
    }
    
    @Override
    public FileObject getContentDirectory() throws IOException {
        return null;
    }
    
    public RootInterface getDeploymentDescriptor(String location) {
        if ("application.xml".equals(location)) { //NOI18N
            location = J2eeModule.APP_XML;
        }
        if ("ejb-jar.xml".equals(location)) { //NOI18N
            location = J2eeModule.EJBJAR_XML;
        }
        if ("web.xml".equals(location)) { //NOI18N
            location = J2eeModule.WEB_XML;
        }

        InputStream str = null;
        try {
            JarFile fil = new JarFile(artifact.getFile());
            ZipEntry entry = fil.getEntry(location);
            if (entry != null) {
                str = fil.getInputStream(entry);
                return readBaseBean(str);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (str != null) {
                try {
                    str.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }
    
    
    private RootInterface readBaseBean(InputStream str) {
        String type = artifact.getType();
        if (WAR.equals(type)) {
            try {
                FileObject root = FileUtil.getArchiveRoot(getArchive());
                return org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getDDRoot(root.getFileObject(J2eeModule.WEB_XML));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (EJB.equals(type)) {
                try {
                    return org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getDDRoot(new InputSource(str));
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
        } else if (EAR.equals(type)) {
            try {
                return org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(new InputSource(str));
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        return null;
    }

    @Override
    public File getResourceDirectory() {
        return  null;
    }

    @Override
    public File getDeploymentConfigurationFile(String name) {
//       if (name == null) {
//            return null;
//        }
//        String path = provider.getConfigSupport().getContentRelativePath(name);
//        if (path == null) {
//            path = name;
//        }
        // here we don't really have access to the source deployment configs, as we operate on top of 
        // maven local repository binaries only..
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
        return null;
    }

    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == EjbJarMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        }
        if (type == WebAppMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getAnnotationMetadataModel();
            return model;
//        } else if (type == WebservicesMetadata.class) {
//            @SuppressWarnings("unchecked") // NOI18N
//            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
//            return model;
        }
        return null;
    }
        
    /**
     * The server plugin needs all models to be either merged on annotation-based. 
     * Currently only the web model does a bit of merging, other models don't. So
     * for web we actually need two models (one for the server plugins and another
     * for everyone else). Temporary solution until merging is implemented
     * in all models.
     */
    public synchronized MetadataModel<WebAppMetadata> getAnnotationMetadataModel() {
        return null;
    }
    
    
    // inspired by netbeans' webmodule codebase, not really sure what is the point
    // of the iterator..
    private static final class ContentIterator implements Iterator<J2eeModule.RootedEntry> {
        private ArrayList<FileObject> ch;
        private FileObject root;
        
        private ContentIterator(FileObject f) {
            this.ch = new ArrayList<FileObject>();
            ch.add(f);
            this.root = f;
        }
        
        @Override
        public boolean hasNext() {
            return ! ch.isEmpty();
        }
        
        @Override
        public J2eeModule.RootedEntry next() {
            FileObject f = ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                FileObject[] chArr = f.getChildren();
                for (int i = 0; i < chArr.length; i++) {
                    ch.add(chArr [i]);
                }
            }
            return new FSRootRE(root, f);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
        
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        private FileObject f;
        private FileObject root;
        
        FSRootRE(FileObject rt, FileObject fo) {
            f = fo;
            root = rt;
        }
        
        @Override
        public FileObject getFileObject() {
            return f;
        }
        
        @Override
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }
    
}
