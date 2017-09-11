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
