/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference.FLOW_DOCUMENT_ID;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowId;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import static org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigComponentImpl.createElementNS;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowCallFacesFlowReferenceImpl extends JSFConfigComponentImpl implements FlowCallFacesFlowReference {

    public FlowCallFacesFlowReferenceImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }

    public FlowCallFacesFlowReferenceImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.FLOW_REFERENCE));
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<FlowId> getFlowIds() {
        return getChildren(FlowId.class);
    }

    @Override
    public void addFlowId(FlowId flowId) {
        appendChild(FLOW_ID, flowId);
    }

    @Override
    public void removeFlowId(FlowId flowId) {
        removeChild(FLOW_ID, flowId);
    }

    @Override
    public List<FlowDocumentId> getFlowDocumentIds() {
        return getChildren(FlowDocumentId.class);
    }

    @Override
    public void addFlowDocumentId(FlowDocumentId flowDocumentId) {
        appendChild(FLOW_DOCUMENT_ID, flowDocumentId);
    }

    @Override
    public void removeFlowDocumentId(FlowDocumentId flowDocumentId) {
        removeChild(FLOW_DOCUMENT_ID, flowDocumentId);
    }

}
