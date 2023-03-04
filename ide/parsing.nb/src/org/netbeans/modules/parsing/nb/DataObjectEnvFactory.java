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

package org.netbeans.modules.parsing.nb;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.Schedulers;
import org.netbeans.modules.parsing.implspi.EnvironmentFactory;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * The DataObject-based implementation of SourceEnvironment. Refactored from the
 * original implementation in Parsing API
 * 
 * @author sdedic
 */
// Note: 40k is a random position, but less than 50k used in parsing.api tests. Anyone who depends
// on parsing.api tests will receive the testing environment.
@ServiceProvider(service = EnvironmentFactory.class, position = 40000) 
public final class DataObjectEnvFactory implements EnvironmentFactory {
    private static Map<String,Reference<Parser>> cachedParsers = new HashMap<String,Reference<Parser>>();    

    @Override
    public Lookup getContextLookup() {
        return Lookup.getDefault();
    }

    @Override
    public Collection<? extends Scheduler> getSchedulers(Lookup context) {
        return Schedulers.getSchedulers();
    }

    public Parser findMimeParser(Lookup context, final String mimeType) {
        Parser p = null;
        final Reference<Parser> ref = cachedParsers.get (mimeType);
        if (ref != null) {
            p = ref.get();
        }
        if (p == null) {
            final Lookup lookup = MimeLookup.getLookup (mimeType);
            final ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
            if (parserFactory == null) {
                throw new IllegalArgumentException("No parser for mime type: " + mimeType);
            }
            p = parserFactory.createParser(Collections.<Snapshot>emptyList());
            cachedParsers.put(mimeType, new SoftReference<Parser>(p));
        }
        return p;
    }
    
    @Override
    public Class<? extends Scheduler> findStandardScheduler(String schedulerName) {
        switch (schedulerName) {
            case "CURSOR_SENSITIVE_TASK_SCHEDULER": // NOI18N
                return CursorSensitiveScheduler.class;
            
            case "EDITOR_SENSITIVE_TASK_SCHEDULER": // NOI18N
                return CurrentDocumentScheduler.class;
                
            case "SELECTED_NODES_SENSITIVE_TASK_SCHEDULER": // NOI18N
                return SelectedNodesScheduler.class;
                
            default:
                return null;
        }
    }
    
    
    static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject)sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject)sdp).getPrimaryFile();
        }
        return null;
    }

    @Override
    public <T> T runPriorityIO (final Callable<T> r) throws Exception {
        assert r != null;
        return ProvidedExtensions.priorityIO(r);
    }

    @Override
    public SourceEnvironment createEnvironment(Source src, SourceControl control) {
        return new EventSupport(control);
    }
}
