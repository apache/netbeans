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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
final class AddCatchFix extends JavaFix {

    private final List<TypeMirrorHandle> thandles;
    
    public AddCatchFix(CompilationInfo info, TreePath tryStatement, List<TypeMirrorHandle> thandles) {
        super(info, tryStatement);
        this.thandles = thandles;
    }
    
    public String getText() {
        return NbBundle.getMessage(MagicSurroundWithTryCatchFix.class, "LBL_AddCatchClauses", thandles.size());
    }
    
    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
        TryTree tt = (TryTree) ctx.getPath().getLeaf();

        List<CatchTree> catches = new ArrayList<CatchTree>();
        
        catches.addAll(tt.getCatches());
        catches.addAll(MagicSurroundWithTryCatchFix.createCatches(ctx.getWorkingCopy(), make, thandles, ctx.getPath()));

        ctx.getWorkingCopy().rewrite(tt, make.Try(tt.getResources(), tt.getBlock(), catches, tt.getFinallyBlock()));
    }
    
}
