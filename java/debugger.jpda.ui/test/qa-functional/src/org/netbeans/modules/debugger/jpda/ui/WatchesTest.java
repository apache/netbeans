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

package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutlineOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.openide.nodes.Node;

/**
 *
 * @author ehucka, Jiri Kovalsky
 */
public class WatchesTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testWatchesPublicVariables",
        "testWatchesProtectedVariables",
        "testWatchesPrivateVariables",
        "testWatchesPackagePrivateVariables",
        "testWatchesFiltersBasic",
        "testWatchesFiltersLinkedList",
        "testWatchesFiltersArrayList",
        "testWatchesFiltersVector",
        "testWatchesFiltersHashMap",
        "testWatchesFiltersHashtable",
        "testWatchesFiltersTreeMap",
        "testWatchesFiltersTreeSet",
        "testWatchesFilters1DArray",
        "testWatchesFilters2DArray",
        "testWatchesValues"
    };

    /**
     *
     * @param name
     */
    public WatchesTest(String name) {
        super(name);
    }
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    /**
     *
     * @return
     */
    public static Test suite() {
        return createModuleTest(WatchesTest.class, tests);
    }
    
    /**
     *
     */
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }
    
    /**
     *
     */
    public void tearDown() {
        super.tearDown();
        Utilities.deleteAllWatches();
    }
    
    /**
     *
     */
    public void testWatchesPublicVariables() {        
        //open source
        org.netbeans.jellytools.nodes.Node beanNode = new org.netbeans.jellytools.nodes.Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode); // NOI18N
        new EventTool().waitNoEvent(5000);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("Vpublic");
        createWatch("Spublic");
        createWatch("inheritedVpublic");
        createWatch("inheritedSpublic");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "Vpublic", "String", "\"Public Variable\"");
        checkOutlineTableLine(outlineOp, 1, "Spublic", "String", "\"Public Variable\"");
        checkOutlineTableLine(outlineOp, 2, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        checkOutlineTableLine(outlineOp, 3, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
    }
    
    /**
     *
     */
    public void testWatchesProtectedVariables() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("Vprotected");
        createWatch("Sprotected");
        createWatch("inheritedVprotected");
        createWatch("inheritedSprotected");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "Vprotected", "String", "\"Protected Variable\"");
        checkOutlineTableLine(outlineOp, 1, "Sprotected", "String", "\"Protected Variable\"");
        checkOutlineTableLine(outlineOp, 2, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        checkOutlineTableLine(outlineOp, 3, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
    }
    
    /**
     *
     */
    public void testWatchesPrivateVariables() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("Vprivate");
        createWatch("Sprivate");
        createWatch("inheritedVprivate");
        createWatch("inheritedSprivate");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "Vprivate", "String", "\"Private Variable\"");
        checkOutlineTableLine(outlineOp, 1, "Sprivate", "String", "\"Private Variable\"");
        checkOutlineTableLine(outlineOp, 2, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        checkOutlineTableLine(outlineOp, 3, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
    }
    
    /**
     *
     */
    public void testWatchesPackagePrivateVariables() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("VpackagePrivate");
        createWatch("SpackagePrivate");
        createWatch("inheritedVpackagePrivate");
        createWatch("inheritedSpackagePrivate");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "VpackagePrivate", "String", "\"Package-private Variable\"");
        checkOutlineTableLine(outlineOp, 1, "SpackagePrivate", "String", "\"Package-private Variable\"");
        checkOutlineTableLine(outlineOp, 2, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        checkOutlineTableLine(outlineOp, 3, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersBasic() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("1==1");
        createWatch("1==0");
        createWatch("Integer.toString(10)");
        createWatch("clazz");
        createWatch("n");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "1==1", "boolean", "true");
        checkOutlineTableLine(outlineOp, 1, "1==0", "boolean", "false");
        checkOutlineTableLine(outlineOp, 2, "Integer.toString(10)", "String", "\"10\"");
        checkOutlineTableLine(outlineOp, 3, "clazz", "Class", "class java.lang.Runtime");
        assertTrue("Node \'clazz\' has no child nodes", hasChildNodes("clazz", outlineOp));
        checkOutlineTableLine(outlineOp, 4, "n", "int", "50");
    }
    
    /**
     *
     */
    public void testWatchesFiltersLinkedList()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("llist");
        createWatch("llist.toString()");
        createWatch("llist.getFirst()");
        createWatch("llist.getLast()");
        createWatch("llist.get(1)");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "llist", "LinkedList", null);
        assertTrue("Node \'llist\' has no child nodes", hasChildNodes("llist", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "llist.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
        checkOutlineTableLine(outlineOp, 2, "llist.getFirst()", "String", "\"0. item\"");
        checkOutlineTableLine(outlineOp, 3, "llist.getLast()", "String", "\"49. item\"");
        checkOutlineTableLine(outlineOp, 4, "llist.get(1)", "String", "\"1. item\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersArrayList()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("alist");
        createWatch("alist.toString()");
        createWatch("alist.get(2)");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "alist", "ArrayList", null);
        assertTrue("Node \'alist\' has no child nodes", hasChildNodes("alist", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "alist.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
        checkOutlineTableLine(outlineOp, 2, "alist.get(2)", "String", "\"2. item\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersVector()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("vec");
        createWatch("vec.toString()");
        createWatch("vec.get(3)");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "vec", "Vector", null);
        assertTrue("Node \'vec\' has no child nodes", hasChildNodes("vec", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "vec.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
        checkOutlineTableLine(outlineOp, 2, "vec.get(3)", "String", "\"3. item\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersHashMap()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("hmap");
        createWatch("hmap.containsKey(\"4\")");
        createWatch("hmap.get(\"5\")");
        createWatch("hmap.put(\"6\",\"test\")");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "hmap", "HashMap", null);
        assertTrue("Node \'hmap\' has no child nodes", hasChildNodes("hmap", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "hmap.containsKey(\"4\")", "boolean", "true");
        checkOutlineTableLine(outlineOp, 2, "hmap.get(\"5\")", "String", "\"5. item\"");
        checkOutlineTableLine(outlineOp, 3, "hmap.put(\"6\",\"test\")", "String", "\"6. item\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersHashtable()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("htab");
        createWatch("htab.containsKey(\"7\")");
        createWatch("htab.get(\"9\")");
        createWatch("htab.put(\"10\", \"test\")");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "htab", "Hashtable", null);
        assertTrue("Node \'htab\' has no child nodes", hasChildNodes("htab", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "htab.containsKey(\"7\")", "boolean", "true");
        checkOutlineTableLine(outlineOp, 2, "htab.get(\"9\")", "String", "\"9. item\"");
        checkOutlineTableLine(outlineOp, 3, "htab.put(\"10\", \"test\")", "String", "\"10. item\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersTreeMap()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("tmap");
        createWatch("tmap.containsKey(\"11\")");
        createWatch("tmap.get(\"12\")");
        createWatch("tmap.put(\"13\",\"test\")");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "tmap", "TreeMap", null);
        assertTrue("Node \'tmap\' has no child nodes", hasChildNodes("tmap", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "tmap.containsKey(\"11\")", "boolean", "true");
        checkOutlineTableLine(outlineOp, 2, "tmap.get(\"12\")", "String", "\"12. item\"");
        checkOutlineTableLine(outlineOp, 3, "tmap.put(\"13\",\"test\")", "String", "\"13. item\"");
    }
    
    /**
     *
     */
    public void testWatchesFiltersTreeSet()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("tset");
        createWatch("tset.contains(\"14. item\")");
        createWatch("tset.iterator()");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "tset", "TreeSet", null);
        assertTrue("Node \'tset\' has no child nodes", hasChildNodes("tset", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "tset.contains(\"14. item\")", "boolean", "true");
        checkOutlineTableLine(outlineOp, 2, "tset.iterator()", "TreeMap$KeyIterator", null);
    }
    
    /**
     *
     */
    public void testWatchesFilters1DArray()  {

        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("policko");
        createWatch("policko.length");
        createWatch("policko[1]");
        createWatch("policko[10]");
        createWatch("pole");
        createWatch("pole.length");
        createWatch("pole[1]");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "policko", "int[]", null);
        assertTrue("Node \'policko\' has no child nodes", hasChildNodes("policko", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "policko.length", "int", "5");
        checkOutlineTableLine(outlineOp, 2, "policko[1]", "int", "2");
        checkOutlineTableLine(outlineOp, 3, "policko[10]", null, ">Array index \"10\" is out of range <0,4><");
        checkOutlineTableLine(outlineOp, 4, "pole", "int[]", null);
        assertTrue("Node \'pole\' has no child nodes", hasChildNodes("pole", outlineOp));
        checkOutlineTableLine(outlineOp, 5, "pole.length", "int", "50");
        checkOutlineTableLine(outlineOp, 6, "pole[1]", "int", "0");
    }
    
    /**
     *
     */
    public void testWatchesFilters2DArray()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 76);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76");
        createWatch("d2");
        createWatch("d2.length");
        createWatch("d2[1]");
        createWatch("d2[1].length");
        createWatch("d2[1][1]");
        createWatch("d2[15].length");
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        checkOutlineTableLine(outlineOp, 0, "d2", "int[][]", null);
        assertTrue("Node \'d2\' has no child nodes", hasChildNodes("d2", outlineOp));
        checkOutlineTableLine(outlineOp, 1, "d2.length", "int", "10");
        checkOutlineTableLine(outlineOp, 2, "d2[1]", "int[]", null);
        assertTrue("Node \'d2[1]\' has no child nodes", hasChildNodes("d2[1]", outlineOp));
        checkOutlineTableLine(outlineOp, 3, "d2[1].length", "int", "20");
        checkOutlineTableLine(outlineOp, 4, "d2[1][1]", "int", "0");
        checkOutlineTableLine(outlineOp, 5, "d2[15].length", null, ">Array index \"15\" is out of range <0,9><");
    }
    
    /**
     *
     */
    public void testWatchesValues()  {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 104);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104");

        createWatch("free");
        createWatch("taken");
        createWatch("total");
        createWatch("this");

        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.watchesViewTitle));
        Node.Property property;
        int count = 0;

        try {
            if (!("free".equals(outlineOp.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'free\' was not created", false);
            property = (Node.Property)outlineOp.getValueAt(count,1);
            if (!("long".equals(property.getValue())))
                assertTrue("Watch type for expression \'free\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)outlineOp.getValueAt(count++,2);
            long free = Long.parseLong(property.getValue().toString());

            if (!("taken".equals(outlineOp.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'taken\' was not created", false);
            property = (Node.Property)outlineOp.getValueAt(count,1);
            if (!("int".equals(property.getValue())))
                assertTrue("Watch type for expression \'taken\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)outlineOp.getValueAt(count++,2);
            long taken = Long.parseLong(property.getValue().toString());

            if (!("total".equals(outlineOp.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'total\' was not created", false);
            property = (Node.Property)outlineOp.getValueAt(count,1);
            if (!("long".equals(property.getValue())))
                assertTrue("Watch type for expression \'total\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)outlineOp.getValueAt(count++,2);
            long total = Long.parseLong(property.getValue().toString());

            assertTrue("Watches values does not seem to be correct (total != free + taken)", total == free + taken);

            if (!("this".equals(outlineOp.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'this\' was not created", false);
            property = (Node.Property)outlineOp.getValueAt(count,1);
            if (!("MemoryView".equals(property.getValue())))
                assertTrue("Watch type for expression \'this\' is " + property.getValue() + ", should be MemoryView", false);
            assertTrue("Watch this has no child nodes", hasChildNodes("this", outlineOp));
        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
    /**
     *
     * @param exp
     */
    protected void createWatch(String exp) {
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JEditorPaneOperator(dialog, 0).setText(exp);
        dialog.ok();
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object dialog) {
                    NbDialogOperator op = (NbDialogOperator)dialog;
                    if (!op.isVisible()) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
                
                public String getDescription() {
                    return "Wait new watch dialog is closed";
                }
            }).waitAction(dialog);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     *
     * @param table
     * @param lineNumber
     * @param name
     * @param type
     * @param value
     */
    protected void checkOutlineTableLine(OutlineOperator outline, int lineNumber, String name, String type, String value) {
        try {
            outline.scrollToCell(lineNumber, 0);
            org.openide.nodes.Node.Property property;
            String string = null;
            assertTrue("Node " + name + " not displayed in Watches view", name.equals(outline.getValueAt(lineNumber, 0).toString()));
            property = (org.openide.nodes.Node.Property)outline.getValueAt(lineNumber, 1);
            string = property.getValue().toString();
            int maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong type in Watches view (displayed: " + string + ", expected: " + type + ")",
                    (type == null) || type.length() == 0 || type.equals(string));
            property = (org.openide.nodes.Node.Property)outline.getValueAt(lineNumber, 2);
            string = property.getValue().toString();
            maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong value in Watches view (displayed: " + string + ", expected: " + value + ")",
                    (type == null) || !type.equals(string));
        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
    protected boolean hasChildNodes(String nodePath, OutlineOperator outlineOp) {
        OutlineNode node = new OutlineNode(outlineOp, nodePath);
        node.select();
        return !node.isLeaf();
    }
}
