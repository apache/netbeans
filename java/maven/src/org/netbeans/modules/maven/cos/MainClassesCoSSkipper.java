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


package org.netbeans.modules.maven.cos;

import java.io.File;
import org.codehaus.plexus.util.DirectoryScanner;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.spi.cos.CompileOnSaveSkipper;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=CompileOnSaveSkipper.class)
public class MainClassesCoSSkipper implements CompileOnSaveSkipper {

    @Override
    public boolean skip(RunConfig config, boolean includingTests, long timeStamp) {
        if (includingTests) {
            if (!RunUtils.hasApplicationCompileOnSaveEnabled(config) && RunUtils.hasTestCompileOnSaveEnabled(config)) {
                //in case when only tests are enabled for CoS, the main source root is not compiled on the fly.
                // we need to checkif something was changed there and if so, recompile manually.
                
                //TODO is there a way to figure if there is a modified java file in a simpler way?
                File dirFile = FileUtilities.convertStringToFile(config.getMavenProject().getBuild().getSourceDirectory());
                if (dirFile == null || !dirFile.exists()) { //#223461
                    return false;
                }
                DirectoryScanner ds = new DirectoryScanner();
                ds.setBasedir(dirFile);
                //includes/excludes
                ds.setIncludes(new String[]{"**/*.java"});
                ds.addDefaultExcludes();
                ds.scan();
                String[] inclds = ds.getIncludedFiles();
                for (String inc : inclds) {
                    File f = new File(dirFile, inc);
                    if (f.lastModified() >= timeStamp) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
