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
