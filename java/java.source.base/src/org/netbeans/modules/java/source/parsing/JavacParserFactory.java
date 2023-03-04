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

package org.netbeans.modules.java.source.parsing;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.source.NoJavacHelper;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
@MimeRegistration(mimeType = "text/x-java", service = ParserFactory.class)
public class JavacParserFactory extends ParserFactory {
    private static final Logger LOG = Logger.getLogger(JavacParserFactory.class.getName());
    
    /** used by tests to ensure that all instances of parser were GCed */
    private static final Logger TIMER = Logger.getLogger("TIMER.JavacParser");

    public JavacParserFactory() {
        LOG.fine("JavacParserFactory created: " + this);
    }
    
    @Override
    public JavacParser createParser(final Collection<Snapshot> snapshots) {
        if (!NoJavacHelper.hasWorkingJavac())
            return null;
        assert snapshots != null;
        if (snapshots.size() == 1) {
            final FileObject fo = snapshots.iterator().next().getSource().getFileObject();
            try {
                if (fo == null) {
                    return null;
                }
                if (fo.getFileSystem().isDefault() && fo.getAttribute("javax.script.ScriptEngine") != null //NOI18N
                        && fo.getAttribute("template") == Boolean.TRUE) { //NOI18N
                    // Do not create javac parser for templates
                    return null;
                }
            } catch (FileStateInvalidException fsie) {}
        }
        JavacParser parser = new JavacParser(snapshots, false);
        if (TIMER.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "JavacParser");
            rec.setParameters(new Object[] { parser });
            TIMER.log(rec);
        }
        return parser;
    }

    public JavacParser createPrivateParser (final Snapshot snapshot) {
        return new JavacParser(snapshot != null && snapshot.getSource().getFileObject() != null ? Collections.singletonList(snapshot) : Collections.emptyList(), true);
    }
    
    public static JavacParserFactory getDefault () {
        final Lookup lookup = MimeLookup.getLookup (JavacParser.MIME_TYPE);
        return (JavacParserFactory)lookup.lookup (ParserFactory.class);       
    }

}
