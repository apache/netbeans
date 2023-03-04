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

package org.netbeans.modules.java.hints.spiimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper.DocumentVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Refreshes all Java Hints on current line upon Alt-Enter or mouseclick
 * @author Max Sauer
 */
@MimeRegistration(mimeType="text/x-java", service=PositionRefresher.class)
public class JavaHintsPositionRefresher implements PositionRefresher {

    private static final Logger LOG = Logger.getLogger(JavaHintsPositionRefresher.class.getName());

    @Override
    public Map<String, List<ErrorDescription>> getErrorDescriptionsAt(final Context context, final Document doc) {

        @SuppressWarnings("unchecked")
        Collection<? extends PositionRefresherHelper<? extends DocumentVersion>> col = MimeLookup.getLookup("text/x-java").lookupAll(PositionRefresherHelper.class);
        final List<? extends PositionRefresherHelper<? extends DocumentVersion>> refreshers = new ArrayList<>(col);

        for (Iterator<? extends PositionRefresherHelper<?>> it = refreshers.iterator(); it.hasNext();) {
            PositionRefresherHelper<?> h = it.next();

            if (h.upToDateCheck(context, doc)) {
                LOG.log(Level.FINE, "Not computing warnings for {0}, results are up-to-date.", h.getKey());
                it.remove();
            } else {
                LOG.log(Level.FINE, "Will compute warnings for {0}, results not up-to-date.", h.getKey());
            }
        }

        if (refreshers.isEmpty()) return Collections.emptyMap();
        
        final JavaSource js = JavaSource.forDocument(doc);

        if (js == null) {
            LOG.log(Level.FINE, "No JavaSource associated to: {0}", new Object[] {doc, doc.getProperty(Document.StreamDescriptionProperty)});
            return Collections.emptyMap();
        }

        final Map<String, List<ErrorDescription>> eds = new HashMap<>();

        Runnable r = () -> {
            try {
                js.runUserActionTask(new RefreshTask(eds, refreshers, context), true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        
        BaseProgressUtils.runOffEventDispatchThread(r, NbBundle.getMessage(JavaHintsPositionRefresher.class, "Refresh_hints"), context.getCancel(), false); // NOI18N

        return eds;
    }


    private class RefreshTask implements Task<CompilationController> {

        private final Map<String, List<ErrorDescription>> eds;
        private final List<? extends PositionRefresherHelper<?>> refreshers;
        private final Context ctx;

        public RefreshTask(Map<String, List<ErrorDescription>> eds, List<? extends PositionRefresherHelper<?>> refreshers, Context ctx) {
            this.eds = eds;
            this.refreshers = refreshers;
            this.ctx = ctx;
        }

        @Override
        public void run(CompilationController controller) throws Exception {
            if (controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                return ;
            }

            Document doc = controller.getDocument();

            if (doc == null) {
                return;
            }

            for (PositionRefresherHelper<?> h : refreshers) {
                if (ctx.isCanceled()) {
                    return;
                }
                
                List<ErrorDescription> errors = h.getErrorDescriptionsAt(controller, ctx, doc);
                
                if (errors == null) continue;
                
                eds.put(h.getKey(), errors);
            }
        }
        
    }

}
