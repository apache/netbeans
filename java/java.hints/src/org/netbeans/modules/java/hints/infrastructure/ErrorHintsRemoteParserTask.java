/**
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.remote.api.RemoteParserTask;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class ErrorHintsRemoteParserTask {

    public static final String KEY_ERRORS = "ErrorDescription[]";
    public static final String KEY_FIXES = "Fix[]";

    @ServiceProvider(service=RemoteParserTask.class)
    public static class Errors implements RemoteParserTask<ErrorShim[], CompilationInfo, Void> {
        @Override
        public Future<ErrorShim[]> computeResult(CompilationInfo info, Void additionalParam) throws IOException {
            ErrorHintsProvider ehp = new ErrorHintsProvider();
            return new SynchronousFuture<>(() -> {
                EditorCookie ec = info.getFileObject().getLookup().lookup(EditorCookie.class);
                Document doc = ec.openDocument();
                Map<Integer, ErrorDescription> id2Error = new HashMap<>();
                Function<ErrorDescription, ErrorShim> error2Shim = err -> {
                    ErrorShim shim = new ErrorShim(err);
                    id2Error.put(shim.callbackId, err);
                    return shim;
                };
                info.getFileObject().setAttribute(KEY_ERRORS, id2Error);
                return ehp.computeErrors(info, doc, "text/x-java") //TODO: mimetype?
                          .stream()
                          .map(error2Shim)
                          .toArray(v -> new ErrorShim[v]);
            }, () -> ehp.cancel());
        }
    }
    
    @ServiceProvider(service=RemoteParserTask.class)
    public static class GetFixes implements RemoteParserTask<FixShim[], CompilationInfo, Integer> {
        @Override
        public Future<FixShim[]> computeResult(CompilationInfo info, Integer id) throws IOException {
            AtomicBoolean cancel = new AtomicBoolean();
            return new SynchronousFuture<>(() -> {
                Map<Integer, ErrorDescription> id2Error = (Map<Integer, ErrorDescription>) info.getFileObject().getAttribute(KEY_ERRORS);
                ErrorDescription err = id2Error != null ? id2Error.get(id) : null;
                List<Fix> fixes;
                if (err != null) {
                    LazyFixList lfl = err.getFixes();
                    //XXX: hack:
                    if (lfl instanceof CreatorBasedLazyFixListBase) {
                        ((CreatorBasedLazyFixListBase) lfl).compute(info, cancel);
                    }
                    fixes = lfl.getFixes();
                } else {
                    fixes = Collections.emptyList();
                }
                Map<Integer, Fix> id2Fix = new HashMap<>();
                Function<Fix, FixShim> fix2Shim = fix -> {
                    FixShim shim = new FixShim(fix);
                    id2Fix.put(shim.callbackId, fix);
                    return shim;
                };
                info.getFileObject().setAttribute(KEY_FIXES, id2Fix);
                return fixes.stream()
                            .map(fix2Shim)
                            .toArray(v -> new FixShim[v]);
            }, () -> cancel.set(true));
        }
    }

    @ServiceProvider(service=RemoteParserTask.class)
    public static class ApplyFix implements RemoteParserTask<EditShim[], CompilationController, Integer> {

        @Override
        public Future<EditShim[]> computeResult(CompilationController cc, Integer id) throws IOException {
            CompletableFuture<EditShim[]> result = new CompletableFuture<>();
            try {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Map<Integer, Fix> id2Fix = (Map<Integer, Fix>) cc.getFileObject().getAttribute(KEY_FIXES);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                List<EditShim> edits = new ArrayList<>();
                DocumentListener l = new DocumentListener() {
                    @Override public void insertUpdate(DocumentEvent e) {
                        try {
                            edits.add(new EditShim(e.getOffset(), e.getOffset(), e.getDocument().getText(e.getOffset(), e.getLength())));
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    @Override public void removeUpdate(DocumentEvent e) {
                        edits.add(new EditShim(e.getOffset(), e.getOffset() + e.getLength(), ""));
                    }
                    @Override public void changedUpdate(DocumentEvent e) {}
                };
                doc.addDocumentListener(l);
                try {
                    id2Fix.get(id).implement();//TODO: change info
                } catch (Exception ex) {
                    throw new IOException(ex);
                } finally {
                    doc.removeDocumentListener(l);
                }

                SaveCookie sc = cc.getFileObject().getLookup().lookup(SaveCookie.class);
                sc.save();

                result.complete(edits.toArray(new EditShim[0]));
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
            return result;
        }
        
    }

    public static final class ErrorShim {
        public int start;
        public int end;
        public String description;

        public int callbackId;
        public boolean probablyContainsFixes;
        //TODO: other attributes...

        private static int nextError; //XXX: synchronization

        public ErrorShim(ErrorDescription err) {
            start = err.getRange().getBegin().getOffset();
            end = err.getRange().getEnd().getOffset();
            description = err.getDescription();
            callbackId = nextError++;
            probablyContainsFixes = err.getFixes().probablyContainsFixes();
        }
        
    }

    public static final class FixShim {
        public String text;
        public int callbackId;
        private static int nextFix;

        public FixShim(Fix fix) {
            this.text = fix.getText();
            this.callbackId = nextFix++;
        }
    }

    public static final class EditShim {
        public int replaceStart;
        public int replaceEnd;
        public String replaceText;

        public EditShim(int replaceStart, int replaceEnd, String replaceText) {
            this.replaceStart = replaceStart;
            this.replaceEnd = replaceEnd;
            this.replaceText = replaceText;
        }
        
    }

}
