/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package threaddemo.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import junit.framework.TestCase;
import threaddemo.locking.RWLock;
import threaddemo.locking.Locks;
import threaddemo.locking.PrivilegedLock;
import threaddemo.util.TwoWaySupport;
import threaddemo.util.TwoWaySupport.DerivationResult;

/**
 * Test the two-way support.
 * @author Jesse Glick
 */
public class TwoWaySupportTest extends TestCase {

    private PrivilegedLock p;
    private SimpleTWS s;
    
    protected void setUp() throws Exception {
        p = new PrivilegedLock();
        RWLock l = Locks.readWrite(p);
        s = new SimpleTWS(l);
        p.enterWrite();
    }
    protected void tearDown() throws Exception {
        p.exitWrite();
    }
    
    public void testBasicDerivation() throws Exception {
        assertNull(s.getValueNonBlocking());
        assertNull(s.getStaleValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getValueBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getStaleValueNonBlocking());
        s.setString("new value");
        assertNull(s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getStaleValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getValueBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getStaleValueNonBlocking());
        s.setString("");
        assertNull(s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getStaleValueNonBlocking());
        try {
            Object v = s.getValueBlocking();
            fail("Should not be computed: " + v.toString());
        } catch (InvocationTargetException e) {
            assertEquals("empty string", e.getTargetException().getMessage());
        }
        assertNull(s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getStaleValueNonBlocking());
    }
    
    // XXX to test:
    // - mutation
    // - initiation
    // - asynchronous access
    // - delayed computation
    // - firing of changes
    // - forgetting
    
    /**
     * Underlying model: text string (String)
     * Broken if underlying model is ""!
     */
    private static final class SimpleTWS extends TwoWaySupport<List<String>, String, List<String>> {
        
        private String string = "initial value";
        
        public SimpleTWS(RWLock l) {
            super(l);
        }
        
        public String getString() {
            return string;
        }
        
        public void setString(String s) {
            this.string = s;
            invalidate(s);
        }
        
        // Impl TWS:
        
        protected String composeUnderlyingDeltas(String underlyingDelta1, String underlyingDelta2) {
            return underlyingDelta2;
        }
        
        protected DerivationResult<List<String>,List<String>> doDerive(List<String> oldValue, String undval) throws Exception {
            if (undval == null) {
                undval = getString();
            }
            if (undval.length() == 0) throw new Exception("empty string");
            List<String> v = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(undval);
            while (tok.hasMoreTokens()) {
                v.add(tok.nextToken());
            }
            return new DerivationResult<List<String>,List<String>>(v, oldValue != null ? v : null);
        }
        
        protected List<String> doRecreate(List<String> oldValue, List<String> l) throws Exception {
            StringBuffer b = new StringBuffer();
            Iterator<String> i = l.iterator();
            if (i.hasNext()) {
                b.append(i.next());
            }
            while (i.hasNext()) {
                b.append(' ');
                b.append(i.next());
            }
            string = b.toString();
            return l;
        }
        
    }
    
}
