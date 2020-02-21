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

package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.cnd.api.xml.LineSeparatorDetector;
import org.netbeans.modules.cnd.api.xml.XMLDocWriter;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.MakeProjectImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.api.support.SmartOutputStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

public class ConfigurationXMLWriter extends XMLDocWriter {

    private final FileObject projectDirectory;
    private final MakeConfigurationDescriptor projectDescriptor;

    private CommonConfigurationXMLCodec encoder;

    public ConfigurationXMLWriter(FileObject projectDirectory, MakeConfigurationDescriptor projectDescriptor) {
        this.projectDirectory = projectDirectory;
        this.projectDescriptor = projectDescriptor;
    }

    public void write() throws IOException {
        if (projectDescriptor == null) {
            return;
        }

        String tag = CommonConfigurationXMLCodec.CONFIGURATION_DESCRIPTOR_ELEMENT;

        encoder = new ConfigurationXMLCodec(tag, null, projectDescriptor, null);
        assert projectDescriptor.getState() != State.READING;
        write(MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML); // NOI18N

        encoder = new AuxConfigurationXMLCodec(tag, projectDescriptor);
        write(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML); // NOI18N
    }

    /*
     * was: ConfigurationDescriptorHelper.storeDescriptor()
     */
    private void write(String relPath) throws IOException {
        setLineSeparator(new LineSeparatorDetector(projectDirectory.getFileObject(relPath), projectDirectory).getInitialSeparator());
        FileObject xml = FileUtil.createData(projectDirectory, relPath);
        try {
            org.openide.filesystems.FileLock lock = xml.lock();
            try {
                OutputStream os = SmartOutputStream.getSmartOutputStream(xml, lock);
                setMasterComment(((MakeProjectImpl) projectDescriptor.getProject()).getConfigurationXMLComment());
                write(os);
            }
            finally {
                lock.releaseLock();
            }
            String customizerId = projectDescriptor.getActiveConfiguration() == null ? null
                    : projectDescriptor.getActiveConfiguration().getCustomizerId();
            Lookups.forPath(MakeProjectTypeImpl.projectMetadataFactoryPath(customizerId)).lookupAll(ProjectMetadataFactory.class).forEach((f) -> {
                f.write(projectDirectory);
            });      
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
        encoder.encode(xes);
    }
}
