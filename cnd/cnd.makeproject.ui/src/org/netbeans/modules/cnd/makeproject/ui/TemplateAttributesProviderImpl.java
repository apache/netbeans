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
package org.netbeans.modules.cnd.makeproject.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class TemplateAttributesProviderImpl implements CreateFromTemplateAttributesProvider {

    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class TemplateAttributesProviderFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(new TemplateAttributesProviderImpl(owner));
        }
    }
    private final MakeProject project;
    private FileEncodingQueryImplementation encodingQuery;
    private static final Logger LOG = Logger.getLogger(TemplateAttributesProviderImpl.class.getName());


    public TemplateAttributesProviderImpl(MakeProject project) {
        this.project = project;
    }

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String, String> values = new HashMap<>();
        Properties priv = project.getProjectProperties(false);
        Properties props = project.getProjectProperties(true);
        String licensePath = priv.getProperty(MakeProjectHelper.PROJECT_LICENSE_PATH_PROPERTY);
        if (licensePath == null) {
            licensePath = props.getProperty(MakeProjectHelper.PROJECT_LICENSE_PATH_PROPERTY);
        }
        if (licensePath != null) {
            FileObject fo = project.getHelper().resolveFileObject(licensePath);
            if (fo != null && fo.isValid()) {
                File file = FileUtil.toFile(fo);
                if (file == null) {
                    try {
                        file = File.createTempFile("license", ".txt"); // NOI18N
                        file.deleteOnExit();
                        try (OutputStream w = Files.newOutputStream(file.toPath())) {
                            InputStream r = fo.getInputStream();
                            while(true) {
                                int i = r.read();
                                if (i == -1) {
                                    break;
                                }
                                w.write(i);
                            }
                            r.close();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (file != null) {
                    values.put("licensePath", file.toURI().toString()); // NOI18N
                }
            } else {
                LOG.log(Level.INFO, "project.licensePath value not accepted - " + licensePath); // NOI18N
            }
        }
            
        String license = priv.getProperty(MakeProjectHelper.PROJECT_LICENSE_NAME_PROPERTY);
        if (license == null) {
            license = props.getProperty(MakeProjectHelper.PROJECT_LICENSE_NAME_PROPERTY);
        }
        if (license != null) {
            values.put("license", license); // NOI18N
        }
        if (encodingQuery == null) {
            encodingQuery = project.getLookup().lookup(FileEncodingQueryImplementation.class);
        }
        Charset charset = encodingQuery.getEncoding(target.getPrimaryFile());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }
        try {
            ProjectInformation info = ProjectUtils.getInformation(project);
            if (info != null) {
                String pname = info.getName();
                if (pname != null) {
                    values.put("name", pname);// NOI18N
                }
                String pdname = info.getDisplayName();
                if (pdname != null) {
                    values.put("displayName", pdname);// NOI18N
                }
            }
        } catch (Exception ex) {
            //not really important, just log.
            Logger.getLogger(TemplateAttributesProviderImpl.class.getName()).log(Level.FINE, "", ex);
        }
       if (values.isEmpty()) {
            return null;
        } else {
            return Collections.singletonMap("project", values); // NOI18N
        }
    }
}
