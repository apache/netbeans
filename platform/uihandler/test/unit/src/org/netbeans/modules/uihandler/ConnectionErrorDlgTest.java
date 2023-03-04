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
