/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    private static final Map<Snapshot, List<ErrorDescription>> hints = new WeakHashMap<>();
    
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
