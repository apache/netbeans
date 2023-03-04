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
import java.util.Set;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/** This interface allows project implementation to provide a custom
 * generator of ORM Java classes from a DB model. An instance of this
 * interface should be registered in project lookup. 
 *
 * If there is no instance the default generator will be used.
 *
 * @author Pavel Buzek
 */
public interface PersistenceGenerator {
    
    void init(WizardDescriptor wiz);
    
    void uninit();
    
    String getFQClassName(String tableName);
    
    String generateEntityName(String className);
    
    /** 
     * Generates entity beans / entity classes based on the model represented
     * by the given <code>helper</code>.
     * 
     * @param progressPanel the panel for displaying progress during the generation, 
     * or null if no panel should be displayed.
     * @param helper the helper that specifies the generation options 
     * @param dcschemafile the schema for generating.
     * @param progressContributor the progress contributor for the generation process.
     * 
     */
    void generateBeans(final ProgressPanel progressPanel,
            final RelatedCMPHelper helper,
            final FileObject dbschemaFile,
            final ProgressContributor progressContributor) throws IOException;
    
    /**
     * @return a set of <code>FileObject</code>s representing 
     * the generated classes or an empty set if no classes were generated, never null.
     */ 
    Set<FileObject> createdObjects();
}
