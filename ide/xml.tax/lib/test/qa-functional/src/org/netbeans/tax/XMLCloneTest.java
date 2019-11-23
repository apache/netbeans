/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.tax;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.XMLDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.TreeNamedObjectMap.KeyListener;
import org.netbeans.tax.TreeDocumentType.DTDIdentity;
import org.netbeans.tax.event.TreeEventChangeSupport;
import org.netbeans.tax.event.TreeEventManager;
import org.netbeans.tests.xml.XTest;


/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: XMLCloneTest
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test clones all nodes in tested document and checks whether the clone is properly created.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * The test pass trough the document's tree and for each node makes its clone.
 * On clones checks his nonstatic fields.
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * For each node one line with test result.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on April 9, 2001, 12:33 PM
 * <P>
 */
public class XMLCloneTest extends XTest {
    /** Creates new CoreSettingsTest */
    public XMLCloneTest(String testName) {
        super(testName);
    }
    
    // Debug variables
    private int maxCalls = 10000;
    private int listCount = 0;
    private int mapCount = 0;
    private Class testedLevel;
    
    // fields' patterns
    private Pattern[] patterns = new Pattern[] {
        //  clazz,                                          name,               isCloneRoot, checker
        new Pattern(TreeParentNode.class,                   null,               Boolean.TRUE,   NullChecker.class),
        new Pattern(TreeElement.class,                      "ownerElement",     Boolean.TRUE,   NullChecker.class),
        new Pattern(KeyListener.class,                      "mapKeyListener",   null,           DifferentOrNullChecker.class),//???
        new Pattern(TreeEventChangeSupport.class,           null,               null,           DifferentOrNullChecker.class),
        new Pattern(TreeEventManager.class,                 null,               null,           DifferentChecker.class),
        new Pattern(String.class,                           null,               null,           ImmutableChecker.class),
        new Pattern(TreeName.class,                         null,               null,           ImmutableChecker.class),
        new Pattern(DTDIdentity.class,                      null,               null,           ImmutableChecker.class),
    };
    
    public void testClone() throws Exception {
        TreeEditorCookie cake = (TreeEditorCookie) TestUtil.THIS.findData("Bookss.xml").getCookie(TreeEditorCookie.class);
        TreeDocument document = (TreeDocument) cake.openDocumentRoot();
        nodeTest(document);
    }
    
    private void nodeTest(TreeNode node) {
        treeCloneTest(node);
        // child test
        if (node instanceof TreeParentNode) {
            TreeChild child = ((TreeParentNode) node).getFirstChild();
            while (child != null) {
                nodeTest(child);
                child = child.getNextSibling();
            }
        }
        
        // attribute test
        if (node instanceof TreeElement && ((TreeElement) node).hasAttributes()) {
            Iterator attributes = ((TreeElement) node).getAttributes().iterator();
            while (attributes.hasNext()) {
                TreeNode attribute = (TreeNode) attributes.next();
                treeCloneTest(attribute);
            }
        }
    }
    
    private void treeCloneTest(TreeNode treeNode) {
        dbg.println("\n\n========> Creating clone of: " + treeNode + "::\n" + TestUtil.nodeToString(treeNode) + "\n\n");
        
        TreeNode treeClone = (TreeNode) treeNode.clone();
        nodeCloneTest(treeNode, treeClone, true);
    }
    
