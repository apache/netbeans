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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.editor.hints.rules;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class InconsistentPortType extends AbstractWebServiceRule {

    public InconsistentPortType() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_WEBSERVICE);
        if (subject.getKind() == ElementKind.CLASS && Utilities.getAnnotationAttrValue(annEntity, ANNOTATION_ATTRIBUTE_SEI) == null) {
            Service service = ctx.getLookup().lookup(Service.class);
            WSDLModel model = ctx.getLookup().lookup(WSDLModel.class);
            if (service != null && model != null && model.getState() == State.VALID) {
                PortType portType = model.findComponentByName(subject.getSimpleName().toString(), PortType.class);
                if (portType == null) {
                    AnnotationValue nameVal = Utilities.getAnnotationAttrValue(annEntity, ANNOTATION_ATTRIBUTE_NAME);
                    if(nameVal!=null)
                        portType = model.findComponentByName(nameVal.toString(), PortType.class);
                }
                if (portType == null) {
                    String label = NbBundle.getMessage(InconsistentPortType.class, "MSG_InconsistentPortType");
                    AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                    getTrees().getTree(subject, annEntity);
                    Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree, ANNOTATION_ATTRIBUTE_WSDLLOCATION);
                    ctx.setElementToAnnotate(problemTree);
                    ErrorDescription problem = createProblem(subject, ctx, label, (Fix) null);
                    ctx.setElementToAnnotate(null);
                    return new ErrorDescription[]{problem};
                }
            }
        }
        return null;
    }
}
