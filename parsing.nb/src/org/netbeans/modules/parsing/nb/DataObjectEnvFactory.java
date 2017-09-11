/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
