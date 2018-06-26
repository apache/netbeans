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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.editor.hints;

import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.rules.*;

/**
 *
 * @author Ajit
 */
public class WSHintsTest extends WSHintsTestBase {
    
    public WSHintsTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(WSHintsTest.class);
        return suite;
    }
    
    // class level hints
    public final void testDefaultPackage() throws IOException {
        Rule<TypeElement> instance = new DefaultPackage();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testHandlerChainAndSoapMessageHandlers() throws IOException {
        Rule<TypeElement> instance = new HandlerChainAndSoapMessageHandlers();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInterfaceEndpointInterface() throws IOException {
        Rule<TypeElement> instance = new InterfaceEndpointInterface();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInterfaceServiceName() throws IOException {
        Rule<TypeElement> instance = new InterfaceServiceName();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidJSRAnnotations() throws IOException {
        Rule<TypeElement> instance = new InvalidJSRAnnotations();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidNameAttribute() throws IOException {
        Rule<TypeElement> instance = new InvalidNameAttribute();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testRPCStyleWrappedParameterStyle() throws IOException {
        Rule<TypeElement> instance = new RPCStyleWrappedParameterStyle();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    // operation level hints
    public final void testOnewayOperationReturnType() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationReturnType();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testOnewayOperationExceptions() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationExceptions();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testOnewayOperationParameterMode() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationParameterMode();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidExcludeAttribute() throws IOException {
        Rule<ExecutableElement> instance = new InvalidExcludeAttribute();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    // parameter level hints
    public final void testWebParamHolder() throws IOException {
        Rule<VariableElement> instance = new WebParamHolder();
        getRulesEngine().getParameterRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }
}
