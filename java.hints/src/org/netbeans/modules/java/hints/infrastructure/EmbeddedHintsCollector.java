/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;

/**
 *
 * @author sdedic
 */

public class EmbeddedHintsCollector extends JavaParserResultTask<Parser.Result> {
    private static final Map<Snapshot, List<ErrorDescription>> hints = new WeakHashMap();
    
    public static void setAnnotations(Snapshot snap, List<ErrorDescription> descs) {
        if (snap.getMimePath().size() == 1) {
            Document doc = snap.getSource().getDocument(false);
            if (doc == null) {
                // the document may have disappeared before errors were computed.
                return;
            }
            HintsController.setErrors(doc, "java-hints", descs);
            return;
        }
        
        synchronized (hints) {
            hints.put(snap, descs);
        }
    }
    
    private boolean javaFound;
    
    private volatile boolean cancelled;
    
    public EmbeddedHintsCollector() {
        super(Phase.PARSED, TaskIndexingMode.DISALLOWED_DURING_SCAN);
    }
    
    private List<ErrorDescription>  allHints;
    
    private void collectResult(Snapshot snap) {
        List<ErrorDescription> partial = hints.get(snap);
        if (allHints == null) {
            allHints = new ArrayList<>();
        }
        if (partial != null) {
            allHints.addAll(partial);
        }
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        cancelled = false;
        final Snapshot mySnapshot = result.getSnapshot();
        if (mySnapshot.getMimePath().size() > 1) {
            // I do not want the inner mimetype
            return;
        }
        if (mySnapshot.getMimeType().equals("text/x-java")) {
            // ignore toplevel java
            return;
        }
        try {
            synchronized (hints) {
                for (Snapshot snap : hints.keySet()) {
                    if (snap.getSource().equals(mySnapshot.getSource())) {
                        collectResult(snap);
                    }
                }
            }
            if (cancelled) {
                return;
            }
            if (allHints != null) {
                HintsController.setErrors(result.getSnapshot().getSource().getDocument(false), "java-hints", allHints);
            }
        } finally {
            synchronized (hints) {
                hints.clear();
                allHints = null;
            }
        }
    }

    @Override
    public int getPriority() {
        return 10000;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
    
    
    @MimeRegistration(service = TaskFactory.class, mimeType = "")
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            if (snapshot.getMimePath().size() > 1) {
                // I do not want the inner mimetype
                return Collections.emptyList();
            }
            return Collections.singleton(new EmbeddedHintsCollector());
        }
        
    }
}
