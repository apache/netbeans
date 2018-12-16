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
package org.netbeans.modules.java.hints.generator.ui;

import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.modules.java.hints.generator.RefactoringDetector;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Hint(category="experimental",
      id="AdaptiveRefactoringDetectorEnabler",
      displayName="#DN_AdaptiveRefactoringDetectorEnabler",
      description="#DESC_AdaptiveRefactoringDetectorEnabler",
      enabled=false)
@Messages({
    "DN_AdaptiveRefactoringDetectorEnabler=Adaptive Refactoring Detector",
    "DESC_AdaptiveRefactoringDetectorEnabler=Experimental automatic Adaptive Refactoring detection",
})
public class AdaptiveRefactoringDetectorEnabler {
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static ErrorDescription ignore(HintContext ctx) throws IOException {
        //when enabled, ensure the EditorPeer is installed:
        Document doc = ctx.getInfo().getDocument();

        if (doc != null) {
            RefactoringDetector.EditorPeer ep = (RefactoringDetector.EditorPeer) doc.getProperty(RefactoringDetector.EditorPeer.class);

            if (ep == null) {
                doc.putProperty(RefactoringDetector.EditorPeer.class, RefactoringDetector.editorPeer(ctx.getInfo()));
            }
        }
        return null;
    }
}
