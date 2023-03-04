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
