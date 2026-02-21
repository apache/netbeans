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
package org.netbeans.modules.web.jsf.editor.facelets;

import com.sun.faces.spi.ConfigurationResourceProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * provider URLs of libraries defined in [javax|jakarta].faces.FACELETS_LIBRARIES context param of deployment descriptor
 *
 * @author marekfukala
 */
public class WebFaceletTaglibResourceProvider implements ConfigurationResourceProvider {

    private static final String FACELETS_LIBRARIES_OLD_PROPERTY_NAME = "facelets.LIBRARIES"; //NOI18N
    private static final String FACELETS_LIBRARIES_PROPERTY_NAME = "javax.faces.FACELETS_LIBRARIES"; //NOI18N
    private static final String FACELETS_LIBRARIES_JAKARTA_PROPERTY_NAME = "jakarta.faces.FACELETS_LIBRARIES"; //NOI18N

    private final WebModule wm;

    public WebFaceletTaglibResourceProvider(WebModule wm) {
        this.wm = wm;
    }

    @Override
    public Collection<URI> getResources(ServletContext ignored) {
        try {
            MetadataModel<WebAppMetadata> model = wm.getMetadataModel();
            String faceletsLibrariesList = null;
            if(model != null && model.isReady()) {
                // This is executed just in case that the model is ready. Otherwise it leads to uncancelable work.
                // Another reports against non-consistents result from the first and second invocation should be
                // consulted with tzetula for better options how to fix this. Related issue is bug #232878.
                faceletsLibrariesList = model.runReadAction(new MetadataModelAction<>() {
                    @Override
                    public String run(WebAppMetadata metadata) throws Exception {
                        //TODO can be init param specified by some annotation or the dd must be present?
                        WebApp ddRoot = metadata.getRoot();
                        if (ddRoot != null) {
                            InitParam[] contextParams = ddRoot.getContextParam();
                            for (InitParam param : contextParams) {
                                if (FACELETS_LIBRARIES_PROPERTY_NAME.equals(param.getParamName())
                                        || FACELETS_LIBRARIES_OLD_PROPERTY_NAME.equals(param.getParamName())
                                        || FACELETS_LIBRARIES_JAKARTA_PROPERTY_NAME.equals(param.getParamName())) {
                                    return param.getParamValue();
                                }
                            }
                        }
                        return null;
                    }
                });
            }

            FileObject webModuleRoot = wm.getDocumentBase();
            FileObject webInfBase = wm.getWebInf() == null ? null : wm.getWebInf().getParent();
            Collection<URI> librariesURIs = new ArrayList<>();
            if(faceletsLibrariesList != null) {
                StringTokenizer st = new StringTokenizer(faceletsLibrariesList, ";");
                while(st.hasMoreTokens()) {
                    String libraryPath = st.nextToken();
                    FileObject libraryFO = null;
                    if(webInfBase != null) {
                        //try to resolve according to the web-inf parent
                        libraryFO = webInfBase.getFileObject(libraryPath);
                    }
                    if(libraryFO == null) {
                        //try to resolve according to the web module root,
                        //in most cases a folder identical to the web-inf's parent folder
                        //but may not always be true
                        libraryFO = webModuleRoot != null ? webModuleRoot.getFileObject(libraryPath) : null; //WebModule may have no root if broken
                    }
                    if(libraryFO != null) {
                        URL url = URLMapper.findURL(libraryFO, URLMapper.INTERNAL);
                        if(url != null) {
                            try {
                                librariesURIs.add(new URI(url.toExternalForm()));
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(WebFaceletTaglibResourceProvider.class.getName()).log(Level.INFO, null, ex);
                            }
                        }
                    }
                }
            }
            return librariesURIs;

        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}
