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

package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.Collections;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.ClassPathBasedHintWrapper;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

@NbBundle.Messages({
    "CTL_FindPattern=Find Pattern",
    "CTL_ApplyPattern=Inspect and Transform"
})
public class InspectAndRefactorUI implements RefactoringUI {

    private final boolean explicitPattern;
    private volatile @NonNull Union2<String, Iterable<? extends HintDescription>> pattern;
    private volatile boolean verify;

    private final boolean query;
    private final FindDuplicatesRefactoring refactoring;
    private InspectAndRefactorPanel panel;
    private Lookup context;

    
    public InspectAndRefactorUI(@NullAllowed String pattern, boolean verify, boolean query, Lookup context) {
        if (!query && !verify) {
            throw new UnsupportedOperationException();
        }

        this.explicitPattern = pattern != null;
        this.pattern = pattern != null ? Union2.<String, Iterable<? extends HintDescription>>createFirst(pattern) : Union2.<String, Iterable<? extends HintDescription>>createSecond(Collections.<HintDescription>emptyList());
        this.verify = verify;
        this.query = query;
        this.refactoring = new FindDuplicatesRefactoring(query);
        this.refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
        this.context = context;
    }

    public String getName() {
        return query ? Bundle.CTL_FindPattern() : Bundle.CTL_ApplyPattern();
    }

    public String getDescription() {
        return query ? Bundle.CTL_FindPattern() : Bundle.CTL_ApplyPattern();
    }

    public boolean isQuery() {
        return query;
    }

    public CustomRefactoringPanel getPanel(final ChangeListener parent) {
        return new CustomRefactoringPanel() {
            private final ClassPathBasedHintWrapper cpBased;
            {
                cpBased = new ClassPathBasedHintWrapper();
            }
            public void initialize() {
                cpBased.compute();
                //TODO: the ordering a bit dubious here:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        panel.initialize();
                    }
                });
            }
            public Component getComponent() {
                if (panel == null) {
                    panel = new InspectAndRefactorPanel(context, parent, query, cpBased);
                    panel.setPreferredSize(new Dimension(panel.getFontMetrics(panel.getFont()).charWidth('A')*75, panel.getPreferredSize().height));//NOI18N
                }

                return panel;
            }
        };
    }

    public Problem setParameters() {
        refactoring.setPattern(panel.getPattern().second());
        refactoring.setScope(panel.getScope());
        return refactoring.checkParameters();
    }

    public Problem checkParameters() {
        refactoring.setPattern(panel.getPattern().second());
        refactoring.setScope(panel.getScope());
        return refactoring.fastCheckParameters();
    }

    public boolean hasParameters() {
        return true;
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.java.hints.jackpot.impl.refactoring.InspectAndRefactorUI");
    }

    public static void openRefactoringUI(Lookup context) {
        UI.openRefactoringUI(new InspectAndRefactorUI(null, true, false, context));
    }

    public static final class HintWrap {
        public final HintMetadata hm;
        public final Iterable<? extends HintDescription> hints;
        public HintWrap(HintMetadata hm, Iterable<? extends HintDescription> hints) {
            this.hm = hm;
            this.hints = hints;
        }
    }
}