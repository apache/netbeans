/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.html.editor.gsf;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlGSFParser extends Parser {

    private static final Logger TIMER = Logger.getLogger("TIMER.j2ee.parser"); // NOI18N
    
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private SyntaxAnalyzerResult result;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        parse(snapshot, event);
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        //avoid change of the mutable 'result' field during the following ternary operator execution.
        final SyntaxAnalyzerResult sar = result; 
        return cancelled.get() || (sar == null) ? null : HtmlParserResultAccessor.get().createInstance(sar);
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        if (CancelReason.SOURCE_MODIFICATION_EVENT == reason && event.sourceChanged()) {
            cancelled.set(true);
            result = null;
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    private void parse(Snapshot snapshot, SourceModificationEvent event) {
        cancelled.set(false);
        if (snapshot == null) {
            //#215101: calling "ParserManager.parseWhenScanFinished("text/html",someTask)" results into null snapshot passed here
            return;
        }

        HtmlSource source = new HtmlSource(snapshot);
        Source snapshotSource = snapshot.getSource();
        String sourceMimetype = snapshotSource != null ? snapshotSource.getMimeType() : snapshot.getMimeType(); //prefer source mimetype

        Collection<? extends HtmlExtension> exts = HtmlExtensions.getRegisteredExtensions(sourceMimetype);
        if (cancelled.get()) {
            return;
        }
        Collection<UndeclaredContentResolver> resolvers = new ArrayList<>();
        for (final HtmlExtension ex : exts) {
            resolvers.add(new UndeclaredContentResolver() {

                @Override
                public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
                    return ex.getUndeclaredNamespaces(source);
                }

                @Override
                public boolean isCustomTag(Named element, HtmlSource source) {
                    return ex.isCustomTag(element, source);
                }

                @Override
                public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
                    return ex.isCustomAttribute(attribute, source);
                }

            });
        }

        if (cancelled.get()) {
            return;
        }
        result = SyntaxAnalyzer.create(source).analyze(new AggregatedUndeclaredContentResolver(resolvers));

        if (TIMER.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "HTML parse result"); // NOI18N
            rec.setParameters(new Object[]{result});
            TIMER.log(rec);
        }

    }

    private static class AggregatedUndeclaredContentResolver implements UndeclaredContentResolver {

        private final Collection<UndeclaredContentResolver> resolvers;

        public AggregatedUndeclaredContentResolver(Collection<UndeclaredContentResolver> resolvers) {
            this.resolvers = resolvers;
        }

        @Override
        public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
            Map<String, List<String>> aggregated = new HashMap<>();
            for (UndeclaredContentResolver resolver : resolvers) {
                aggregated.putAll(resolver.getUndeclaredNamespaces(source));
            }
            return aggregated;
        }

        @Override
        public boolean isCustomTag(Named element, HtmlSource source) {
            for (UndeclaredContentResolver r : resolvers) {
                if (r.isCustomTag(element, source)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isCustomAttribute(Attribute attr, HtmlSource source) {
            for (UndeclaredContentResolver r : resolvers) {
                if (r.isCustomAttribute(attr, source)) {
                    return true;
                }
            }
            return false;
        }

    }

}
