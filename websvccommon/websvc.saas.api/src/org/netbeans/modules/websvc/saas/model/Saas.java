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
package org.netbeans.modules.websvc.saas.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.net.URL;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifact;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifacts;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices.Header;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.CodeGen;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
public class Saas implements Comparable<Saas> {

    public static final String PROP_PARENT_GROUP = "parentGroup";
    public static final String PROP_STATE = "saasState";

    public static enum State {
        UNINITIALIZED,
        INITIALIZING,
        RETRIEVED,
        READY,
        REMOVED
    }
    public static final String NS_SAAS = "http://xml.netbeans.org/websvc/saas/services/1.0";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_WADL = "http://research.sun.com/wadl/2006/10";
    public static final String NS_WADL_09 = "http://wadl.dev.java.net/2009/02";
    //private static final String CUSTOM = "custom";
    public static final String ARTIFACT_TYPE_LIBRARY = "library";
    
    private static final String JAVA_TARGETS = "java,servlet,resource,jsp";     //NOI18N
    private static final String PHP_TARGETS = "php";        //NOI18N

    public static final String[] SUPPORTED_TARGETS = {JAVA_TARGETS, PHP_TARGETS};
    
    public static final String WSDL_EXT = "wsdl";      //NOI18N
    public static final String WADL_EXT = "wadl";      //NOI18N
    public static final String ASMX_EXT = "asmx";       //NOI18N
    public static final String XML_EXT = "xml";         //NOI18N
    
    public static final String[] SUPPORTED_EXTENSIONS = {WSDL_EXT, WADL_EXT, ASMX_EXT, XML_EXT};
    
    protected final SaasServices delegate;
    private SaasGroup parentGroup;
    private SaasGroup topGroup;
    private List<SaasMethod> saasMethods;
    private State state = State.UNINITIALIZED;
    protected FileObject saasFolder; // userdir folder to store customization and consumer artifacts

    private boolean userDefined = true;
    private List<FileObject> libraryJars; // library artifacts to add to consumer project classpath

    public Saas(SaasGroup parentGroup, SaasServices services) {
        this.delegate = services;
        this.parentGroup = parentGroup;
    }

    public Saas(SaasGroup parent, String url, String displayName, String packageName) {
        delegate = new SaasServices();
        delegate.setUrl(url);
        delegate.setDisplayName(displayName);

        SaasMetadata m = new SaasMetadata();
        delegate.setSaasMetadata(m);
        
        CodeGen cg = new CodeGen();
        updateArtifacts(cg);
        cg.setPackageName(packageName);
        m.setCodeGen(cg);
        
        setParentGroup(parent);
        computePathFromRoot();
    }

    public SaasServices getDelegate() {
        return delegate;
    }

    public SaasGroup getParentGroup() {
        return parentGroup;
    }

