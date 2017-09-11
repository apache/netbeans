/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.ui;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
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
        org.netbeans.api.progress.ProgressUtils.runOffEventDispatchThread(new Runnable() {
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
            ProgressContributor contributor = AggregateProgressFactory.createProgressContributor("multi-1");
            AggregateProgressHandle handle = AggregateProgressFactory.createHandle("Downloading plugin sources", new ProgressContributor[]{contributor}, ProgressTransferListener.cancellable(), null);
            handle.start();
            try {
                ProgressTransferListener.setAggregateHandle(handle);
                NbMavenProject pr = prj.getLookup().lookup(NbMavenProject.class);
                onlineEmbedder.resolve(art, pr.getMavenProject().getPluginArtifactRepositories(), onlineEmbedder.getLocalRepository());
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
