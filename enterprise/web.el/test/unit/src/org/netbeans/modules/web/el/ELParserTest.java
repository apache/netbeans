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
package org.netbeans.modules.web.el;

import com.sun.el.parser.*;
import javax.el.ELException;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Currently no real tests but temporary test code just to help
 * understanding and debug the EL parser.
 */
public class ELParserTest extends TestCase {

    public ELParserTest(String name) {
        super(name);
    }

    @Test
    public void testParseDeferred() {
        String expr = "#{taskController.removeTask(cc.attrs.story, cc.attrs.task)}";
        Node result = ELParser.parse(expr);
        assertNotNull(result);
        assertNotNull(findIdentifier(result, "taskController"));
        assertNotNull(findMethod(result, "removeTask"));
        print(expr, result);
    }

    @Test
    public void testParseDeferred2() {
        String expr = "#{customer.name}";
        Node result = ELParser.parse(expr);
        assertNotNull(result);
        assertNotNull(findIdentifier(result, "customer"));
        assertNotNull(findProperty(result, "name"));
        print(expr, result);
    }

    @Test
    public void testParseImmediate() {
        String expr = "${sessionScope.cart.numberOfItems > 0}";
        Node result = ELParser.parse(expr);
        assertNotNull(result);
        assertNotNull(findIdentifier(result, "sessionScope"));
        assertNotNull(findProperty(result, "cart"));
        assertNotNull(findProperty(result, "numberOfItems"));
        assertNotNull(findNode(result, "0", AstInteger.class));
        print(expr, result);
    }

    @Test
    public void testParseImmediate2() {
        String expr = "${sessionScope.cart.total}";
        Node result = ELParser.parse(expr);
        assertNotNull(result);
        assertNotNull(findIdentifier(result, "sessionScope"));
        assertNotNull(findProperty(result, "cart"));
        assertNotNull(findProperty(result, "total"));
        print(expr, result);
    }

    @Test
    public void testParseImmediate3() {
        String expr = "${customer}";
        Node result = ELParser.parse(expr);
        assertNotNull(result);
        assertNotNull(findIdentifier(result, "customer"));
        print(expr, result);
    }

    @Test
    public void testParseImmediate4() {
        String expr = "${customer.address[\"street\"]}";
        Node result = ELParser.parse(expr);
        assertNotNull(result);
        assertNotNull(findIdentifier(result, "customer"));
        print(expr, result);
    }

    @Test
    public void testParseInvalid() {
        String expr = "${customer.ad";
        try {
            ELParser.parse(expr);
            fail("Should not parse: " + expr);
        } catch (ELException ele) {
            assertNotNull(ele.getMessage());
        }
    }

    @Test
    public void testParseEscaped() {
        String[] exprs = {"${'${'}cart.numberOfItems}",
        "${'#{'}cart.numberOfItems}",
        "\\#{cart.numberOfItems}", "\\${cart.numberOfItems}"};
        for (String expr : exprs) {
            Node result = ELParser.parse(expr);
            assertNull(findIdentifier(result, "cart"));
            assertNull(findProperty(result, "numberOfItems"));
            print(expr, result);
        }
    }

    public void testOffsets() {
        String expr = "#{foo.bar.baz}";
        Node result = ELParser.parse(expr);
        Node foo = findIdentifier(result, "foo");
        Node bar = findProperty(result, "bar");
        Node baz = findProperty(result, "baz");
        assertOffsets(foo, 2, 5);
        assertOffsets(bar, 6, 9);
        assertOffsets(baz, 10, 13);
    }

    public void testOffsets2() {
        String expr = "#{foo.bar(baz)}";
        Node result = ELParser.parse(expr);
        Node foo = findIdentifier(result, "foo");
        Node bar = findMethod(result, "bar");
        Node baz = findIdentifier(result, "baz");
        assertOffsets(foo, 2, 5);
        assertOffsets(bar, 6, 14);
        assertOffsets(baz, 10, 13);
        print(expr, result);
    }

    public void testOffsets3() {
        String expr = "#{foo.bar[\"baz\"]}";
        Node result = ELParser.parse(expr);
        Node foo = findIdentifier(result, "foo");
        Node bar = findProperty(result, "bar");
        Node brackets = findNode(result, AstBracketSuffix.class);
        Node bazString = findNode(result, "\"baz\"", AstString.class);
        assertOffsets(foo, 2, 5);
        assertOffsets(bar, 6, 9);
        assertOffsets(brackets, 9, 16);
        assertOffsets(brackets, 9, 16);
        assertOffsets(bazString, 10, 15);
    }

    public void testConcatenation() {
        String expr = "${'Welcome ' + customer.name + ' to our site.'}";
        Node result = ELParser.parse(expr);
        Node customer = findIdentifier(result, "customer");
        Node name = findProperty(result, "name");
        assertNotNull(customer);
        assertNotNull(name);
        assertOffsets(customer, 15, 23);
        assertOffsets(name, 24, 28);
    }

