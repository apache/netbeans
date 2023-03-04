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

package org.netbeans.modules.html.editor;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Attribute;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Element;
import org.netbeans.modules.html.editor.lib.dtd.Registry;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class NbReaderProviderTest extends TestBase {

    public NbReaderProviderTest() {
        super(NbReaderProviderTest.class.getName());
    }

    public void testHTML32() {
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 3.2 Final//EN", null));
    }

    public void testHTML40() {
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 4.0//EN", null));
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 4.0 Transitional//EN", null));
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 4.0 Frameset//EN", null));
    }

    public void testHTML401() {
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 4.01//EN", null));
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 4.01 Transitional//EN", null));
        assertNotNull(Registry.getDTD("-//W3C//DTD HTML 4.01 Frameset//EN", null));
    }

    public void testXHTML() {
        assertNotNull(Registry.getDTD("-//W3C//DTD XHTML 1.0 Strict//EN", null));
        assertNotNull(Registry.getDTD("-//W3C//DTD XHTML 1.0 Transitional//EN", null));
        assertNotNull(Registry.getDTD("-//W3C//DTD XHTML 1.0 Frameset//EN", null));
    }

    public void testX() {
        DTD d = Registry.getDTD("-//W3C//DTD HTML 4.01//EN", null);
        List l = d.getElementList("");
        Set<Attribute> all = new TreeSet<>(new Comparator<Attribute>() {
            @Override
            public int compare(Attribute o1, Attribute o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });


        Set<Element> alltags = new TreeSet<>(new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });
        for(Object o : l) {
            Element e = (Element)o;
            alltags.add(e);
            for(Object a : e.getAttributeList("")) {
                all.add((Attribute)a);
            }
        }

//        for(Attribute a : all) {
//            System.out.print("\"" + a.getName() + "\", ");
//        }
//
        for(Element e : alltags) {
            System.out.print("\"" + e.getName().toLowerCase() + "\", ");
        }

    }

}
