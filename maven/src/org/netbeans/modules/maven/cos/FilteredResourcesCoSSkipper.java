/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
