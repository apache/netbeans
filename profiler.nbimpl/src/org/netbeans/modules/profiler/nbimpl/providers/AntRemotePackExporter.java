/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.providers;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.spi.AntEvent;
import org.netbeans.modules.profiler.attach.spi.AbstractRemotePackExporter;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service=AbstractRemotePackExporter.class)
public class AntRemotePackExporter extends AbstractRemotePackExporter {
    private AntProjectCookie cookie;

    @Override
    public String export(String exportPath, String hostOS, String jvm) throws IOException {
        assert hostOS != null;
        assert jvm != null;
        
        AntTargetExecutor.Env env = new AntTargetExecutor.Env();
        env.setVerbosity(AntEvent.LOG_VERBOSE);
        Properties antProperties = new Properties();
        antProperties.setProperty("lib.dir", "../lib");

        antProperties.setProperty("dest.dir", adjustExportPath(exportPath)); //NOI18N
        env.setProperties(antProperties);
        AntTargetExecutor ate = AntTargetExecutor.createTargetExecutor(env);
        ate.execute(getCookie(), new String[]{"profiler-server-" + getPlatformShort(hostOS) + "-" + getJVMShort(jvm)}).result();
        
        return getRemotePackPath(exportPath, hostOS);
    }

    @Override
    public String getRemotePackPath(String exportPath, String hostOS) {
        return adjustExportPath(exportPath) + File.separator + "profiler-server-" + getPlatformShort(hostOS) + ".zip";
    }
    
    private String adjustExportPath(String exportPath) {
        return exportPath != null ? exportPath : System.getProperty("java.io.tmpdir");
    }
    
    private synchronized AntProjectCookie getCookie() throws IOException {
        if (cookie == null) {
            File antFile = InstalledFileLocator.getDefault().locate("remote-pack-defs/build.xml", "org-netbeans-lib-profiler", false); //NOI18N
            cookie = DataObject.find(FileUtil.toFileObject(antFile)).getCookie(AntProjectCookie.class);
        }
        return cookie;
    }
}
