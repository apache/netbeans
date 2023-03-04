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
