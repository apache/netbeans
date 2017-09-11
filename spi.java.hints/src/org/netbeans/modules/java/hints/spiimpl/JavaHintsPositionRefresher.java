/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl;

import java.io.IOException;
import java.util.ArrayList;
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
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper;
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
        final List<PositionRefresherHelper> refreshers = new ArrayList<PositionRefresherHelper>(MimeLookup.getLookup("text/x-java").lookupAll(PositionRefresherHelper.class));

        for (Iterator<PositionRefresherHelper> it = refreshers.iterator(); it.hasNext();) {
            PositionRefresherHelper h = it.next();

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

        final Map<String, List<ErrorDescription>> eds = new HashMap<String, List<ErrorDescription>>();

        Runnable r = new Runnable() {

            public void run() {
                try {
                    js.runUserActionTask(new RefreshTask(eds, refreshers, context, doc), true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        
        ProgressUtils.runOffEventDispatchThread(r, NbBundle.getMessage(JavaHintsPositionRefresher.class, "Refresh_hints"), context.getCancel(), false); // NOI18N

        return eds;
    }


    private class RefreshTask implements Task<CompilationController> {

        private final Map<String, List<ErrorDescription>> eds;
        private final List<PositionRefresherHelper> refreshers;
        private final Context ctx;
        private final Document doc;

        public RefreshTask(Map<String, List<ErrorDescription>> eds, List<PositionRefresherHelper> refreshers, Context ctx, Document doc) {
            this.eds = eds;
            this.refreshers = refreshers;
            this.ctx = ctx;
            this.doc = doc;
        }

        public void run(CompilationController controller) throws Exception {
            if (controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                return ;
            }

            Document doc = controller.getDocument();

            if (doc == null) {
                return;
            }

            for (PositionRefresherHelper h : refreshers) {
                if (ctx.isCanceled()) {
                    return;
                }
                
                List errors = h.getErrorDescriptionsAt(controller, ctx, doc);
                
                if (errors == null) continue;
                
                eds.put(h.getKey(), errors);
            }
        }
        
    }

}
