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

package org.netbeans.modules.editor.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vita Stejskal
 */
@MimeLocation(subfolderName=GlyphGutterActionsProvider.GLYPH_GUTTER_ACTIONS_FOLDER_NAME, instanceProviderClass=GlyphGutterActionsProvider.class)
public final class GlyphGutterActionsProvider extends ActionsList implements InstanceProvider<GlyphGutterActionsProvider> {

    public static final String GLYPH_GUTTER_ACTIONS_FOLDER_NAME = "GlyphGutterActions"; //NOI18N
    
    public static List getGlyphGutterActions(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        GlyphGutterActionsProvider provider = MimeLookup.getLookup(mimePath).lookup(GlyphGutterActionsProvider.class);
        return provider == null ? Collections.emptyList() : provider.getActionsOnly();
    }
    
    public GlyphGutterActionsProvider() {
        super(null, false, false);
    }

    private GlyphGutterActionsProvider(List<FileObject> keys) {
        super(keys, false, false);
    }
    
    public GlyphGutterActionsProvider createInstance(List<FileObject> fileObjectList) {
        return new GlyphGutterActionsProvider(fileObjectList);
    }
}
