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
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/** This interface allows project implementation to provide a custom
 * generator of ORM Java classes from a DB model. An instance of this
 * interface should be registered in project lookup. 
 *
 * If there is no instance the default generator will be used.
 *
 * @author Pavel Buzek
 */
public interface FacadeGenerator {
    static final String FACADE_SUFFIX = "Facade"; //NOI18N

    Set<FileObject> generate(final Project project,
            final Map<String, String> entityNames,
            final FileObject targetFolder,
            final String entityFQN,
            final String idClass,
            final String pkg,
            final boolean hasRemote,
            final boolean hasLocal,
            boolean overrideExisting) throws IOException;
}