    private void nodeCloneTest(Object node, Object clone, boolean isCloneRoot) {
        dbg.println("\nNode: " + node + "\n<<<\n" + TestUtil.nodeToString(node) + "\n>>>");
        Class<?> clazz = node.getClass();
        do {
            cloneLevelCheck(clazz, node, clone, isCloneRoot);
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        
        // child test
        if (node instanceof TreeParentNode) {
            TreeChild childNode = ((TreeParentNode) node).getFirstChild();
            TreeChild childClone = ((TreeParentNode) clone).getFirstChild();
            while (childNode != null) {
                if (childClone == null) {
                    err("Missing clone child: " + TestUtil.nodeToString(childNode)
                    + "\nIn clone: " + TestUtil.nodeToString(clone), node);
                    break;
                }
                nodeCloneTest(childNode, childClone, false);
                childNode = childNode.getNextSibling();
                childClone = childClone.getNextSibling();
            }
        }
        
        // attribute test
        if (node instanceof TreeElement && ((TreeElement) node).hasAttributes()) {
            Iterator nodeAtrs = ((TreeElement) node).getAttributes().iterator();
            Iterator cloneAtrs = ((TreeElement) clone).getAttributes().iterator();
            while (nodeAtrs.hasNext()) {
                TreeNode nodeAtr = (TreeNode) nodeAtrs.next();
                TreeNode cloneAtr = (TreeNode) cloneAtrs.next();
                nodeCloneTest(nodeAtr, cloneAtr, false);
            }
        }
    }
    
    private void cloneLevelCheck(Class clazz, Object node, Object clone, boolean isCloneRoot) {
        if (maxCalls-- < 0) {//!!!
            System.exit(1);
        }
        
        dbg.println("Level: " + clazz);
        testedLevel = clazz;
        Field[] fields = clazz.getDeclaredFields();
        Class checker;
        String name;
        
        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers())) {
                continue;
            }
            
            // Checks TreeObjectList
            if (fields[i].getType() == TreeObjectList.class) {
                dbg.println("\n#" + listCount++ + ") CHECKING TREE_OBJECT_LIST: " + fields[i].getName());
                try {
                    // Get lists.
                    Field listField = TreeObjectList.class.getDeclaredField("list");
                    listField.setAccessible(true);
                    fields[i].setAccessible(true);
                    List nodeList = (List) listField.get(fields[i].get(node));
                    List cloneList = (List) listField.get(fields[i].get(clone));
                    
                    // Test lists' elements
                    if (isComparable(nodeList, cloneList, node)) {
                        for (int j = 0; j < nodeList.size(); j++) {
                            nodeCloneTest((TreeNode) nodeList.get(j), (TreeNode) cloneList.get(j), false);
                        }
                    }
                } catch (Exception ex) {
                    err("In TreeObjectList Check", node);
                    ex.printStackTrace(dbg);
                } finally {
                    dbg.println("/#" + --listCount + ") END CHECK\n");
                }
            }
            
            // Checks TreeNamedObjectMap
            if (fields[i].getType() == TreeNamedObjectMap.class) {
                Object key = null;
                
                dbg.println("\n@" + mapCount++ + ") CHECKING TREE_NAMED_OBJECT_MAP: " + fields[i].getName());
                try {
                    // Get maps
                    Field mapField = TreeNamedObjectMap.class.getDeclaredField("map");
                    mapField.setAccessible(true);
                    fields[i].setAccessible(true);
                    Map nodeMap = (Map) mapField.get(fields[i].get(node));
                    Map cloneMap = (Map) mapField.get(fields[i].get(clone));
                    
                    // Test maps' elements
                    if (isComparable(nodeMap, cloneMap, node)) {
                        
                        Object[] keys = nodeMap.keySet().toArray();
                        for (int j = 0; j < keys.length; j++) {
                            key = keys[j];
                            nodeCloneTest(nodeMap.get(key), cloneMap.get(key), false);
                        }
                    }
                } catch (Exception ex) {
                    err("In TreeNamedObjectMap Check: key = \"" + key + "\"", node);
                    ex.printStackTrace(dbg);
                } finally {
                    dbg.println("/@" + --mapCount + ") END CHECK\n");
                }
            }
            
