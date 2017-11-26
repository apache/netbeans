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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Flow;
import com.sun.tools.javac.comp.TypeEnter;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import java.nio.CharBuffer;
import java.util.Map;

/**
 *
 * @author Tomas Zezula
 */
public class PartialReparser {
    
    protected static final Context.Key<PartialReparser> partialReparserKey = new Context.Key<PartialReparser>();
    
    public static PartialReparser instance(Context ctx) {
        PartialReparser res = ctx.get(partialReparserKey);
        
        if (res == null) {
            ctx.put(partialReparserKey, res = new PartialReparser(ctx));
        }
        
        return res;
    }

    private final Context context;

    public PartialReparser(Context context) {
        this.context = context;
    }

    public JCBlock reparseMethodBody(CompilationUnitTree topLevel, MethodTree methodToReparse, String newBodyText, int annonIndex,
            final Map<? super JCTree,? super LazyDocCommentTable.Entry> docComments) {
        NBParserFactory parserFactory = (NBParserFactory) NBParserFactory.instance(context);
        CharBuffer buf = CharBuffer.wrap((newBodyText+"\u0000").toCharArray(), 0, newBodyText.length());
        JavacParser parser = parserFactory.newParser(buf, ((JCBlock)methodToReparse.getBody()).pos, ((JCCompilationUnit)topLevel).endPositions);
        final JCStatement statement = parser.parseStatement();
        NBParserFactory.assignAnonymousClassIndices(Names.instance(context), statement, Names.instance(context).empty, annonIndex);
        if (statement.getKind() == Tree.Kind.BLOCK) {
            if (docComments != null) {
                docComments.putAll(((LazyDocCommentTable) parser.getDocComments()).table);
            }
            return (JCBlock) statement;
        }
        return null;
    }

    public BlockTree reattrMethodBody(MethodTree methodToReparse, BlockTree block) {
        Attr attr = Attr.instance(context);
        assert ((JCMethodDecl)methodToReparse).localEnv != null;
        JCMethodDecl tree = (JCMethodDecl) methodToReparse;
        final Names names = Names.instance(context);
        final Symtab syms = Symtab.instance(context);
        final TypeEnter typeEnter = TypeEnter.instance(context);
        final Log log = Log.instance(context);
        final TreeMaker make = TreeMaker.instance(context);
        final Env<AttrContext> env = attr.dupLocalEnv(((JCMethodDecl) methodToReparse).localEnv);
        final ClassSymbol owner = env.enclClass.sym;
        if (tree.name == names.init && !owner.type.isErroneous() && owner.type != syms.objectType) {
            JCBlock body = tree.body;
            if (body.stats.isEmpty() || !TreeInfo.isSelfCall(body.stats.head)) {
                body.stats = body.stats.
                prepend(typeEnter.SuperCall(make.at(body.pos),
                    List.<Type>nil(),
                    List.<JCVariableDecl>nil(),
                    false));
            } else if ((env.enclClass.sym.flags() & Flags.ENUM) != 0 &&
                (tree.mods.flags & Flags.GENERATEDCONSTR) == 0 &&
                TreeInfo.isSuperCall(body.stats.head)) {
                // enum constructors are not allowed to call super
                // directly, so make sure there aren't any super calls
                // in enum constructors, except in the compiler
                // generated one.
                log.error(tree.body.stats.head.pos(),
                          "call.to.super.not.allowed.in.enum.ctor",
                          env.enclClass.sym);
                    }
                }
        attr.attribStat((JCBlock)block, env);
        return block;
    }

    public BlockTree reflowMethodBody(CompilationUnitTree topLevel, ClassTree ownerClass, MethodTree methodToReparse) {
        Flow flow = Flow.instance(context);
        TreeMaker make = TreeMaker.instance(context);
        flow.reanalyzeMethod(make.forToplevel((JCCompilationUnit)topLevel),
                (JCClassDecl)ownerClass);
        return methodToReparse.getBody();
    }
}
