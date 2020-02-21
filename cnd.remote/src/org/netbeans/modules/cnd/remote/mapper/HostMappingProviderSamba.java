/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.mapper;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 */
/*package*/final class HostMappingProviderSamba implements HostMappingProvider {

    @Override
    public Map<String, String> findMappings(ExecutionEnvironment execEnv, ExecutionEnvironment otherExecEnv) {
        Map<String, String> mappings = new HashMap<>();
        ProcessUtils.ExitStatus exit = ProcessUtils.execute(execEnv, "cat", "/etc/sfw/smb.conf"); //NOI18N
        if (exit.isOK()) {
            mappings.putAll(parseOutput(new StringReader(exit.getOutputString())));
        }
        return mappings;
    }

    @Override
    public boolean isApplicable(PlatformInfo hostPlatform, PlatformInfo otherPlatform) {
        return otherPlatform.isWindows() && hostPlatform.isUnix();
    }

    private static final String GLOBAL = "global"; //NOI18N
    private static final String PATH = "path"; //NOI18N

    static Map<String, String> parseOutput(Reader outputReader) {
        Map<String, String> mappings = new HashMap<>();
        SimpleConfigParser parser = new SimpleConfigParser();
        parser.parse(outputReader);
        for (String name : parser.getSections()) {
            if (!GLOBAL.equals(name)) {
                String path = parser.getAttributes(name).get(PATH); //TODO: investigate case-sensitivity
                if (path != null) {
                    mappings.put(name, path);
                }
            }
        }
        return mappings;
    }
}
