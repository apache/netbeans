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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author Andrei Badea
 */
public class PersistenceGeneratorImpl implements PersistenceGenerator {
    
    @Override
    public String generateEntityName(String className) {
        return className;
    }

    @Override
    public void init(WizardDescriptor wiz) {
    }

    @Override
    public void generateBeans(final ProgressPanel progressPanel, final RelatedCMPHelper helper, final FileObject dbschemaFile, final ProgressContributor handle) throws IOException {
    }

    @Override
    public void uninit() {
    }

    public boolean isCMP() {
        return false;
    }

    @Override
    public Set<FileObject> createdObjects() {
        return Collections.<FileObject>emptySet();
    }

    @Override
    public String getFQClassName(String tableName) {
        return null;
    }

}