    protected void setParentGroup(SaasGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public SaasGroup getTopLevelGroup() {
        if (topGroup == null && parentGroup != null) {
            topGroup = parentGroup;
            while (topGroup != null && topGroup.getParent() != SaasServicesModel.getInstance().getRootGroup()) {
                topGroup = topGroup.getParent();
            }

            if (topGroup == null) {
                topGroup = SaasServicesModel.getInstance().getRootGroup();
            }
        }
        return topGroup;
    }

    protected void computePathFromRoot() {
        delegate.getSaasMetadata().setGroup(parentGroup.getPathFromRoot());
    }
    protected FileObject saasFile;

    public FileObject getSaasFile() throws IOException {
        if (saasFile == null) {
            FileObject folder = getSaasFolder();
            String filename = folder.getName() + "-saas.xml"; //NOI18N

            saasFile = folder.getFileObject(filename);
            if (saasFile == null) {
                saasFile = getSaasFolder().createData(filename);
            }
        }
        return saasFile;
    }

    public void save() {
        try {
            SaasUtil.saveSaas(this, getSaasFile());
            if (getProperties().size() > 0) {
                java.io.OutputStream out = null;
                try {
                    out = getPropFile(true).getOutputStream();
                    getProperties().store(out, getDisplayName() + " : " + getUrl());
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    protected void setUserDefined(boolean v) {
        if (userDefined) {
            userDefined = v;
        }
    }

    public String getUrl() {
        return delegate.getUrl();
    }

    public State getState() {
        return state;
    }

    protected synchronized void setState(State v) {
        State old = state;
        state = v;
        SaasServicesModel.getInstance().fireChange(PROP_STATE, this, old, state);
    }

    /**
     * Asynchronous call to transition Saas to READY state; mainly for UI usage
     * Sub-class need to completely override as needed, without calling super().
     */
    public void toStateReady(boolean synchronous) {
        if (synchronous) {
            setState(state);
        } else {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    setState(State.READY);
                }
            });
        }
    }

    public SaasMetadata getSaasMetadata() {
        return delegate.getSaasMetadata();
    }

    public List<SaasMethod> getMethods() {
        if (saasMethods == null) {
            saasMethods = new ArrayList<SaasMethod>();
            if (delegate.getMethods() != null && delegate.getMethods().getMethod() != null) {
                for (Method m : delegate.getMethods().getMethod()) {
                    saasMethods.add(createSaasMethod(m));
                }
            }
        }
        
        return new ArrayList<SaasMethod>(saasMethods);
    }

    protected SaasMethod createSaasMethod(Method method) {
        return new SaasMethod(this, method);
    }

    public Header getHeader() {
        return delegate.getHeader();
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public String getApiDoc() {
        return delegate.getApiDoc();
    }

    public synchronized FileObject getSaasFolder() {
        if (saasFolder == null) {
            String folderName = SaasUtil.toValidJavaName(getDisplayName());
            saasFolder = SaasServicesModel.getWebServiceHome().getFileObject(folderName);
            if (saasFolder == null) {
                try {
                    saasFolder = SaasServicesModel.getWebServiceHome().createFolder(folderName);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return saasFolder;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    protected void refresh() {
        setState(State.INITIALIZING);
        saasMethods = null;
    }
    private Properties props;
    public static final String SAAS_PROPERTIES = "saas.properties";

    private Properties getProperties() throws IOException {
        if (props == null) {
            props = new Properties();
            FileObject fo = getPropFile(false);
            if (fo != null) {
                InputStream in = getPropFile(false).getInputStream();
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
            }
        }
        return props;
    }
    public static final String PROP_LOCAL_SERVICE_FILE = "local.service.file";
    private FileObject propFile;

    private FileObject getPropFile(boolean create) throws IOException {
        if (propFile == null) {
            propFile = getSaasFolder().getFileObject(SAAS_PROPERTIES);
            if (propFile == null && create) {
                propFile = getSaasFolder().createData(SAAS_PROPERTIES);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
        return propFile;
    }

    protected String getProperty(String name) {
        try {
            return getProperties().getProperty(name);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }

    protected void setProperty(String name, String value) {
        try {
            getProperties().setProperty(name, value);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    /**
     * @return absolute paths to all library jars, generated or
     * provided by vendor module.  Generated
     */
    public List<FileObject> getLibraryJars() {
        if (getState() != State.READY) {
            throw new IllegalStateException("Should only access libraries when in ready state");
        }

        if (libraryJars == null) {
            libraryJars = new ArrayList<FileObject>();
            if (getSaasMetadata() != null && getSaasMetadata().getCodeGen() != null) {
                for (Artifacts arts : getSaasMetadata().getCodeGen().getArtifacts()) {
                    for (Artifact art : arts.getArtifact()) {
                        if (ARTIFACT_TYPE_LIBRARY.equals(art.getType())) {
                            try {
                                URL url = new URL(art.getUrl());
                                libraryJars.add(FileUtil.toFileObject(new File(url.toURI())));
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(libraryJars);
    }

    public String getPackageName() {
//        if (getSaasMetadata() != null && getSaasMetadata().getCodeGen() != null) {
//            return getSaasMetadata().getCodeGen().getPackageName();
//        }
        return SaasUtil.deriveDefaultPackageName(this);
    }

    public void upgrade() {
        if (!userDefined) return;
        
        boolean needsSave = false;
        
        SaasMetadata m = delegate.getSaasMetadata();       
        if (m == null) {
            m = new SaasMetadata();
            delegate.setSaasMetadata(m);
            needsSave = true;
        }
     
        CodeGen cg = m.getCodeGen();
        if (cg == null) {
            cg = new CodeGen();
            m.setCodeGen(cg);
            needsSave = true;
        }
        
        if (updateArtifacts(cg)) {
            needsSave = true;
        }
        
        if (needsSave) {
            save();
        }
    }
    
    private boolean updateArtifacts(CodeGen cg) {
        List<Artifacts> list = cg.getArtifacts();
        int size = list.size();
        boolean needsSave = false;
        
        for (int i = 0; i < SUPPORTED_TARGETS.length; i++) {
            Artifacts artifacts = null;
            String targets = SUPPORTED_TARGETS[i];
            
            if (i < size) {
                artifacts = list.get(i);
                if (!targets.equals(artifacts.getTargets())) {
                    artifacts.setTargets(targets);
                    needsSave = true;
                }
            } else {
                artifacts = new Artifacts();
                artifacts.setTargets(targets);
                list.add(artifacts);
                needsSave = true;
            }
        }
        
        return needsSave;
    }
    
    public int compareTo(Saas saas) {
        return getDisplayName().compareTo(saas.getDisplayName());
    }
}
