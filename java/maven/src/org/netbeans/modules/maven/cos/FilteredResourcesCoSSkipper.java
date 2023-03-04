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
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.DirectoryScanner;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.spi.cos.CompileOnSaveSkipper;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=CompileOnSaveSkipper.class)
public class FilteredResourcesCoSSkipper implements CompileOnSaveSkipper {

    @Override
    public boolean skip(RunConfig config, boolean includingTests, long timeStamp) {
       List<Resource> res = config.getMavenProject().getResources();
        for (Resource r : res) {
            if (r.isFiltering()) {
                if (hasChangedResources(r, timeStamp)) {
                    return true;
                }
                // if filtering resource not changed, proceed with CoS
                continue;
            }
        }
        if (includingTests) {
            res = config.getMavenProject().getTestResources();
            for (Resource r : res) {
                if (r.isFiltering()) {
                    if (hasChangedResources(r, timeStamp)) {
                        return true;
                    }
                    // if filtering resource not changed, proceed with CoS
                    continue;
                }
            }
        }
        return false;
    }
    
    public static final String[] DEFAULT_INCLUDES = {"**"};

    private boolean hasChangedResources(Resource r, long stamp) {
        String dir = r.getDirectory();
        File dirFile = FileUtil.normalizeFile(new File(dir));
  //      System.out.println("checkresource dirfile =" + dirFile);
        if (dirFile.exists()) {
            List<File> toCopy = new ArrayList<File>();
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(dirFile);
            //includes/excludes
            String[] incls = r.getIncludes().toArray(new String[0]);
            if (incls.length > 0) {
                ds.setIncludes(incls);
            } else {
                ds.setIncludes(DEFAULT_INCLUDES);
            }
            String[] excls = r.getExcludes().toArray(new String[0]);
            if (excls.length > 0) {
                ds.setExcludes(excls);
            }
            ds.addDefaultExcludes();
            ds.scan();
            String[] inclds = ds.getIncludedFiles();
//            System.out.println("found=" + inclds.length);
            for (String inc : inclds) {
                File f = new File(dirFile, inc);
                if (f.lastModified() >= stamp) { 
                    toCopy.add(FileUtil.normalizeFile(f));
                }
            }
            if (toCopy.size() > 0) {
                    //the case of filtering source roots, here we want to return false
                    //to skip CoS altogether.
                return true;
            }
        }
        return false;
    }
    
}
