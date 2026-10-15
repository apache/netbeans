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
package org.netbeans.nbbuild.extlibs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 */
public class SetupLimitModules extends Task {

    private String limitModulesProperty;
    private String releaseVersion;
    private String excludedModules;
    private String nbjdkHome;
    private File cacheFile;

    public void setLimitModulesProperty(String limitModulesProperty) {
        this.limitModulesProperty = limitModulesProperty;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setExcludedModules(String excludedModules) {
        this.excludedModules = excludedModules;
    }

    public void setNbjdkHome(String nbjdkHome) {
        this.nbjdkHome = nbjdkHome;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    @Override
    public void execute() throws BuildException {
        try {
            Properties cache = new Properties();

            if (cacheFile != null && cacheFile.canRead()) {
                try (InputStream in = new FileInputStream(cacheFile)) {
                    cache.load(in);
                }
            }

            String cacheKey = nbjdkHome + "-" + releaseVersion;
            String limitedModules = cache.getProperty(cacheKey);

            if (limitedModules == null) {
                String antlibJar = SetupLimitModules.class
                                                    .getProtectionDomain()
                                                    .getCodeSource()
                                                    .getLocation()
                                                    .getPath();
                List<String> command = new ArrayList<>();
                command.add(new File(new File(nbjdkHome, "bin"), "java").getAbsolutePath());
                command.add("-classpath");
                command.add(antlibJar);
                command.add("org.netbeans.nbbuild.extlibs.SetupLimitModulesProbe");
                command.add(releaseVersion);
                command.addAll(Arrays.asList(excludedModules.split(",")));
                Process p = new ProcessBuilder(command).redirectError(Redirect.INHERIT).start();
                p.waitFor();
                StringBuilder limitModulesText = new StringBuilder();
                InputStream in = p.getInputStream();
                int r;
                while ((r = in.read()) != (-1)) {
                    limitModulesText.append((char) r);
                }
                limitedModules = limitModulesText.toString().trim();
                if (cacheFile != null) {
                    cache.put(cacheKey, limitedModules);

                    try (OutputStream out = new FileOutputStream(cacheFile)) {
                        cache.store(out, "");
                    }
                }
            }

            getProject().setNewProperty(limitModulesProperty, limitedModules);
        } catch (IOException | InterruptedException ex) {
            throw new BuildException(ex);
        }
    }

}
