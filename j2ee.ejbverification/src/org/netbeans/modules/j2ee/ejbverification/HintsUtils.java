/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.ejbverification;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext.SessionData;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class HintsUtils {

    private static final Logger LOG = Logger.getLogger(HintsUtils.class.getName());
    private static final String CACHED_CONTEXT = "cached-ejbProblemContext-";

    public static ErrorDescription createProblem(Element subject, CompilationInfo cinfo,
            String description) {
        return createProblem(subject, cinfo, description, Severity.ERROR, Collections.<Fix>emptyList());
    }

    public static ErrorDescription createProblem(Element subject, CompilationInfo cinfo,
            String description, Severity severity) {
        return createProblem(subject, cinfo, description, severity, Collections.<Fix>emptyList());
    }

    public static ErrorDescription createProblem(Element subject, CompilationInfo cinfo, String description,
            Severity severity, Fix fix) {
        return createProblem(subject, cinfo, description, severity, Collections.singletonList(fix));
    }

    public static ErrorDescription createProblem(Element subject, CompilationInfo cinfo, String description, Fix fix) {
        return createProblem(subject, cinfo, description, Severity.ERROR, Collections.singletonList(fix));
    }

    public static ErrorDescription createProblem(Element subject, CompilationInfo cinfo,
            String description, Severity severity, List<Fix> fixes) {
        ErrorDescription err = null;
        List<Fix> fixList = fixes == null ? Collections.<Fix>emptyList() : fixes;

        // by default place error annotation on the element being checked
        Tree elementTree = cinfo.getTrees().getTree(subject);

        if (elementTree != null) {
            TextSpan underlineSpan = getUnderlineSpan(cinfo, elementTree);

            err = ErrorDescriptionFactory.createErrorDescription(
                    severity, description, fixList, cinfo.getFileObject(),
                    underlineSpan.getStartOffset(), underlineSpan.getEndOffset());

        } else {
            // report problem
        }

        return err;
    }

    /**
     * Says whether the given version is of the EJB version 3.0 and higher. BTW, annotation EJB model always returns EJB
     * 3.0.
     *
     * @param ejbVersion string representation of the EJB version
     * @return {@code true} if the version is equal or higher than EJB3.0, {@code false} otherwise
     */
    public static boolean isEjb30Plus(String ejbVersion) {
        return org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.VERSION_3_0.equals(ejbVersion)
                || org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.VERSION_3_1.equals(ejbVersion)
                || org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.VERSION_3_2.equals(ejbVersion);
    }

    /**
     * This method returns the part of the syntax tree to be highlighted. It will be usually the class/method/variable
     * identifier.
     */
    public static TextSpan getUnderlineSpan(CompilationInfo info, Tree tree) {
        SourcePositions srcPos = info.getTrees().getSourcePositions();

        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);

        Tree startSearchingForNameIndentifierBehindThisTree = null;

        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
            startSearchingForNameIndentifierBehindThisTree = ((ClassTree) tree).getModifiers();

        } else if (tree.getKind() == Tree.Kind.METHOD) {
            startSearchingForNameIndentifierBehindThisTree = ((MethodTree) tree).getReturnType();
        } else if (tree.getKind() == Tree.Kind.VARIABLE) {
            startSearchingForNameIndentifierBehindThisTree = ((VariableTree) tree).getType();
        }

        if (startSearchingForNameIndentifierBehindThisTree != null) {
            int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                    startSearchingForNameIndentifierBehindThisTree);

            TokenSequence tokenSequence = info.getTreeUtilities().tokensFor(tree);

            if (tokenSequence != null) {
                boolean eob = false;
                tokenSequence.move(searchStart);

                do {
                    eob = !tokenSequence.moveNext();
                } while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);

                if (!eob) {
                    Token identifier = tokenSequence.token();
                    startOffset = identifier.offset(info.getTokenHierarchy());
                    endOffset = startOffset + identifier.length();
                }
            }
        }

        return new TextSpan(startOffset, endOffset);
    }

    /**
     * Represents a span of text
     */
    public static class TextSpan {

        private int startOffset;
        private int endOffset;

        public TextSpan(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }

    public static boolean isContainingKnownClasses(ExecutableElement method) {
        if (method.getReturnType().getKind() == TypeKind.ERROR) {
            return false;
        }

        for (TypeMirror type : method.getThrownTypes()) {
            if (type.getKind() == TypeKind.ERROR) {
                return false;
            }
        }

        for (VariableElement variableElement : method.getParameters()) {
            if (variableElement.asType().getKind() == TypeKind.ERROR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets problem context used by standard EJB hints. This method can be used for @TriggerTreeKind based hints.
     * Uses cached value if found, otherwise creates a new one which stores into the CompilationInfo.
     *
     * @param context Hints API context
     * @return EJB hint's context
     */
    public static EJBProblemContext getOrCacheContext(HintContext context) {
        Element element = context.getInfo().getTrees().getElement(context.getPath());
        String elementType = element.asType().toString();
        return getOrCacheContext(context, elementType);
    }

    /**
     * Gets problem context used by standard EJB hints.
     * Uses cached value if found, otherwise creates a new one which stores into the CompilationInfo.
     *
     * @param context Hints API context
     * @param elementType FQN of the element where should be hint applied
     * @return EJB hint's context
     */
    public static EJBProblemContext getOrCacheContext(HintContext context, String elementType) {
        Object cached = context.getInfo().getCachedValue(CACHED_CONTEXT + elementType);
        if (cached == null) {
            LOG.log(Level.FINEST, "HintContext doesn''t contain cached EJBProblemContext which is going to be created for type: {0}", elementType);
            EJBProblemContext newContext = createEJBProblemContext(context);
            context.getInfo().putCachedValue(CACHED_CONTEXT + elementType, newContext, CompilationInfo.CacheClearPolicy.ON_SIGNATURE_CHANGE);
            return newContext;
        } else {
            LOG.log(Level.FINEST, "EJBProblemContext cached value used.");
            return (EJBProblemContext) cached;
        }
    }

    private static EJBProblemContext createEJBProblemContext(final HintContext context) {
        final CompilationInfo info = context.getInfo();
        final FileObject file = info.getFileObject();

        final Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }

        J2eeProjectCapabilities projCap = J2eeProjectCapabilities.forProject(project);
        if (projCap == null || (!projCap.isEjb30Supported() && !projCap.isEjb31LiteSupported())) {
            return null;
        }

        final EjbJar ejbModule = EjbJar.getEjbJar(file);
        if (ejbModule == null) {
            return null;
        }

        try {
            return ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, EJBProblemContext>() {
                @Override
                public EJBProblemContext run(EjbJarMetadata metadata) {
                    long startTime = Calendar.getInstance().getTimeInMillis();
                    String ejbVersion = metadata.getRoot().getVersion().toString();
                    if (!HintsUtils.isEjb30Plus(ejbVersion)) {
                        return null; // Only EJB 3.0+ are supported
                    }

                    Element element = info.getTrees().getElement(context.getPath());
                    if (element instanceof TypeElement) {
                        TypeElement javaClass = (TypeElement) element;
                        Ejb ejb = metadata.findByEjbClass(javaClass.getQualifiedName().toString());

                        // precompute EJB information
                        String[] businessLocal = new String[0];
                        String[] businessRemote = new String[0];
                        String sessionType = "";
                        try {
                            if (ejb instanceof Session) {
                                Session session = ((Session) ejb);
                                businessLocal = session.getBusinessLocal();
                                businessRemote = session.getBusinessRemote();
                                sessionType = session.getSessionType();
                            }
                        } catch (VersionNotSupportedException ex) {
                            LOG.log(Level.INFO, ex.getMessage(), ex);
                        }

                        if (LOG.isLoggable(Level.FINE)) {
                            long timeElapsed = Calendar.getInstance().getTimeInMillis() - startTime;
                            LOG.log(Level.FINE, "processed class {0} in {1} ms", new Object[]{javaClass.getSimpleName(), timeElapsed});
                        }
                        return new EJBProblemContext(project, ejbModule, file, javaClass, ejb, new SessionData(businessLocal, businessRemote, sessionType));
                    } else {
                        return null;
                    }
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