            // find checker and check
            checker = DefaultChecker.class;
            for (int j = 0; j < patterns.length; j++) {
                if (patterns[j].compare(fields[i].getType(), fields[i].getName(), isCloneRoot)) {
                    checker = patterns[j].getChecker();
                    break;
                }
            }
            newCheckerInstace(checker, fields[i], node, clone).check();
        }
    }
    
    private boolean isComparable(List nodeList, List cloneList, Object node) {
        if (cloneList == null && nodeList == null) {
            return false;
            
        } else if (cloneList == null || nodeList == null) {
            err("List is Null:"
            + "\nnodeList = " + nodeList
            + "\ncloneList = " + cloneList, node);
            return false;
            
        } else if (nodeList.size() != cloneList.size()) {
            err("Lists have different size:"
            + "\nnodeList.size()  = " + nodeList.size()
            + "\ncloneList.size() = " + cloneList.size(), node);
            return false;
        }
        
        return true;
    }
    
    private boolean isComparable(Map nodeMap, Map cloneMap, Object node) {
        if (cloneMap == null && nodeMap == null) {
            return false;
            
        } else if (nodeMap != null && nodeMap.size() == 0 && cloneMap == null) {
            return false;
            
        } else if (cloneMap == null || nodeMap == null) {
            err("Map is Null:"
            + "\nnodeMap  = " + nodeMap
            + "\ncloneMap = " + cloneMap, node);
            return false;
            
        } else if (nodeMap.size() != cloneMap.size()) {
            err("Maps have different size:"
            + "\nnodeMap.size()  = " + nodeMap.size()
            + "\ncloneMap.size() = " + cloneMap.size(), node);
            return false;
        }
        
        return true;
    }
    
    private FieldChecker newCheckerInstace(Class clazz, Field field, Object node, Object clone) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor(new Class[] {XMLCloneTest.class, Field.class, Object.class, Object.class});
            return  (FieldChecker) constructor.newInstance(new Object[] {this, field, node, clone});
        } catch (Exception e) {
            e.printStackTrace(dbg);
            return null;
        }
    }
    
    private String toStr(Object obj) {
        String str = null;
        if (obj instanceof TreeNode) {
            try {
                str = TestUtil.nodeToString((TreeNode) obj);
            } catch (Exception e) {};
        } else {
            str = "" + obj;
        }
        return str;
    }
    
    protected void err(String message, Object node) {
        message =
        "\n!!! ERROR:" + message
        + "======================================================>"
        + "Node: " + TestUtil.nodeToString(node)
        + "Level: " + testedLevel;
        
        fail(message);
    }
    
    protected String xmlTestName() {
        return "XML-Clone-Test";
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        DEBUG = true;
        TestRunner.run(XMLCloneTest.class);
    }
    
    //@@@@
    
    private class Pattern {
        private Class _clazz;
        private String _name;
        private Boolean _isCloneRoot;
        private Class _checker;
        
        public Pattern(Class clazz, String name, Boolean isCloneRoot, Class checker) {
            _clazz = clazz;
            _name = name;
            _isCloneRoot = isCloneRoot;
            _checker = checker;
        }
        
        public boolean compare(Class clazz, String name, boolean isCloneRoot) {
            return
            ((_clazz == null) || (_clazz == clazz))
            && ((_isCloneRoot == null) || (_isCloneRoot.booleanValue() == isCloneRoot))
            && ((_name == null) || (_name.indexOf(name) != -1));
        }
        
        public Class getChecker() {
            return _checker;
        }
    }
    
    private abstract class FieldChecker {
        protected Field field;
        protected Object node;
        protected Object clone;
        
        public FieldChecker(Field field, Object node, Object clone) {
            this.field = field;
            this.node = node;
            this.clone = clone;
            this.field.setAccessible(true);
        }
        
        public boolean check() {
            if (test()) {
                dbg.println(prefix() + "Field \"" + field.getType().getName() + "::" + field.getName() + "\" is OK");
                return true;
            } else {
                reportErr();
                return false;
            }
        }
        
        protected boolean isBothNull() {
            return (fieldFrom(node) == null) && (fieldFrom(clone) == null);
        }
        
        protected  boolean hasDifferentID() {
            return fieldFrom(node) != fieldFrom(clone);
        }
        
        protected  boolean hasSameValue() {
            if (field.getClass().isPrimitive())
                return fieldFrom(node).equals(fieldFrom(clone));
            else if ((fieldFrom(node) != null) && (fieldFrom(clone) == null)) {//!!!
                return false;
            }
            return true;
        }
        
        protected Object fieldFrom(Object obj) {
            Object result = null;
            try {
                result = field.get(obj);
            } catch (Exception e) {
                e.printStackTrace(dbg);
            }
            return result;
        }
        
        private void reportErr() {
            String clazz;
            
            if (fieldFrom(node) != null) {
                clazz = fieldFrom(node).getClass().getName();
            } else {
                clazz = "Null.clazz";
            }
            
            err(prefix()
            + "\nClone error in field: \"" + field.getType().getName() + "::" + field.getName() + "\"."
            + "\nclazz         : " + clazz
            + "\noriginal value: " + toStr(fieldFrom(node))
            + "\nclone value   : " + toStr(fieldFrom(clone))
            + "\noriginal ID   : " + System.identityHashCode(fieldFrom(node))
            + "\nclone ID      : " + System.identityHashCode(fieldFrom(clone))
            + "\nhasDifferentID: " + hasDifferentID()
            + "\nhasSameValue  : " + hasSameValue()
            + "\nisBoothNull   : " + isBothNull()
            , node
            );
        }
        
        protected abstract boolean test();
        
        protected abstract String prefix();
    }
    
    /**
     * Check whether the fields have (different identity and identical value)
     * or are both Null.
     */
    private class DefaultChecker extends FieldChecker {
        
        public DefaultChecker(Field field, Object node, Object clone) {
            super(field, node, clone);
        }
        
        protected boolean test() {
            /*
            if (field.getName().equals("parentNode")) {
                System.out.println("parentNode on Node:" + fieldFrom(node));
                System.out.println("parentNode on Clone:" + fieldFrom(clone));
                return false;
            }
             */
            return (hasDifferentID() && hasSameValue()) || isBothNull();
        }
        
        protected String prefix() {
            return "DEFAULT  : ";
        }
    }
    
    /**
     * Check nothing, check() always return true.
     */
    private class EmptyChecker extends FieldChecker {
        
        public EmptyChecker(Field field, Object node, Object clone) {
            super(field, node, clone);
        }
        
        protected boolean test() {
            return true;
        }
        
        protected String prefix() {
            return "EMPTY    : ";
        }
    }
    
    /**
     * Check whether the clone's field is Null without reference to original filed.
     */
    private class NullChecker extends FieldChecker {
        
        public NullChecker(Field field, Object node, Object clone) {
            super(field, node, clone);
        }
        
        protected boolean test() {
            return fieldFrom(clone) == null;
        }
        
        protected String prefix() {
            return "NULL     : ";
        }
    }
    
    /**
     * Check whether the fields have same identity.
     */
    private class ImmutableChecker extends FieldChecker {
        
        public ImmutableChecker(Field field, Object node, Object clone) {
            super(field, node, clone);
        }
        
        protected boolean test() {
            return fieldFrom(clone) == fieldFrom(node);
        }
        
        protected String prefix() {
            return "IMMUTABLE: ";
        }
    }
    
    /**
     * Check whether the fields have different identity.
     */
    private class DifferentOrNullChecker extends FieldChecker {
        
        public DifferentOrNullChecker(Field field, Object node, Object clone) {
            super(field, node, clone);
        }
        
        protected boolean test() {
            return (fieldFrom(clone) != fieldFrom(node) || (fieldFrom(clone) == null && fieldFrom(node) == null));
        }
        
        protected String prefix() {
            return "DIFFERENT_OR_NULL: ";
        }
    }
    
    /**
     * Check whether the fields have different identity.
     */
    private class DifferentChecker extends FieldChecker {
        
        public DifferentChecker(Field field, Object node, Object clone) {
            super(field, node, clone);
        }
        
        protected boolean test() {
            return fieldFrom(clone) != fieldFrom(node);
        }
        
        protected String prefix() {
            return "DIFFERENT: ";
        }
    }
}
