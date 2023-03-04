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
package org.netbeans.modules.css.editor;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author marek
 */
public class Css3UtilsTest extends TestBase {
    
    public Css3UtilsTest(String name) {
        super(name);
    }

    public void testGetOffsetRange() {
        assertEquals(OffsetRange.NONE, Css3Utils.getOffsetRange(new TestNode(0, 0)));
        assertEquals(OffsetRange.NONE, Css3Utils.getOffsetRange(new TestNode(-1, -1)));
        assertEquals(OffsetRange.NONE, Css3Utils.getOffsetRange(new TestNode(-20, 0)));
        assertEquals(OffsetRange.NONE, Css3Utils.getOffsetRange(new TestNode(0, -20)));
        assertEquals(OffsetRange.NONE, Css3Utils.getOffsetRange(new TestNode(10, 4)));
        
        assertEquals(new OffsetRange(0, 20), Css3Utils.getOffsetRange(new TestNode(0, 20)));
        assertEquals(new OffsetRange(10, 20), Css3Utils.getOffsetRange(new TestNode(10, 20)));
    }
    
    public void testGetDocumentOffsetRange() {
        Document doc = getDocument("div { color: blue; }");
        Source s = Source.create(doc);
        Snapshot snap = s.createSnapshot();
        
        assertEquals(OffsetRange.NONE, Css3Utils.getDocumentOffsetRange(new TestNode(0, 0), snap));
        assertEquals(OffsetRange.NONE, Css3Utils.getDocumentOffsetRange(new TestNode(-1, 0), snap));
        assertEquals(OffsetRange.NONE, Css3Utils.getDocumentOffsetRange(new TestNode(-1, -22), snap));
        
        assertEquals(new OffsetRange(1, 3), Css3Utils.getDocumentOffsetRange(new TestNode(1, 3), snap));
        
    }
    
    private static final class TestNode implements Node {

        private final int from, to;

        public TestNode(int from, int to) {
            this.from = from;
            this.to = to;
        }
        
        @Override
        public int from() {
            return from;
        }

        @Override
        public int to() {
            return to;
        }

        @Override
        public String name() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public NodeType type() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<Node> children() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Node parent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public CharSequence image() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
}
