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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Context;

/**
 *
 * @author lahvac
 */
public class NBAttr extends Attr {

    public static void preRegister(Context context) {
        context.put(attrKey, new Context.Factory<Attr>() {
            public Attr make(Context c) {
                return new NBAttr(c);
            }
        });
    }

    private final CancelService cancelService;

    public NBAttr(Context context) {
        super(context);
        cancelService = CancelService.instance(context);
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        cancelService.abortIfCanceled();
        super.visitClassDef(tree);
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
        cancelService.abortIfCanceled();
        super.visitMethodDef(tree);
    }

    @Override
    public void visitBlock(JCBlock tree) {
        cancelService.abortIfCanceled();
        super.visitBlock(tree);
    }

}
