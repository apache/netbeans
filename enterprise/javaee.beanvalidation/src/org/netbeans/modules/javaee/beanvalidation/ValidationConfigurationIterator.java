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

package org.netbeans.modules.javaee.beanvalidation;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author alexey butenko
 */
public class ValidationConfigurationIterator extends AbstractIterator {

    private static final String defaultName = "validation";   //NOI18N

    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);

        FileObject fo = DDHelper.createValidationXml(Profile.JAVA_EE_6_FULL, targetDir, targetName);
        if (fo != null) {
            return Collections.singleton(DataObject.find(fo));
        } else {
            return Collections.<DataObject>emptySet();
        }
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

}
