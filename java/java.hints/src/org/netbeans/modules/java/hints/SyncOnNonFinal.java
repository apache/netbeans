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

package org.netbeans.modules.java.hints;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.SyncOnNonFinal", description = "#DESC_org.netbeans.modules.java.hints.SyncOnNonFinal", id="org.netbeans.modules.java.hints.SyncOnNonFinal", category="thread", suppressWarnings="SynchronizeOnNonFinalField")
public class SyncOnNonFinal {

    @TriggerTreeKind(Kind.SYNCHRONIZED)
    public static ErrorDescription run(HintContext ctx) {
        ExpressionTree expression = ((SynchronizedTree) ctx.getPath().getLeaf()).getExpression();
        
        Element e = ctx.getInfo().getTrees().getElement(new TreePath(ctx.getPath(), expression));
        
        if (e == null || e.getKind() != ElementKind.FIELD || e.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }
        
        String displayName = NbBundle.getMessage(SyncOnNonFinal.class, "ERR_SynchronizationOnNonFinalField");
        
        return ErrorDescriptionFactory.forTree(ctx, expression, displayName);
    }

}
