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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.lucene.document.Document;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IndexerField;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;

/** Just tracks what is being unpacked after a remote index has been downloaded. */
final class NotifyingIndexCreator extends AbstractIndexCreator {

    private RemoteIndexTransferListener listener;

    NotifyingIndexCreator() {
        super(NotifyingIndexCreator.class.getName(), Arrays.asList(MinimalArtifactInfoIndexCreator.ID));
    }

    void start(RemoteIndexTransferListener listener) {
        this.listener = listener;
    }

    void end() {
        listener = null;
    }

    @Override public void updateDocument(ArtifactInfo artifactInfo, Document document) {
        listener.unpackingProgress(artifactInfo.getGroupId() + ':' + artifactInfo.getArtifactId());
    }
    
    @Override public Collection<IndexerField> getIndexerFields() {
        return Collections.emptySet();
    }
    
    @Override public void populateArtifactInfo(ArtifactContext artifactContext) throws IOException {}
    
    @Override public boolean updateArtifactInfo(Document document, ArtifactInfo artifactInfo) {
        return false;
    }

}
