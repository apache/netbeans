/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
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
