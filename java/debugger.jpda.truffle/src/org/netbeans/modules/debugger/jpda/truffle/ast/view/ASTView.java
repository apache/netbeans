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

package org.netbeans.modules.debugger.jpda.truffle.ast.view;

import org.netbeans.spi.debugger.ui.ViewFactory;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * The AST view.
 */
public final class ASTView {

    public static final String AST_VIEW_NAME = "TruffleASTDebugView";

    private ASTView() {}

    /**
     * @deprecated  Do not call directly, invoked from module layer.
     */
    @Deprecated
    @NbBundle.Messages({"CTL_AST_View=Truffle AST Node Tree", "CTL_AST_View_tooltip=Truffle AST Node Tree"})
    public static TopComponent getASTView() {
        return ViewFactory.getDefault().createViewTC(
                "",
                AST_VIEW_NAME,
                "NetBeansDebuggerTruffleASTNode",
                null,
                Bundle.CTL_AST_View(),
                Bundle.CTL_AST_View_tooltip());
    }
}
