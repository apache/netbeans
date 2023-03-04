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

package org.netbeans.modules.xml.schema.model.resolver;


import org.netbeans.modules.xml.schema.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.undo.UndoManager;
import org.junit.After;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author nn136682
 * @author Nikita Krjukov
 */
public class SchemaModelResolverTest {
    
    @After
    public void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * B includes C & D. C & D do not know anything about each other.
     * In this use-case, we'll explore components in C and it'll NOT resolve types
     * from D.
     *
     * It's impossible to resolve the reference if the B hasn't loaded yet.
     * If uncomment the first line, which loads the T1B.xsd, then the reference
     * becomes resolvable.
     */
    @Test
    public void testResolve1() throws Exception {
        // SchemaModel smB = Util.loadSchemaModel("resources/resolve1.zip", "resolve1/T1B.xsd");
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve1.zip", "resolve1/T1C.xsd");
        assert(sm.getState() == State.VALID);
        GlobalComplexType gct = (GlobalComplexType)sm.getSchema().getChildren().get(0);
        assertEquals(gct.getName(), "C1");
        LocalElement e1 = (LocalElement)gct.getChildren().get(0).getChildren().get(0);
        assertNotNull(e1);
        assertEquals(e1.getName(), "C11");
        NamedComponentReference ncr = e1.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "http://xml.netbeans.org/schema/B:D1");
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assertNull(gct);
        // assertEquals(gct.getName(), "D1");
    }
    
    /**
     * A imports B, B includes C. An element in A uses a complex type
     * defined in C. See http://www.netbeans.org/issues/show_bug.cgi?id=134861.
     */
    @Test
    public void testResolve2() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve2.zip", "resolve2/T2A.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertEquals("A1", ge.getName());
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/B:C1", name);
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals("C1", gct.getName());
    }
    
    /**
     * A imports B, B includes D. An element in A uses a complex type defined in D.
     * D doesn't have targetNamespace.
     */
    @Test
    public void testResolve3() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve3.zip", "resolve3/T3A.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertEquals("A2", ge.getName());
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/B:D1", name);
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals("D1", gct.getName());
    }
    
    /**
     * B includes C & D. B uses types defined in C. C uses types defined in D.
     * C & D do not know anything about each other.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=122836.
     * In this use-case, if you expand from B, it'll resolve types from D in C.
     */
    @Test
    public void testResolve4() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve4.zip", "resolve4/T4B.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(2);
        assertEquals("B2",ge.getName());
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/B:C2", name);
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "C2");
        LocalElement e1 = (LocalElement)gct.getChildren().get(0).getChildren().get(0);
        assertNotNull(e1);
        assertEquals(e1.getName(), "C21");
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/B:D1", name);
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "D1");
    }

    /**
     * Another one test related to the issue #122836
     * See http://www.netbeans.org/issues/show_bug.cgi?id=122836.
     * It tests more deep inclusions. The first solution has supported only
     * simple case, like is testein by testResolve4()
     *
     * B includes C & D; C includes E; D includes F.
     * B uses types defined in E. E uses types defined in F.
     * E & F do not know anything about each other.
     * In this use-case, if you expand from B, it'll resolve types from 
     * all included schema: C, D, E, F.
     */
    @Test
    public void testResolve5() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve5.zip", "resolve5/T5B.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(2);
        assertEquals("B3", ge.getName());
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/B:E2", name);
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "E2");
        LocalElement e1 = (LocalElement)gct.getChildren().get(0).getChildren().get(0);
        assertNotNull(e1);
        assertEquals(e1.getName(), "E21");
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/B:F1", name);
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "F1");
    }

    /**
     * A imports B, B imports G. An element in A uses a complex type defined in G.
     * It has to be not accessible.
     */
    @Test
    public void testResolve6() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve6.zip", "resolve6/T6A.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(2);
        assertEquals("G1", ge.getName());
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/G:G1", name);
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        //
        Import gImport = (Import)sm.getSchema().getChildren().get(1);
        assertEquals("T6G.xsd", gImport.getSchemaLocation());
        //
        UndoManager um = new javax.swing.undo.UndoManager();
        AbstractDocumentModel.class.cast(sm).addUndoableEditListener(um);
        //
        sm.startTransaction();
        try {
            sm.getSchema().removeExternalReference(gImport);
        } finally {
            sm.endTransaction();
        }
        //
        // Try resolve G2 type
        // index less by 1 because the import was deleted!
        ge = (GlobalElement)sm.getSchema().getChildren().get(2);
        assertEquals("G2", ge.getName());
        ncr = ge.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/G:G2", name);
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assertNull(gct);
        //
        um.undo();
    }

    /**
     * B includes C. C imports H. Definitions from H has to be visible from B
     * because the import declaration from C is included to B. 
     *
     */
    @Test
    public void testResolve7() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve7.zip", "resolve7/T7B.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertEquals("elemH", ge.getName());
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/H:H1", name);
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "H1");
        //
    }

    /**
     * B includes C imports D includes E. Everyone has target namespace.
     * A type from E has to be visible to B. 
     */
    @Test
    public void testResolve8() throws Exception {
        testResolve8to12("resources/resolve8.zip", "resolve8/T8B.xsd", "T8E.xsd"); // NOI18N
    }

    /**
     * B includes C imports D includes E. Everyone has target namespace except E.
     * A type from E has to be visible to B.
     */
    @Test
    public void testResolve9() throws Exception {
        testResolve8to12("resources/resolve9.zip", "resolve9/T9B.xsd", "T9E.xsd"); // NOI18N
    }

    /**
     * B includes C imports D includes E. Everyone has target namespace except C.
     * A type from E has to be visible to B.
     */
    @Test
    public void testResolve10() throws Exception {
        testResolve8to12("resources/resolve10.zip", "resolve10/T10B.xsd", "T10E.xsd"); // NOI18N
    }

    /**
     * B includes C imports D includes E. D & E have target namespace.
     * B & C don't have target namespace.
     * A type from E has to be visible to B.
     */
    @Test
    public void testResolve11() throws Exception {
        testResolve8to12("resources/resolve11.zip", "resolve11/T11B.xsd", "T11E.xsd"); // NOI18N
    }

    /**
     * B includes C imports D. F includes D & E. B & C & F have target namespace.
     * D & E don't have target namespace.
     * A type from E has to be visible to B.
     */
    @Test
    public void testResolve12() throws Exception {
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve12.zip", "resolve12/T12F.xsd");
        assertNotNull(sm);
        // 
        testResolve8to12("resources/resolve12.zip", "resolve12/T12B.xsd", "T12E.xsd"); // NOI18N
    }

    /**
     * B includes C imports D. F includes D & E. B & C have target namespace.
     * D & E & F don't have target namespace.
     * A type from E has to be visible to B.
     */
    @Test
    public void testResolve13() throws Exception {
        SchemaModel smF = Util.loadSchemaModel2("resources/resolve13.zip", "resolve13/T13F.xsd");
        assertNotNull(smF);
        //
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve13.zip", "resolve13/T13B.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertEquals("elemE", ge.getName()); // NOI18N
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(":E1", name); // NOI18N
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "E1"); // NOI18N
        assertEquals(gct.getModel().toString(), "T13E.xsd");
    }

    /**
     * B includes C & D. C & D do not know anything about each other.
     * D imports E.
     * In this use-case, we'll explore components in C and it'll NOT resolve types
     * from E.
     *
     */
    @Test
    public void testResolve14() throws Exception {
        SchemaModel smB = Util.loadSchemaModel2("resources/resolve14.zip", "resolve14/T14B.xsd");
        assertNotNull(smB);
        //
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve14.zip", "resolve14/T14C.xsd");
        assertNotNull(sm);
        assert(sm.getState() == State.VALID);
        GlobalElement el = (GlobalElement)sm.getSchema().getChildren().get(0);
        assertNotNull(el);
        assertEquals(el.getName(), "elemE");
        NamedComponentReference ncr = el.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "http://xml.netbeans.org/schema/E:E1");
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "E1");
    }

    /**
     * B includes C & D. C & D do not know anything about each other.
     * F includes E & G. E & G do not know anything about each other.
     * D imports E.
     * In this use-case, we'll explore components in C and it'll NOT resolve types
     * from G.
     *
     */
    @Test
    public void testResolve15() throws Exception {
        SchemaModel smB = Util.loadSchemaModel2("resources/resolve15.zip", "resolve15/T15B.xsd");
        assertNotNull(smB);
        //
        SchemaModel smF = Util.loadSchemaModel2("resources/resolve15.zip", "resolve15/T15F.xsd");
        assertNotNull(smF);
        //
        SchemaModel sm = Util.loadSchemaModel2("resources/resolve15.zip", "resolve15/T15C.xsd");
        assertNotNull(sm);
        assert(sm.getState() == State.VALID);
        GlobalElement el = (GlobalElement)sm.getSchema().getChildren().get(0);
        assertNotNull(el);
        assertEquals(el.getName(), "elemG");
        NamedComponentReference ncr = el.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "http://xml.netbeans.org/schema/F:G1");
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "G1");
        //
        assertNotNull(smB);
        assertNotNull(smF);
    }

    /**
     * B is included by A and E.
     * A, C, D form a reference cycle.
     * E also includes F and it includes C.
     *
     * Types from C and F has to be visible from B. 
     *
     */
    @Test
    public void testResolve16() throws Exception {
        SchemaModel smA = Util.loadSchemaModel2("resources/cyclic_dependencies.zip", "cyclic_dependencies/A.xsd");
        assertNotNull(smA);
        //
        SchemaModel smB = Util.loadSchemaModel2("resources/cyclic_dependencies.zip", "cyclic_dependencies/B.xsd");
        assertNotNull(smB);
        //
        SchemaModel smC = Util.loadSchemaModel2("resources/cyclic_dependencies.zip", "cyclic_dependencies/C.xsd");
        assertNotNull(smC);
        //
        SchemaModel smD = Util.loadSchemaModel2("resources/cyclic_dependencies.zip", "cyclic_dependencies/D.xsd");
        assertNotNull(smD);
        //
        SchemaModel smE = Util.loadSchemaModel2("resources/cyclic_dependencies.zip", "cyclic_dependencies/E.xsd");
        assertNotNull(smE);
        //
        SchemaModel smF = Util.loadSchemaModel2("resources/cyclic_dependencies.zip", "cyclic_dependencies/F.xsd");
        assertNotNull(smF);
        //
        assert(smB.getState() == State.VALID);
        GlobalElement el = (GlobalElement)smB.getSchema().getChildren().get(0);
        assertNotNull(el);
        assertEquals(el.getName(), "elemC");
        NamedComponentReference ncr = el.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "cyclic_dependencies_test:complexTypeC");
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "complexTypeC");
        //
        el = (GlobalElement)smB.getSchema().getChildren().get(1);
        assertNotNull(el);
        assertEquals(el.getName(), "elemF");
        ncr = el.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "cyclic_dependencies_test:complexTypeF");
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "complexTypeF");
        //
    }

    private void testResolve8to12(String zipFile, String schemaFile, String referencedSchemaFile)
            throws Exception {
        //
        SchemaModel sm = Util.loadSchemaModel2(zipFile, schemaFile);
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertEquals("elemE", ge.getName()); // NOI18N
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals("http://xml.netbeans.org/schema/D:E1", name); // NOI18N
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assertNotNull(gct);
        assertEquals(gct.getName(), "E1"); // NOI18N
        assertEquals(gct.getModel().toString(), referencedSchemaFile);
    }

}
