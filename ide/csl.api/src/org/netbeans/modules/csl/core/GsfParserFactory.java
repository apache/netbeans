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

package org.netbeans.modules.csl.core;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.filesystems.FileObject;

/**
 * Registers parsers provided by GSF-based languages with the Parsing API.
 * 
 * @author Vita Stejskal
 */
public final class GsfParserFactory extends ParserFactory {

    public static ParserFactory create(FileObject f) {
        String mimeType = f.getParent().getPath().substring("Editors/".length()); //NOI18N
        return new GsfParserFactory(mimeType);
    }

    public @Override Parser createParser(Collection<Snapshot> snapshots) {
        for(Snapshot s : snapshots) {
            MimePath p = MimePath.get(s.getMimeType());
            String inhType = p.getInheritedType();
            if (!(p.getMimeType(0).equals(mimeType) ||
                  mimeType.equals(inhType))) {
                return null;
            }
        }

        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        assert l != null : "No CSL language registered for " + mimeType; //NOI18N
        return l == null ? null : l.getParser(snapshots);
    }

    private final String mimeType;

    private GsfParserFactory(String mimeType) {
        this.mimeType = mimeType;
    }

}
