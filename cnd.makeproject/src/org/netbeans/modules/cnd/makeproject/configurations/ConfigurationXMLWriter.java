/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
