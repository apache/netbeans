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

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 */
public class JavacTask extends Javac {

    private static final String CLASS_ZIP_FILE_INDEX = "com.sun.tools.javac.zip.ZipFileIndex";  //NOI18N
    private static final String METHOD_CLOSE = "clearCache";    //NOI18N

    @Override
    public void execute() throws BuildException {
        final Project p = getProject();
        p.log("Overridden Javac task called", Project.MSG_DEBUG);

        boolean ensureBuilt =    p.getProperty("ensure.built.source.roots") != null
                              || Boolean.valueOf(p.getProperty("deploy.on.save"));
        
        if (ensureBuilt) {
            String[] srcdir = getSrcdir().list();
            boolean noBin = false;
            boolean wasBuilt = false;
            
            for (String path : srcdir) {
                File f = PropertyUtils.resolveFile(p.getBaseDir().getAbsoluteFile(), path);
                
                try {
                    Boolean built = BuildArtifactMapperImpl.ensureBuilt(Utilities.toURI(f).toURL(), getProject(), false, true);

                    if (built == null) {
                        noBin = true;
                        
                        if (wasBuilt) {
                            throw new BuildException("Cannot build classfiles for source directories: " + Arrays.asList(srcdir));
                        }
                    } else {
                        wasBuilt = true;

                        if (noBin) {
                            throw new BuildException("Cannot build classfiles for source directories: " + Arrays.asList(srcdir));
                        }
                        
                        if (!built) {
                            throw new UserCancel();
                        }
                    }
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }

            if (!wasBuilt) {
                try {
                    super.execute();
                } finally {
                    cleanUp(p);
                }
            }
        } else {
            if (CheckForCleanBuilds.cleanBuild.get() && getSrcdir() != null) {
		File apSources = null;
		for (Iterator<String> it = Arrays.asList(getCurrentCompilerArgs()).iterator(); it.hasNext(); ) {
		    if ("-s".equals(it.next()) && it.hasNext()) {
			apSources = PropertyUtils.resolveFile(p.getBaseDir().getAbsoluteFile(), it.next());
			break;
		    }
		}
                for (String path : getSrcdir().list()) {
                    File f = PropertyUtils.resolveFile(p.getBaseDir().getAbsoluteFile(), path);

		    if (f.equals(apSources)) {
			p.log("Not forcing rescan for AP source output: " + f.getAbsolutePath(), Project.MSG_VERBOSE);
			continue;
		    }

                    try {
                        p.log("Forcing rescan of: " + f.getAbsolutePath(), Project.MSG_VERBOSE);
                        IndexingManager.getDefault().refreshIndex(Utilities.toURI(f).toURL(), null, false);
                    } catch (MalformedURLException ex) {
                        p.log(ex.getMessage(), ex, Project.MSG_VERBOSE);
                    }
                }
            }
            try {
                super.execute();
            } finally {
                cleanUp(p);
            }
        }
    }


    private static void cleanUp (final Project p) {
        try {
            p.log("Cleaning ZipFileIndex cache", Project.MSG_DEBUG);    //NOI18N
            final Class<?> zipFileIndex = Class.forName(CLASS_ZIP_FILE_INDEX);
            final Method clean = zipFileIndex.getDeclaredMethod(METHOD_CLOSE);
            clean.setAccessible(true);
            clean.invoke(null);
            p.log("ZipFileIndex cache cleaned", Project.MSG_DEBUG);    //NOI18N
        } catch (ClassNotFoundException e) {
            p.log("ZipFileIndex clearCache failed", e, Project.MSG_VERBOSE);    //NOI18N
        } catch (NoSuchMethodException e) {
            p.log("ZipFileIndex clearCache failed", e, Project.MSG_VERBOSE);    //NOI18N
        } catch (SecurityException e) {
            p.log("ZipFileIndex clearCache failed", e, Project.MSG_WARN);    //NOI18N
        } catch (InvocationTargetException e) {
            p.log("ZipFileIndex clearCache failed", e, Project.MSG_WARN);    //NOI18N
        } catch (IllegalArgumentException e) {
            p.log("ZipFileIndex clearCache failed", e, Project.MSG_WARN);    //NOI18N
        } catch (IllegalAccessException e) {
            p.log("ZipFileIndex clearCache failed", e, Project.MSG_WARN);    //NOI18N
        }
    }

}