    public void testOperator01() {
        String expr = "${employees.where(e->e.firstName == 'Larry')}";
        Node result = ELParser.parse(expr);
        Node empl = findIdentifier(result, "employees");
        Node where = findMethod(result, "where");
        assertNotNull(empl);
        assertNotNull(where);
        assertOffsets(empl, 2, 11);
        assertOffsets(where, 12, 44);
    }

    public void testSemicolon01() {
        String expr = "${a = mybean.property; b}";
        Node result = ELParser.parse(expr);
        Node a = findIdentifier(result, "a");
        Node b = findIdentifier(result, "b");
        Node mybean = findIdentifier(result, "mybean");
        Node property = findProperty(result, "property");
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(mybean);
        assertNotNull(property);
        assertOffsets(a, 2, 3);
        assertOffsets(b, 23, 24);
        assertOffsets(mybean, 6, 12);
        assertOffsets(property, 13, 21);
    }

    public void testStaticField01() {
        String expr = "${T(java.lang.Boolean).TRUE}";
        Node result = ELParser.parse(expr);
        Node T = findFunction(result, "T");
        Node TRUE = findProperty(result, "TRUE");
        assertNotNull(T);
        assertNotNull(TRUE);
        assertOffsets(T, 2, 22);
        assertOffsets(TRUE, 23, 27);
    }

    public void testSet01() {
        String expr = "${{1, 2, 3}}";
        Node result = ELParser.parse(expr);
        Node set = findNode(result, null, AstMapData.class);
        Node entry = findNode(result, null, AstMapEntry.class);
        Node value = findNode(result, "2", AstInteger.class);
        assertNotNull(set);
        assertNotNull(entry);
        assertNotNull(value);
        assertOffsets(set, 2, 11);
        assertOffsets(value, 6, 7);
    }

    public void testList01() {
        String expr = "${[1, \"two\", 3]}";
        Node result = ELParser.parse(expr);
        Node set = findNode(result, null, AstListData.class);
        Node value = findNode(result, "\"two\"", AstString.class);
        assertNotNull(set);
        assertNotNull(value);
        assertOffsets(set, 2, 15);
        assertOffsets(value, 6, 11);
    }

    public void testMap01() {
        String expr = "${{\"one\":1, \"two\":2}}";
        Node result = ELParser.parse(expr);
        Node set = findNode(result, null, AstMapData.class);
        Node entry = findNode(result, null, AstMapEntry.class);
        Node value = findNode(result, "1", AstInteger.class);
        assertNotNull(set);
        assertNotNull(entry);
        assertNotNull(value);
        assertOffsets(set, 2, 20);
        assertOffsets(value, 9, 10);
    }

    private void assertOffsets(Node node, int start, int end) {
        assertEquals("Start offset", start, node.startOffset());
        assertEquals("End offset", end, node.endOffset());
    }

    private static Node findProperty(final Node root, final String image) {
        return findNode(root, image, AstDotSuffix.class);
    }

    private static Node findIdentifier(final Node root, final String image) {
        return findNode(root, image, AstIdentifier.class);
    }

    private static Node findMethod(final Node root, final String image) {
        return findNode(root, image, AstDotSuffix.class);
    }

    private static Node findFunction(final Node root, final String localName) {
        final Node[] result = new Node[1];
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node.getClass().equals(AstFunction.class)) {
                    AstFunction function = (AstFunction) node;
                    if (function.getLocalName() == null) {
                        result[0] = node;
                        return;
                    } else if (localName.equals(function.getLocalName())) {
                        result[0] = node;
                        return;
                    }
                }
            }
        });
        return result[0];
    }

    private static Node findNode(final Node root, final Class clazz) {
        return findNode(root, null, clazz);
    }

    private static Node findNode(final Node root, final String image, final Class clazz) {
        final Node[] result = new Node[1];
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node.getClass().equals(clazz)) {
                    if (image == null) {
                        result[0] = node;
                        return;
                    } else if (image.equals(node.getImage())) {
                        result[0] = node;
                        return;
                    }
                }
            }
        });
        return result[0];
    }

    static void print(String expr, Node node) {
        System.out.println("------------------------------");
        System.out.println("AST for " + expr);
        System.out.println("------------------------------");
        printTree(node, 0);
    }

    private static void printTree(Node node, int level) {
        StringBuilder indent = new StringBuilder(level);
        for (int i = 0; i < level; i++) {
            indent.append(" ");
        }
        System.out.println(indent.toString() + node + ", offset: start - " + node.startOffset()  + " end - " + node.endOffset() + ", image: " + node.getImage() + ", class: " + node.getClass().getSimpleName());
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            printTree(child, ++level);
        }
    }

}
