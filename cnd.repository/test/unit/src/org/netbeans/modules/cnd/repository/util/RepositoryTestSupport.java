/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
