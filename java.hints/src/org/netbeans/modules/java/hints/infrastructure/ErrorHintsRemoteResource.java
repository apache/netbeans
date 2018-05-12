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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.remote.api.Parser;
import org.netbeans.modules.java.source.remote.api.Parser.Config;
import org.netbeans.modules.java.source.remote.api.ResourceRegistration;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@Path("/errors")
public class ErrorHintsRemoteResource {
    
    @GET
    @Path("/get")
    public String errors(@QueryParam("parser-config") String config) throws IOException {
        Gson gson = new Gson();
        Config conf = gson.fromJson(config, Config.class);

        return Parser.runTask(conf, ci -> {
            ErrorHintsProvider ehp = new ErrorHintsProvider();
            EditorCookie ec = ci.getFileObject().getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            Map<Integer, ErrorDescription> id2Error = new HashMap<>();
            Function<ErrorDescription, ErrorShim> error2Shim = err -> {
                ErrorShim shim = new ErrorShim(err);
                id2Error.put(shim.callbackId, err);
                return shim;
            };
            ci.putCachedValue(ErrorDescription[].class, id2Error, CompilationInfo.CacheClearPolicy.ON_CHANGE);
            return gson.toJson(ehp.computeErrors(ci, doc, "text/x-java") //TODO: mimetype?
                                  .stream()
                                  .map(error2Shim)
                                  .toArray(v -> new ErrorShim[v]));
        });
    }
    
    @GET
    @Path("/fixes/get")
    public String fixes(@QueryParam("id") int id, @QueryParam("parser-config") String config) throws IOException {
        try {
        Gson gson = new Gson();
        Config conf = gson.fromJson(config, Config.class);

        return Parser.runTask(conf, ci -> {
            Map<Integer, ErrorDescription> id2Error = (Map<Integer, ErrorDescription>) ci.getCachedValue(ErrorDescription[].class);
            ErrorDescription err = id2Error != null ? id2Error.get(id) : null;
            List<Fix> fixes;
            if (err != null) {
                LazyFixList lfl = err.getFixes();
                //XXX: hack:
                if (lfl instanceof CreatorBasedLazyFixListBase) {
                    ((CreatorBasedLazyFixListBase) lfl).compute(ci, new AtomicBoolean());
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
            ci.putCachedValue(Fix[].class, id2Fix, CompilationInfo.CacheClearPolicy.ON_CHANGE);
            return gson.toJson(fixes.stream()
                                    .map(fix2Shim)
                                    .toArray(v -> new FixShim[v]));
        });
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @GET
    @Path("/fixes/apply")
    public String apply(@QueryParam("id") int id, @QueryParam("parser-config") String config) throws IOException {
        try {
        Gson gson = new Gson();
        Config conf = gson.fromJson(config, Config.class);

        return Parser.runTask(conf, ci -> {
            Map<Integer, Fix> id2Fix = (Map<Integer, Fix>) ci.getCachedValue(Fix[].class);
            Document doc = ci.getSnapshot().getSource().getDocument(true);
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
            } finally {
                doc.removeDocumentListener(l);
            }
            return gson.toJson(edits);
        });
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
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
    @ServiceProvider(service=ResourceRegistration.class)
    public static final class RegistrationImpl implements ResourceRegistration {

        @Override
        public Class<?> getResourceClass() {
            return ErrorHintsRemoteResource.class;
        }
        
    }
}
