/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer;

import com.google.inject.Inject;
import java.io.File;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoRecord;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.context.IndexingContext;

/** Adapted from org.netbeans:nexus-for-netbeans. */
public final class CustomArtifactContextProducer extends DefaultArtifactContextProducer {

    @Inject
    public CustomArtifactContextProducer(ArtifactPackagingMapper mapper) {
        super(mapper);
    }

    @Override public ArtifactContext getArtifactContext(IndexingContext context, File file) {
        ArtifactContext ac = super.getArtifactContext(context, file);
        if (ac != null) {
            final ArtifactInfo ai = ac.getArtifactInfo();
            String fext = ai.getFileExtension();
            if (fext != null) {
                if (fext.endsWith(".lastUpdated")) {
                    // #197670: why is this even considered?
                    return null;
                }
                // Workaround for anomalous classifier behavior of nbm-maven-plugin:
                if (fext.equals("nbm")) {
                    return new ArtifactContext(ac.getPom(), ac.getArtifact(), ac.getMetadata(), new ArtifactInfo(ai.getRepository(), ai.getGroupId(), ai.getArtifactId(), ai.getVersion(), ai.getClassifier(), fext) {
                        private String uinfo = null;
                        @Override public String getUinfo() {
                            if (uinfo == null) {
                                uinfo = new StringBuilder().
                                        append(ai.getGroupId()).append(ArtifactInfoRecord.FS).
                                        append(ai.getArtifactId()).append(ArtifactInfoRecord.FS).
                                        append(ai.getVersion()).append(ArtifactInfoRecord.FS).
                                        append(ArtifactInfoRecord.NA).append(ArtifactInfoRecord.FS).
                                        append(ai.getPackaging()).toString();
                            }
                            return uinfo;
                        }
                    }, ac.getGav());
                }
            }
        }
        return ac;
    }

}
