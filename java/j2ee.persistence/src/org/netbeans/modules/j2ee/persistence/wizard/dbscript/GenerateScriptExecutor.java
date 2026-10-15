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
package org.netbeans.modules.j2ee.persistence.wizard.dbscript;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Executes save script.
 */
public class GenerateScriptExecutor {

    public void execute(Project project, FileObject file, PersistenceEnvironment pe, PersistenceUnit pu, HashMap map, List<String> problems, ProgressHandle handle, boolean validateOnly) {
        try {

            Class pClass = Thread.currentThread().getContextClassLoader().loadClass("javax.persistence.Persistence");//NOI18N
            javax.persistence.Persistence p = (javax.persistence.Persistence) pClass.getDeclaredConstructor().newInstance();

            //
            map.put("javax.persistence.schema-generation.scripts.action", "create");
            //map.put("javax.persistence.schema-generation-target", "scripts");
            if(!validateOnly) {
                try {
                    map.put("javax.persistence.schema-generation.scripts.create-target", new FileWriter(FileUtil.toFile(file)));
                } catch (IOException ex) {
                    problems.add( NbBundle.getMessage(GenerateScriptExecutor.class, "ERR_File", file.getPath()));
                }
                //
                handle.progress(NbBundle.getMessage(DBScriptWizard.class, "MSG_ScriptGeneration"),15);
                p.generateSchema(pu.getName(), map);
                handle.progress(95);
            }
        } catch (ReflectiveOperationException ex) {
                problems.add( NbBundle.getMessage(GenerateScriptExecutor.class, "ERR_Classpath", file.getPath()));
        }

    }
}
