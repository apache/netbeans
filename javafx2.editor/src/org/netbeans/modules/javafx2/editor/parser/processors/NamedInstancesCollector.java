/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.parser.processors;

import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstanceCopy;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class NamedInstancesCollector extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    private Map<String, FxInstance> instances = null;
    
    private BuildEnvironment env;

    public NamedInstancesCollector() {
    }

    public NamedInstancesCollector(BuildEnvironment env) {
        this.env = env;
    }

    @NbBundle.Messages({
        "ERR_duplicateIdentifier=Instance has duplicate fx:id",
        "# {0} - tag name",
        "ERR_defineMustProvideId=Missing fx:id attribute on {0} inside fx:define block"
    })
    @Override
    protected void visitBaseInstance(FxInstance i) {
        if (i.getId() == null) {
            // check if the instance is not directly inside 'define' node:
            if (parentNode != null && parentNode.getKind() == FxNode.Kind.Element &&
                "define".equals(parentNode.getSourceName())) {
                TextPositions pos = env.getTreeUtilities().positions(i);
                env.addError(new ErrorMark(
                    pos.getStart(), pos.getContentStart() - pos.getStart(),
                    "define-must-provide-id",
                    ERR_defineMustProvideId(i.getSourceName()),
                    i
                ));
            }
            super.visitBaseInstance(i);
            return;
        }
        if (instances == null) {
            instances = Collections.singletonMap(i.getId(), i);
        } else {
            Map<String, FxInstance> newInstances;
            
            if (instances.size() == 1) {
                newInstances = new HashMap<String, FxInstance>();
                newInstances.putAll(instances);
                instances = newInstances;
            } else {
                newInstances = instances;
            }
            if (newInstances.containsKey(i.getId())) {
                TextPositions pos = env.getTreeUtilities().positions(i);
                // error, duplicate ID found.
                env.addError(new ErrorMark(
                    pos.getStart(), pos.getContentStart() - pos.getStart(),
                    "duplicate-id",
                    ERR_duplicateIdentifier(),
                    i
                ));
            } else {
                newInstances.put(i.getId(), i);
            }
        }
        super.visitBaseInstance(i);
    }
    
    private FxNode parentNode;
    
    public void visitNode(FxNode node) {
        FxNode previous = this.parentNode;
        this.parentNode = node;
        super.visitNode(node);
        this.parentNode = previous;
    }
    
    @Override
    public void visitSource(FxModel source) {
        super.visitSource(source);
        if (instances != null) {
            env.getAccessor().setNamedInstances(source, instances);
        }
    }

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new NamedInstancesCollector(env);
    }
    
}
