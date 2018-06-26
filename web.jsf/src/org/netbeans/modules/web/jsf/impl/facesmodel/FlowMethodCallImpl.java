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
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall.DEFAULT_OUTCOME;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall.METHOD;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall.PARAMETER;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Method;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowMethodCallImpl extends JSFConfigComponentImpl implements FlowMethodCall {

    public FlowMethodCallImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.METHOD_CALL));
    }

    FlowMethodCallImpl(JSFConfigModelImpl myModel, Element element) {
        super(myModel, element);
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<Method> getMethods() {
        return getChildren(Method.class);
    }

    @Override
    public void addMethod(Method method) {
        appendChild(METHOD, method);
    }

    @Override
    public void removeMethod(Method method) {
        removeChild(METHOD, method);
    }

    @Override
    public List<FlowDefaultOutcome> getDefaultOutcomes() {
        return getChildren(FlowDefaultOutcome.class);
    }

    @Override
    public void addDefaultOutcome(FlowDefaultOutcome defaultOutcome) {
        appendChild(DEFAULT_OUTCOME, defaultOutcome);
    }

    @Override
    public void removeDefaultOutcome(FlowDefaultOutcome defaultOutcome) {
        removeChild(DEFAULT_OUTCOME, defaultOutcome);
    }

    @Override
    public List<FlowCallParameter> getParameters() {
        return getChildren(FlowCallParameter.class);
    }

    @Override
    public void addParameter(FlowCallParameter parameter) {
        appendChild(PARAMETER, parameter);
    }

    @Override
    public void removeParameter(FlowCallParameter parameter) {
        removeChild(PARAMETER, parameter);
    }


}
