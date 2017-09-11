/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
