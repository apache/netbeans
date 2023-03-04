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
package org.netbeans.modules.java.hints.providers.spi;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper.DocumentVersion;

/**TODO: should be public?
 *
 * @author lahvac
 */
public abstract class PositionRefresherHelper<V extends DocumentVersion> {

    private final Object documentKey = new Object();
    private final String key;

    protected PositionRefresherHelper(String key) {
        this.key = key;
    }

    protected abstract boolean isUpToDate(Context context, Document doc, V oldVersion);
    /**XXX: should be protected*/public abstract @CheckForNull List<ErrorDescription> getErrorDescriptionsAt(CompilationInfo info, Context context, Document doc) throws Exception;

    protected final void setVersion(Document doc, V version) {
        if (doc != null) {
            doc.putProperty(documentKey, version);
        }
    }

    @SuppressWarnings("unchecked")
    protected @CheckForNull V getUpToDateDocumentVersion(Context context, Document doc) {
        V oldVersion = (V) doc.getProperty(documentKey);

        if (oldVersion == null) return null;

        if (((DocumentVersion) oldVersion).version != DocumentUtilities.getDocumentVersion(doc)) return null;
        
        return oldVersion;
    }
    
    /**XXX*/ public boolean upToDateCheck(Context context, Document doc) {
        V oldVersion = getUpToDateDocumentVersion(context, doc);

        if (oldVersion == null) return false;

        return isUpToDate(context, doc, oldVersion);
    }

    /**XXX*/ public String getKey() {
        return key;
    }

    public static class DocumentVersion {
        private final long version;

        public DocumentVersion(Document doc) {
            this.version = doc != null ? DocumentUtilities.getDocumentVersion(doc) : 0;
        }

    }
}
