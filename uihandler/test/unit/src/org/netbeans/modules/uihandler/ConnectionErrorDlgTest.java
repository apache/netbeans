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
package org.netbeans.modules.uihandler;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.uihandler.ConnectionErrorDlg.Segment;
import org.netbeans.modules.uihandler.ConnectionErrorDlg.SegmentKind;

/**
 *
 * @author Martin Entlicher
 */
public class ConnectionErrorDlgTest {
    
    public ConnectionErrorDlgTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of get method, of class ConnectionErrorDlg.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String msg = "";
        Object expResult = "";
        Object result = ConnectionErrorDlg.get(msg);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseMessage method, of class ConnectionErrorDlg.
     */
    @Test
    public void testParseMessage() {
        System.out.println("parseMessage");
        
        String msg = "Just a text";
        ConnectionErrorDlg.Segment[] expResult = null;
        ConnectionErrorDlg.Segment[] result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "Text with a link [URL]Link[/URL]";
        expResult = new Segment[] { new Segment(SegmentKind.LABEL, "Text with a link "),
                                    new Segment(SegmentKind.LINK, "Link") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "Two line text\nwith a link [URL]Link[/URL]";
        expResult = new Segment[] { new Segment(SegmentKind.LABEL, "Two line text", true),
                                    new Segment(SegmentKind.LABEL, "with a link "),
                                    new Segment(SegmentKind.LINK, "Link") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "Text with two links: [URL]Link1[/URL], [URL]Link2[/URL]";
        expResult = new Segment[] { new Segment(SegmentKind.LABEL, "Text with two links: "),
                                    new Segment(SegmentKind.LINK, "Link1"),
                                    new Segment(SegmentKind.LABEL, ", "),
                                    new Segment(SegmentKind.LINK, "Link2") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "Text with a [URL]Link[/URL] and a [TTC]text to copy[/TTC]";
        expResult = new Segment[] { new Segment(SegmentKind.LABEL, "Text with a "),
                                    new Segment(SegmentKind.LINK, "Link"),
                                    new Segment(SegmentKind.LABEL, " and a "),
                                    new Segment(SegmentKind.FIELD, "text to copy") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "Text with a [URL]Link[/URL][TTC]followed by text to copy[/TTC]";
        expResult = new Segment[] { new Segment(SegmentKind.LABEL, "Text with a "),
                                    new Segment(SegmentKind.LINK, "Link"),
                                    new Segment(SegmentKind.FIELD, "followed by text to copy") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "Text with two fields: [TTC]field 1[/TTC], [TTC]field 2[/TTC]";
        expResult = new Segment[] { new Segment(SegmentKind.LABEL, "Text with two fields: "),
                                    new Segment(SegmentKind.FIELD, "field 1"),
                                    new Segment(SegmentKind.LABEL, ", "),
                                    new Segment(SegmentKind.FIELD, "field 2") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        msg = "[URL]Many links[/URL] and many [TTC]fields 1[/TTC][URL]link 2[/URL] and some text "+
              "[TTC]field 2[/TTC] and [TTC]field 3[/TTC] and links [URL]link 3[/URL], [URL]link 4[/URL]";
        expResult = new Segment[] { new Segment(SegmentKind.LINK, "Many links"),
                                    new Segment(SegmentKind.LABEL, " and many "),
                                    new Segment(SegmentKind.FIELD, "fields 1"),
                                    new Segment(SegmentKind.LINK, "link 2"),
                                    new Segment(SegmentKind.LABEL, " and some text "),
                                    new Segment(SegmentKind.FIELD, "field 2"),
                                    new Segment(SegmentKind.LABEL, " and "),
                                    new Segment(SegmentKind.FIELD, "field 3"),
                                    new Segment(SegmentKind.LABEL, " and links "),
                                    new Segment(SegmentKind.LINK, "link 3"),
                                    new Segment(SegmentKind.LABEL, ", "),
                                    new Segment(SegmentKind.LINK, "link 4") };
        result = ConnectionErrorDlg.parseMessage(msg);
        assertArrayEquals(expResult, result);
        
        
    }
}
