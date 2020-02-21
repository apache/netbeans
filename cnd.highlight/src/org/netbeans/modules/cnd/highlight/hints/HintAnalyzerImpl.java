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

package org.netbeans.modules.cnd.highlight.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.analysis.api.AbstractAnalyzer;
import org.netbeans.modules.cnd.analysis.api.AbstractHintsPanel;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class HintAnalyzerImpl extends AbstractAnalyzer {
    private static final String PREFIX = "cnd-"; //NOI18N

    private HintAnalyzerImpl(Context ctx) {
        super(ctx);
    }

    @Override
    protected CsmErrorProvider getErrorProvider(Preferences preferences) {
        if (preferences != null) {
            return new CsmHintProvider(preferences);
        } else {
            return CsmHintProvider.getInstance();
        }
    }

    @Override
    protected Collection<? extends ErrorDescription> doRunImpl(final FileObject sr, final Context ctx, final CsmErrorProvider provider, final AtomicBoolean cancel) {
        final CsmFile csmFile = CsmUtilities.getCsmFile(sr, false, false);
        if (csmFile == null) {
            return Collections.<ErrorDescription>emptyList();
        }
        CsmErrorProvider.Request request = new AbstractAnalyzer.RequestImpl(csmFile, ctx, cancel);
        final ArrayList<ErrorDescription> res = new ArrayList<>();
        CsmErrorProvider.Response responce = new ResponseImpl(sr, res, cancel);
        provider.getErrors(request, responce);
        return res;
    }

    @Override
    protected boolean isCompileUnitBased() {
        return false;
    }
    
    private static class ResponseImpl extends AbstractAnalyzer.AbstractResponse {

        public ResponseImpl(FileObject sr, ArrayList<ErrorDescription> res, AtomicBoolean cancel) {
            super(sr, res, cancel);
        }
        
        @Override
        protected ErrorDescription addErrorImpl(CsmErrorInfo errorInfo, FileObject fo) {
            String messages[] = errorInfo.getMessage().split("\n");
            if (messages.length >=2 ) {
                String abbr = messages[0];
                LazyFixList list = new AbstractAnalyzer.LazyFixListImpl();
                StringBuilder buf = new StringBuilder("<pre>"); //NOI18N
                boolean first = true;
                for(int i = 1; i < messages.length; i++) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append("<br>"); //NOI18N
                    }
                    buf.append(messages[i]);
                }
                buf.append("</pre>"); //NOI18N
                String message = messages[1];
                return ErrorDescriptionFactory.createErrorDescription(PREFIX+abbr, Severity.ERROR,
                        message, buf.toString(), list, fo, errorInfo.getStartOffset(), errorInfo.getStartOffset());
            }
            return null;
        }
    }
    
    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class AnalyzerFactoryImpl extends AnalyzerFactory {
        
        public AnalyzerFactoryImpl() {
            super(CsmHintProvider.NAME,
                   NbBundle.getMessage(CsmHintProvider.class, "General_DESCRIPTION"), //NOI18N
                   ImageUtilities.loadImage("org/netbeans/modules/cnd/highlight/resources/bugs.png")); //NOI18N
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            List<WarningDescription> result = new ArrayList<>();
            final CsmHintProvider provider = (CsmHintProvider)CsmHintProvider.getInstance();
            for(CodeAudit audit : provider.getAudits()) {
                result.add(WarningDescription.create(PREFIX+audit.getID(), audit.getName(), CsmHintProvider.NAME, provider.getDisplayName()));
            }
            return result;
        }

        @Override
        public CustomizerProvider<Void, AbstractHintsPanel> getCustomizerProvider() {
            return new CustomizerProvider<Void, AbstractHintsPanel>() {

                @Override
                public Void initialize() {
                    return null;
                }

                @Override
                public AbstractHintsPanel createComponent(CustomizerContext<Void, AbstractHintsPanel> context) {
                    AbstractHintsPanel result = null;
                    if (context != null) {
                        result = context.getPreviousComponent();
                    }

                    if (result == null) {
                        result = AbstractAnalyzer.createComponent(getErrorProvider(context.getSettings()));
                    } else {
                        if (context != null) {
                            result.setSettings(getErrorProvider(context.getSettings()).getPreferences().getPreferences());
                        }
                    }

                    return result;
                }
            };
        }

        private CodeAuditProvider getErrorProvider(Preferences preferences) {
            if (preferences != null) {
                return new CsmHintProvider(preferences);
            } else {
                return (CodeAuditProvider)CsmHintProvider.getInstance();
            }
        }
        
        @Override
        public Analyzer createAnalyzer(Context context) {
            return new HintAnalyzerImpl(context);
        }
    }
}
