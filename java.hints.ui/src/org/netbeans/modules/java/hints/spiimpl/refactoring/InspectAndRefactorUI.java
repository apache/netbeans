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