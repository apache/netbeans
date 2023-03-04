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
package org.netbeans.modules.java.hints.introduce;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Becicka
 */
public final class IntroduceParameterFix implements Fix {
    private final TreePathHandle tph;

    public IntroduceParameterFix(TreePathHandle tph) {
        this.tph = tph;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceParameterFix.class, "FIX_IntroduceParameter"); // NOI18N
    }

    @Override
    public ChangeInfo implement() throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
                if (activatedNodes.length > 0) {
                    EditorCookie ec = activatedNodes[0].getLookup().lookup(EditorCookie.class);
                    Action a = JavaRefactoringActionsFactory.introduceParameterAction().createContextAwareInstance(Lookups.fixed(activatedNodes[0],ec));
                    a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                }
                
            }
        });
        return null;
    }

    @Override
    public String toString() {
        return "[Introduce Parameter Fix]";
    }

}