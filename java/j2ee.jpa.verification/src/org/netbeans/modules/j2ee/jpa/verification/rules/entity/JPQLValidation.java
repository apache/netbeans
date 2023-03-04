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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblemResourceBundle;
import org.eclipse.persistence.jpa.jpql.parser.DefaultEclipseLinkJPQLGrammar;
import org.eclipse.persistence.jpa.jpql.tools.DefaultJPQLQueryHelper;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Verify content of
 *
 * @NamedQuery query 
 * 
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.JPQLValidation",
        displayName = "#JPQLValidation.display.name",
        description = "#JPQLValidation.desc",
        category = "javaee/jpa",
        enabled = true,
        suppressWarnings = "JPQLValidation")
@NbBundle.Messages({
    "JPQLValidation.display.name=JPQL validation",
    "JPQLValidation.desc=Parse and find errors in a jpql query"
})
public class JPQLValidation {

    /**
     * Creates a new instance of NonFinalClass
     */
    @TriggerPatterns(value = {
        @TriggerPattern(value = JPAAnnotations.NAMED_QUERY),
        @TriggerPattern(
                value = "$em.createQuery",
                constraints =
                @ConstraintVariableType(
                variable = "$em",
                type = "javax.persistence.EntityManager"))//NOI18N
    })
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || ((hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION) && hc.getVariables().get("$em") == null)) {//NOI18N
            return null;
        }
        boolean emCreate = hc.getVariables().get("$em") != null;
        Iterator<Tree> it1 = hc.getPath().iterator();
        int count = 0;
        //find nq
        String query = null;
        TreePath queryPath = null;
        if (emCreate) {
            queryPath = hc.getPath();
            it1.next();
            Tree tr = it1.next();
            if (tr instanceof MethodInvocationTree) {
                for(ExpressionTree et:((MethodInvocationTree )tr).getArguments()) {

                        if (et instanceof LiteralTree) {
                            query = ((LiteralTree) et).getValue() != null ? ((LiteralTree) et).getValue().toString() : null;
                            break;
                        } else if (et instanceof BinaryTree) {
                            query = queryFromBinaryTree((BinaryTree) et);
                            break;
                        } else {
                            query = null;//can't parse, smth unknown
                        }

                        if (count++ > 2) {
                            break;
                        }
                }
            }
        } else {
            while (it1.hasNext()) {
                Tree tr = it1.next();
                if (tr instanceof AnnotationTree) {
                    AnnotationTree at = (AnnotationTree) tr;
                    for (ExpressionTree exp : at.getArguments()) {
                        if (exp instanceof AssignmentTree) {
                            AssignmentTree ast = (AssignmentTree) exp;
                            if ("query".equals(ast.getVariable().toString())) { //NOI18N
                                ExpressionTree et = ast.getExpression();
                                hc.getInfo().getCompilationUnit().getPackageAnnotations();
                                queryPath = TreePath.getPath(hc.getInfo().getCompilationUnit(), ast.getVariable());//let's underline queryattribute name only
                                if (et instanceof LiteralTree) {
                                    query = ((LiteralTree) et).getValue().toString();
                                } else if (et instanceof BinaryTree) {
                                    query = queryFromBinaryTree((BinaryTree) et);
                                } else {
                                    query = null;//can't parse, smth unknown
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
                if (count++ > 2) {
                    break;
                }
            }
        }
        if (query == null || hc.isCanceled()) {
            return null;
        }
        queryPath = queryPath != null ? queryPath : hc.getPath().getParentPath();
        JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }
        final DefaultJPQLQueryHelper helper = new DefaultJPQLQueryHelper(DefaultEclipseLinkJPQLGrammar.instance());
        Project project = FileOwnerQuery.getOwner(hc.getInfo().getFileObject());
        final List<JPQLQueryProblem> problems = new ArrayList<>();
        ManagedTypeProvider mtp = ModelUtils.getOrCreateCachedMTP(hc, project, ctx.getMetaData(), ctx.getCompilationInfo().getElements());
        helper.setQuery(new Query(null, query, mtp));

        try {
            MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
            model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
                    List<JPQLQueryProblem> tmp = null;
                    try {
                        tmp = helper.validate();
                    } catch (UnsupportedOperationException ex) {
                        JPAProblemFinder.LOG.log(Level.INFO, "Unsupported jpql validation case: " + ex.getMessage(), ex);
                    } catch (NullPointerException ex) {
                        JPAProblemFinder.LOG.log(Level.INFO, "NPE in jpql validation: " + ex.getMessage(), ex);
                    }
                    if (tmp != null && tmp.size() > 0) {
                        problems.addAll(tmp);
                    }
                    return null;
                }
            });
        } catch (IOException ex) {
        }

        helper.dispose();
        ErrorDescription ret = null;
        if (!hc.isCanceled() && problems.size() > 0) {
            ListResourceBundle msgBundle;
            try {
                msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName());//NOI18N
            } catch (MissingResourceException ex) {//default en
                msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName(), Locale.ENGLISH);//NOI18N
            }
            StringBuilder sb = new StringBuilder("");
            for (JPQLQueryProblem problem : problems) {
                sb.append("[").append(problem.getStartPosition()).append(";").append(problem.getEndPosition()).append("]: ");
                sb.append(java.text.MessageFormat.format(msgBundle.getString(problem.getMessageKey()), (Object[]) problem.getMessageArguments())).append("\n");
            }
            ret = ErrorDescriptionFactory.forTree(
                    hc,
                    queryPath,
                    sb.substring(0, sb.length() - 1),
                    (Fix) null);
        }
        return ret;
    }

    private static String queryFromBinaryTree(BinaryTree bt) {
        String query = "";
        while (bt.getLeftOperand() instanceof BinaryTree) {
            if (bt.getRightOperand() instanceof LiteralTree) {
                query = ((LiteralTree) bt.getRightOperand()).getValue().toString() + query;
            } else {
                query = null;
                return query;
            }
            bt = (BinaryTree) bt.getLeftOperand();
        }
        if (bt.getRightOperand() instanceof LiteralTree) {
            query = ((LiteralTree) bt.getRightOperand()).getValue().toString() + query;
        } else {
            query = null;
        }
        if (bt.getLeftOperand() instanceof LiteralTree) {
            query = ((LiteralTree) bt.getLeftOperand()).getValue().toString() + query;
        } else {
            query = null;

        }
        return query;
    }

    protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext) ctx;

        return (jpaCtx.isEntity() || jpaCtx.isMappedSuperClass());
    }
}
