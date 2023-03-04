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

package org.netbeans.modules.spring.beans;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.loader.SpringXMLConfigDataLoader;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Enumerations;

/**
 * A base class for unit tests using configuration file. Sets up the DataLoader,
 * MIMEResolver, etc.
 *
 * @author Andrei Badea
 */
public class ConfigFileTestCase extends NbTestCase {

    protected File configFile;

    public ConfigFileTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws IOException {
        MockServices.setServices(DataLoaderPoolImpl.class, MIMEResolverImpl.class);
        clearWorkDir();
        configFile = new File(getWorkDir(), "applicationContext.xml");
    }

    protected File createConfigFileName(String name) throws IOException {
        return new File(getWorkDir(), name);
    }

    protected SpringConfigModel createConfigModel(File... files) {
        SpringConfigFileModelManager fileModelManager = new SpringConfigFileModelManager();
        ConfigFileGroup group = ConfigFileGroup.create(Arrays.asList(files));
        return SpringConfigModelAccessor.getDefault().createSpringConfigModel(fileModelManager, group);
    }

    public static final class DataLoaderPoolImpl extends DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(new SpringXMLConfigDataLoader());
        }
    }

    // XXX better to find a way to use MIMEResolverImpl from o.n.core.
    public static final class MIMEResolverImpl extends MIMEResolver {

        @Override
        public String findMIMEType(FileObject fo) {
            try {
                // Nope, no FileEncodingQuery. It needs a DataObject and that needs a MIME type :-)
                File file = FileUtil.toFile(fo);
                if (file == null) {
                    return null;
                }
                String contents = TestUtils.copyFileToString(file);
                if (!contents.contains("http://www.springframework.org/schema/beans")) {
                    return null;
                }
                return SpringConstants.CONFIG_MIME_TYPE;
            } catch (IOException e) {
                throw (IllegalStateException)new IllegalStateException(e.getMessage()).initCause(e);
            }
        }
    }
}
