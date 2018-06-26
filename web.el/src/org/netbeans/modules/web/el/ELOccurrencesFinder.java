/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ELException;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Occurrences finder for Expression Language.
 *
 * @author Erno Mononen
 */
final class ELOccurrencesFinder extends OccurrencesFinder {

    private int caretPosition;
    private boolean cancelled;
    private final Map<OffsetRange, ColoringAttributes> occurrences = new HashMap<>();

    public ELOccurrencesFinder() {
    }

    @Override
    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }

    @Override
    public Map getOccurrences() {
        return occurrences;
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        occurrences.clear();
        if (checkAndResetCancel()) {
            return;
        }
        computeOccurrences((ELParserResult) result);
    }

    @Override
    public int getPriority() {
        return 200; // not sure what to return here, 200 this is just a random number.
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    private void computeOccurrences(final ELParserResult parserResult) {
        ELElement current = parserResult.getElementAt(caretPosition);
        if (current == null) {
            return;
        }
        final Node targetNode = current.findNodeAt(caretPosition);
        if (targetNode == null || targetNode.getImage() == null) {
           return;
        }
        final Pair<ELElement,Node> target = Pair.of(current, targetNode);
        // find other similar nodes
        final List<Pair<ELElement, Node>> matching = new ArrayList<>();
        for (final ELElement eLElement : parserResult.getElements()) {
            if (checkAndResetCancel()) {
                return;
            }
            if (!eLElement.isValid()) {
                continue;
            }
            eLElement.getNode().accept(new NodeVisitor() {

                @Override
                public void visit(Node node) throws ELException {
                    if (node.getClass().equals(targetNode.getClass())
                            && targetNode.getImage().equals(node.getImage())) {
                        matching.add(Pair.of(eLElement, node));
                    }
                }
            });
        }
        final FileObject file = parserResult.getFileObject();
        JavaSource jsource = JavaSource.create(ELTypeUtilities.getElimplExtendedCPI(file));
        try {
            jsource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    occurrences.putAll(findMatchingTypes(CompilationContext.create(file, info), parserResult, target, matching));
                    
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        if (this.occurrences.isEmpty()) {
            // perhaps the caret is on a resource bundle key node
            occurrences.putAll(findMatchingResourceBundleKeys(target, parserResult));
        }
    }

    private Map<OffsetRange, ColoringAttributes> findMatchingResourceBundleKeys(Pair<ELElement, Node> target, ELParserResult parserResult) {
        ResourceBundles resourceBundles = ResourceBundles.get(parserResult.getFileObject());
        if (!resourceBundles.canHaveBundles()) {
            return Collections.emptyMap();
        }
        List<Pair<AstIdentifier, Node>> keys = new ArrayList<>();
        // the logic here is a bit strange, maybe should add new methods to ResourceBundles
        // for a more straightforward computation.
        // first, check whether the current EL elements has keys
        keys.addAll(resourceBundles.collectKeys(target.first().getNode()));
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        // second, if yes, check whether it has a key matching to the node under the caret
        boolean found = false;
        for (Pair<AstIdentifier, Node> pair : keys) {
            if (pair.second().equals(target.second())) {
                found = true;
                break;
            }
        }
        if (!found) {
            return Collections.emptyMap();
        }
        // third: collect the other matching keys and return them
        Map<OffsetRange, ColoringAttributes> result = new HashMap<>();
        for (ELElement each : parserResult.getElements()) {
            if (!each.isValid()) {
                continue;
            }
            for (Pair<AstIdentifier, Node> candidate : resourceBundles.collectKeys(each.getNode())) {
                if (candidate.second().equals(target.second())) {
                    OffsetRange range = each.getOriginalOffset(candidate.second());
                    result.put(range, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        }
        return result;
    }

    private Map<OffsetRange, ColoringAttributes> findMatchingTypes(CompilationContext info, ELParserResult parserResult, Pair<ELElement,Node> target, List<Pair<ELElement,Node>> candidates) {
        Element targetType = ELTypeUtilities.resolveElement(info, target.first(), target.second());
        Map<OffsetRange, ColoringAttributes>  result = new HashMap<>();

        for (Pair<ELElement,Node> candidate : candidates) {
            if (checkAndResetCancel()) {
                return result;
            }
            Element type = ELTypeUtilities.resolveElement(info, candidate.first(), candidate.second());
            if (type != null && type.equals(targetType)) {
                OffsetRange range = candidate.first().getOriginalOffset(candidate.second());
                result.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }
        return result;
    }

    private boolean checkAndResetCancel() {
        if (cancelled) {
            cancelled = false;
            return true;
        }
        return false;
    }

}
