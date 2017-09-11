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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
