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
package org.netbeans.modules.cnd.repository.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImplTest;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public class RepositoryTestSupport {

    public static boolean grep(String text, File file, StringBuilder fileContent) throws IOException {

        Pattern pattern = Pattern.compile(text);

        boolean found = false;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (fileContent != null) {
                fileContent.append(line).append("\n");
            }
            if (!found && pattern.matcher(line).find()) {
                found = true;
            }
        }
        return found;
    }

    public static void dumpCsmProject(CsmProject project, PrintStream printStream, boolean returnOnNonParsed) {
        Map<CharSequence, FileImpl> map = new TreeMap<CharSequence, FileImpl>();
        for (CsmFile f : project.getAllFiles()) {
            map.put(f.getAbsolutePath(), (FileImpl) f);
            if (!f.isParsed()) {
                if (returnOnNonParsed) {
                    return;
                }
                System.err.printf("not parsed on closing: %s\n", f.toString());
                CndUtils.threadsDump();
            }
        }
        CsmCacheManager.enter();
        try {
            for (FileImpl file : map.values()) {
                CsmTracer tracer = new CsmTracer(printStream);
                tracer.setDeep(true);
                tracer.setDumpTemplateParameters(false);
                tracer.setTestUniqueName(false);
                tracer.dumpModel(file);
            }        
            dumpCsmProjectContainers(project, printStream);
        } finally {
            CsmCacheManager.leave();
        }
    }

    public static void dumpCsmProjectContainers(CsmProject project, PrintStream printStream) {
        ModelImplTest.dumpProjectContainers(printStream, (ProjectBase) project, true);
    }
}
