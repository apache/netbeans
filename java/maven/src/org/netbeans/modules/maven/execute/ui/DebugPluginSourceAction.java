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

package org.netbeans.modules.maven.execute.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.debug.Utils;
import org.netbeans.modules.maven.execute.cmd.ExecMojo;
import org.netbeans.modules.maven.queries.MavenSourceJavadocAttacher;
import org.netbeans.modules.maven.queries.SourceJavadocByHash;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class DebugPluginSourceAction extends AbstractAction {
    private final ExecMojo mojo;
    private final RunConfig config;

    @NbBundle.Messages(value = "ACT_DEBUG_Plugin=Debug Plugin Mojo Source")
    public DebugPluginSourceAction(ExecMojo start, RunConfig conf) {
        putValue(NAME, Bundle.ACT_DEBUG_Plugin());
        this.mojo = start;
        this.config = conf;
    }

    @Override
    @NbBundle.Messages(value = "TIT_DEBUG_Plugin=Debugging Plugin Mojo")
    public void actionPerformed(ActionEvent e) {
        final AtomicBoolean cancel = new AtomicBoolean();
        org.netbeans.api.progress.BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                doLoad(cancel);
            }

        }, Bundle.TIT_DEBUG_Plugin(), cancel, false);
    }
    
    private void doLoad(final AtomicBoolean cancel) {
        URL[] urls = mojo.getClasspathURLs();
        String impl = mojo.getImplementationClass();
        if (urls != null) {
            List<URL> normalizedUrls = new ArrayList<URL>();
            //first download the source files for the binaries..
            MavenSourceJavadocAttacher attacher = new MavenSourceJavadocAttacher();
            for (URL url : urls) {
                //the urls are not normalized and can contain ../ path parts
                try {
                    url = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(Utilities.toFile(url.toURI())));
                    if (url == null) {
                        continue; //#242324
                    }
                    normalizedUrls.add(url);
                    List<? extends URL> ret = attacher.getSources(url, new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return cancel.get();
                        }
                    });
                    SourceForBinaryQuery.Result2 result = SourceForBinaryQuery.findSourceRoots2(url);
                    if (result.getRoots().length == 0 && !ret.isEmpty()) {
                        //binary not in repository, we need to hardwire the mapping here to have sfbq pick it up.
                        Set<File> fls = new HashSet<File>();
                        for (URL u : ret) {
                            File f = FileUtil.archiveOrDirForURL(u);
                            if (f != null) {
                                fls.add(f);
                            }
                        }
                        if (!fls.isEmpty()) {
                            SourceJavadocByHash.register(url, fls.toArray(new File[0]), false);
                        }
                    }
                    if (cancel.get()) {
                        return;
                    }
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (cancel.get()) {
                return;
            }
            ClassPath cp = Utils.convertToSourcePath(normalizedUrls.toArray(new URL[0]));
            RunConfig clone = RunUtils.cloneRunConfig(config);
            clone.setInternalProperty("jpda.additionalClasspath", cp);
            clone.setInternalProperty("jpda.stopclass", impl);
//stop method sometimes doesn't exist when inherited
            clone.setInternalProperty("jpda.stopmethod", "execute");
            clone.setProperty(Constants.ACTION_PROPERTY_JPDALISTEN, "maven");
            if (cancel.get()) {
                return;
            }
            RunUtils.run(clone);
        }
    }
}
