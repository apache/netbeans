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

package org.netbeans.modules.maven.execute.ui;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.execute.cmd.ExecMojo;
import org.netbeans.modules.maven.nodes.DependencyNode;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
class GotoPluginSourceAction extends AbstractAction {
    private final ExecMojo mojo;
    private final RunConfig config;

    @NbBundle.Messages(value = "ACT_GOTO_Plugin=Go to Plugin Mojo Source")
    public GotoPluginSourceAction(ExecMojo start, RunConfig conf) {
        putValue(NAME, Bundle.ACT_GOTO_Plugin());
        this.mojo = start;
        this.config = conf;
    }

    @Override
    @NbBundle.Messages(value = "TIT_GOTO_Plugin=Opening Plugin Mojo Sources")
    public void actionPerformed(ActionEvent e) {
        final AtomicBoolean cancel = new AtomicBoolean();
        org.netbeans.api.progress.BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                doLoad(cancel);
            }
        }, Bundle.TIT_GOTO_Plugin(), cancel, false);
    }

    private void doLoad(AtomicBoolean cancel) {
        final MavenEmbedder onlineEmbedder = EmbedderFactory.getOnlineEmbedder();
        Artifact art = onlineEmbedder.createArtifact(mojo.plugin.groupId, mojo.plugin.artifactId, mojo.plugin.version, "jar");
        Project prj = config.getProject();
        if (prj != null) {
            //todo what about build without project.. it's just create archetype one though..
            ProgressContributor contributor = BasicAggregateProgressFactory.createProgressContributor("multi-1");
            AggregateProgressHandle handle = BasicAggregateProgressFactory.createHandle("Downloading plugin sources", new ProgressContributor[]{contributor}, ProgressTransferListener.cancellable(), null);
            handle.start();
            try {
                ProgressTransferListener.setAggregateHandle(handle);
                NbMavenProject pr = prj.getLookup().lookup(NbMavenProject.class);
                onlineEmbedder.resolveArtifact(art, pr.getMavenProject().getPluginArtifactRepositories(), onlineEmbedder.getLocalRepository());
                if (art.getFile().exists() && !cancel.get()) {
                    Artifact sourceArt = DependencyNode.downloadJavadocSources(contributor, false, art, prj);
                    FileObject binaryRoot = FileUtil.toFileObject(art.getFile());
                    if (!cancel.get() && binaryRoot != null && FileUtil.isArchiveFile(binaryRoot)) {
                        binaryRoot = FileUtil.getArchiveRoot(binaryRoot);
                        if (!cancel.get()) {
                            String className = mojo.getImplementationClass();
                            if (className != null) {
                                FileObject fo = binaryRoot.getFileObject(className.replace(".", "/") + ".class");
                                if (!cancel.get() && fo != null) {
                                    try {
                                        DataObject dobj = DataObject.find(fo);
                                        if (dobj != null) {
                                            OpenCookie cookie = dobj.getLookup().lookup(OpenCookie.class);
                                            if (cookie != null) {
                                                cookie.open();
                                            }
                                        }
                                    } catch (DataObjectNotFoundException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    contributor.finish();
                }
            } catch (ArtifactResolutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ArtifactNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ThreadDeath d) {
                // download interrupted
            } catch (IllegalStateException ise) {
                //download interrupted in dependent thread. #213812
                if (!(ise.getCause() instanceof ThreadDeath)) {
                    throw ise;
                }
            } finally {
                handle.finish();
                ProgressTransferListener.clearAggregateHandle();
            }
        }
    }

}
