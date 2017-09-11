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
