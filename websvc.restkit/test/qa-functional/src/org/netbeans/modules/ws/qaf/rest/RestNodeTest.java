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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest;

import java.awt.Component;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;

/**
 * Tests REST node in project logical view
 *
 * Duration of this test suite: approx. 2min
 *
 * @author lukas
 */
public class RestNodeTest extends RestTestBase {

    protected static final String[] services = {"ItemResource", "ItemsResource [/items]", "SampleResource [sample]"}; //NOI18N
    private static final String addMethod =
            "    @javax.ws.rs.POST\n" + //NOI18N
            "    @javax.ws.rs.Consumes(\"application/xml\")\n" + //NOI18N
            "    public void postXml() {\n" + //NOI18N
            "    }\n"; //NOI18N

    public RestNodeTest(String name) {
        super(name, Server.GLASSFISH);
    }

    public RestNodeTest(String name, Server server) {
        super(name, server);
    }

    @Override
    protected String getProjectName() {
        return "NodesSample"; //NOI18N
    }

    /**
     * Test if all REST web service related nodes are visible in project logical
     * view after opening a project
     */
    public void testNodesAfterOpen() {
        assertEquals("Some node not shown", 3, getRestNode().getChildren().length); //NOI18N
        assertEquals("Some method not shown for " + services[0], 3, //NOI18N
                getMethodsNode(services[0]).getChildren().length); //NOI18N
        assertEquals("Some method not shown for " + services[1], 1, //NOI18N
                getMethodsNode(services[1]).getChildren().length); //NOI18N
        assertEquals("Some method not shown for " + services[2], 2, //NOI18N
                getMethodsNode(services[2]).getChildren().length); //NOI18N
        assertEquals("Offending locator for " + services[0], 0, //NOI18N
                getSubresourcesNode(services[0]).getChildren().length); //NOI18N
        assertEquals("Missing locator for " + services[1], 1, //NOI18N
                getSubresourcesNode(services[1]).getChildren().length); //NOI18N
        assertEquals("Offending locator for " + services[2], 0, //NOI18N
                getSubresourcesNode(services[2]).getChildren().length); //NOI18N
    }

    /**
     * Test "Open" action on the Resource node
     */
    public void testOpenOnResource() {
        Node n = getResourceNode(services[2]);
        String open = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        n.performPopupAction(open);
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
    }

    /**
     * Test "Open" action on the resource's method node
     */
    public void testOpenOnMethod() {
        Node n = new Node(getMethodsNode(services[0]), "getXML"); //NOI18N
        String open = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        n.performPopupAction(open);
        final EditorOperator eo = new EditorOperator(services[0]);
        eo.waitState(new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                return eo.getLineNumber() == 73;
            }

            @Override
            public String getDescription() {
                return "Line number 73 reached in " + services[0];
            }
        });
    }

    /**
     * Test "Open" action on the resource's sub resource locator node
     */
    public void testOpenOnLocator() {
        Node n = new Node(getSubresourcesNode(services[1]), "{name}"); //NOI18N
        String open = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        n.performPopupAction(open);
        final EditorOperator eo = new EditorOperator(services[1].substring(0, 13));
        eo.waitState(new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                return eo.getLineNumber() == 80;
            }

            @Override
            public String getDescription() {
                return "Line number 80 reached in " + services[1];
            }
        });
    }

    /**
     * Test new node visibility after adding a method to the resource
     */
    public void testAddMethod() {
        EditorOperator eo = new EditorOperator(services[0]);
        eo.select(92);
        eo.insert(addMethod);
        eo.save();
        Node addedMethodNode = new Node(getMethodsNode(services[0]), "postXml");
    }

    /**
     * Test new node visibility after removing a method from the resource
     */
    public void testRemoveMethod() {
        Node addedMethodNode = new Node(getMethodsNode(services[0]), "postXml");
        EditorOperator eo = new EditorOperator(services[0]);
        eo.deleteLine(93);
        eo.deleteLine(93);
        eo.deleteLine(93);
        eo.deleteLine(93);
        eo.save();
        addedMethodNode.waitNotPresent();
    }

    protected Node getResourceNode(String resourceName) {
        Node n = new Node(getRestNode(), resourceName);
        if (n.isCollapsed()) {
            n.expand();
        }
        return n;
    }

    protected Node getMethodsNode(String resourceName) {
        String methodsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_HttpMethods");
        Node n = new Node(getResourceNode(resourceName), methodsLabel);
        if (n.isCollapsed()) {
            n.expand();
        }
        return n;
    }

    protected Node getMethodNode(Node methodsNode, String methodName) {
        return new Node(methodsNode, methodName);
    }

    protected Node getSubresourcesNode(String resourceName) {
        String subresourceLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_SubResourceLocators");
        Node n = new Node(getResourceNode(resourceName), subresourceLabel);
        if (n.isCollapsed()) {
            n.expand();
        }
        return n;
    }

    protected Node getSubresourceNode(Node subresourcesNode, String locatorName) {
        return new Node(subresourcesNode, locatorName);
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, RestNodeTest.class,
                "testNodesAfterOpen",
                "testOpenOnResource",
                "testOpenOnMethod",
                "testOpenOnLocator",
                "testAddMethod",
                "testRemoveMethod",
                "testCloseProject");
    }
}
