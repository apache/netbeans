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
package org.netbeans.modules.websvc.saas.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.oauth.Metadata;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Include;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;

/**
 *
 * @author nam
 */
public class WadlSaas extends Saas {

    private Application wadlModel;
    private List<WadlSaasResource> resources;
    private FileObject wadlFile;
    private List<FileObject> schemaFiles;
    private List<FileObject> jaxbJars;
    private List<FileObject> jaxbSourceJars;

    public WadlSaas(SaasGroup parentGroup, SaasServices services) {
        super(parentGroup, services);
    }

    public WadlSaas(SaasGroup parent, String url, String displayName, String packageName) {
        super(parent, url, displayName, packageName);
        getDelegate().setType(NS_WADL_09);
    }

    public Application getWadlModel() throws IOException {
        if (wadlModel == null) {
            InputStream in = null;
            if (isUserDefined()) {
                if (getLocalWadlFile() != null) {
                    in = getLocalWadlFile().getInputStream();
                }
            } else {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(getUrl());
            }

            try {
                if (in != null) {
                    wadlModel = SaasUtil.loadWadl(in);
                }
            } catch (JAXBException ex) {
                String msg = NbBundle.getMessage(WadlSaas.class, "MSG_ErrorLoadingWadl", getUrl());
                IOException ioe = new IOException(msg);
                ioe.initCause(ex);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return wadlModel;
    }

    public List<WadlSaasResource> getResources() {
        if (resources == null) {
            resources = new ArrayList<WadlSaasResource>();
            try {
                for (org.netbeans.modules.websvc.saas.model.wadl.Resources wadlResources : 
                    getWadlModel().getResources()) 
                {
                    for (Resource r : wadlResources.getResource()) {
                        resources.add(new WadlSaasResource(this, null, r));
                    }
                }
            } 
            catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return Collections.<WadlSaasResource>emptyList();
            }
        }
        return new ArrayList<WadlSaasResource>(resources);
    }

    public FileObject getLocalWadlFile() {
        if (wadlFile == null) {
            try {
                if (isUserDefined()) {
                    String path = getProperty(PROP_LOCAL_SERVICE_FILE);
                    if (path != null) {
                        wadlFile = getSaasFolder().getFileObject(path);
                    }
                    if (wadlFile == null) {
                        wadlFile = SaasUtil.retrieveWadlFile(this);
                        if (wadlFile != null) {
                            path = FileUtil.getRelativePath(saasFolder, wadlFile);
                            setProperty(PROP_LOCAL_SERVICE_FILE, path);
                            save();
                        } else {
                            throw new IllegalStateException(NbBundle.getMessage(WadlSaas.class, "MSG_FailedToRetrieve") + " " + getUrl());
                        }
                    }
                } else {
                    wadlFile = SaasUtil.extractWadlFile(this);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return wadlFile;
    }

    @Override
    protected WadlSaasMethod createSaasMethod(Method m) {
        return new WadlSaasMethod(this, m);
    }

    @Override
    public void toStateReady(boolean synchronous) {
        if (wadlModel == null) {
            setState(State.INITIALIZING);
            if (synchronous) {
                toStateReady();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        toStateReady();
                    }
                });
            }
        }
    }

    private void toStateReady() {
        try {
            getWadlModel();
            setState(State.RETRIEVED);
        } catch (Exception ex) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage());
            DialogDisplayer.getDefault().notify(msg);
            setState(State.UNINITIALIZED);
            return;
        }

//        try {
//            compileSchemas();
        setState(State.READY);
//        } catch (IOException ioe) {
//            Exceptions.printStackTrace(ioe);
//        }
    }

    public String getBaseURL() {
        try {
            List<org.netbeans.modules.websvc.saas.model.wadl.Resources> wadlResources = 
                    getWadlModel().getResources();
            if ( wadlResources.size() >0 ){
                return wadlResources.get(0).getBase();
            }
            return null;
        } catch (IOException ioe) {
            // should not happen at this point
            return NbBundle.getMessage(WadlSaas.class, "LBL_BAD_WADL");
        }
    }

    @Override
    public void refresh() {
        super.refresh();
        if (wadlFile != null) {
            try {
                wadlFile.getParent().delete();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        wadlFile = null;
        wadlModel = null;
        resources = null;
        toStateReady(false);
    }

    public List<FileObject> getLocalSchemaFiles() throws IOException {
        if (wadlModel == null) {
            throw new IllegalStateException("Should transition state to at least RETRIEVED");
        }
        schemaFiles = new ArrayList<FileObject>();
        if (wadlModel.getGrammars() == null || wadlModel.getGrammars().getInclude() == null) {
            return schemaFiles;
        }
        FileObject wadlDir = getLocalWadlFile().getParent();
        for (Include include : wadlModel.getGrammars().getInclude()) {
            String uri = include.getHref();
            FileObject schemaFile = wadlDir.getFileObject(uri);
            if (schemaFile == null) {
                try {
                    URI xsdUri = new URI(getUrl()).resolve(uri);
                    String dirPath = SaasUtil.dirOnlyPath(uri);
                    schemaFile = SaasUtil.saveResourceAsFile(wadlDir, dirPath, xsdUri.getPath());
                } catch (URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (schemaFile != null) {
                schemaFiles.add(schemaFile);
            }
        }
        return schemaFiles;
    }

    public Metadata getOauthMetadata() throws IOException, JAXBException {
        if (wadlModel == null) {
            throw new IllegalStateException("Should transition state to at least RETRIEVED");
        }
        if (wadlModel.getGrammars() == null || wadlModel.getGrammars().getAny() == null) {
            return null;
        }
        List<Object> otherGrammars = wadlModel.getGrammars().getAny();
        for (Object g : otherGrammars) {
            if (g instanceof Element) {
                Element el = (Element)g;
                if ("http://netbeans.org/ns/oauth/metadata/1".equals(el.getNamespaceURI()) && //NOI18N
                        "metadata".equals(el.getLocalName())) { //NOI18N
                    JAXBContext jc = JAXBContext.newInstance("org.netbeans.modules.websvc.saas.model.oauth"); //NOI18N
                    Unmarshaller u = jc.createUnmarshaller();
                    JAXBElement<Metadata> jaxbEl = u.unmarshal(el, Metadata.class);
                    return jaxbEl.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public List<FileObject> getLibraryJars() {
        return jaxbJars;
    }

    public void setLibraryJars(List<FileObject> jaxbJars) {
        this.jaxbJars = jaxbJars;
    }

    public List<FileObject> getJaxbSourceJars() {
        return jaxbSourceJars;
    }

    public void setJaxbSourceJars(List<FileObject> jaxbSourceJars) {
        this.jaxbSourceJars = jaxbSourceJars;
    }
}
