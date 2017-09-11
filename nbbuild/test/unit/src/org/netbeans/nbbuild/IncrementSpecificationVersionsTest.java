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

package org.netbeans.nbbuild;

import org.netbeans.junit.NbTestCase;
import static org.netbeans.nbbuild.IncrementSpecificationVersions.*;

/** Test for increments of spec versions.
 *
 * @author Jaroslav Tulach
 */
public class IncrementSpecificationVersionsTest extends NbTestCase {

    public IncrementSpecificationVersionsTest(String testName) {
        super(testName);
    }

    public void testIncrementTrunkManifest() throws Exception {        
        boolean manifest = true;
        int sticky = 1;
        
        assertEquals("1.1", increment("1.0", sticky, manifest));
        assertEquals("1.2", increment("1.1", sticky, manifest));
        assertEquals("2.3", increment("2.2", sticky, manifest));
        assertEquals("3.12", increment("3.11", sticky, manifest));
        assertEquals("203.215", increment("203.214", sticky, manifest));
        
        assertEquals("1.9", increment("1.8.1", sticky, manifest));
        assertEquals("1.10", increment("1.9.1", sticky, manifest));        
        assertEquals("1.1", increment("1.0", sticky, manifest));
        assertEquals("1.7", increment("1.6", sticky, manifest));
        assertEquals("2.4", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.4", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.4", increment("2.3.8.1.5.6", sticky, manifest));
        
        assertEquals("1.1", increment("1", sticky, manifest));
        assertEquals("100.1", increment("100", sticky, manifest));
    }
    
    public void testIncrementTrunkSVB() throws Exception {
        boolean manifest = false;
        int sticky = 1;
                
        assertEquals("1.1.0", increment("1.0.0", sticky, manifest));
        assertEquals("1.2.0", increment("1.1.0", sticky, manifest));
        assertEquals("2.3.0", increment("2.2.0", sticky, manifest));
        assertEquals("3.12.0", increment("3.11.0", sticky, manifest));
        assertEquals("203.215.0", increment("203.214.0", sticky, manifest));

        assertEquals("1.9.0", increment("1.8.1", sticky, manifest));
        assertEquals("1.10.0", increment("1.9.1", sticky, manifest));        
        assertEquals("1.2.0", increment("1.0", sticky, manifest));
        assertEquals("1.8.0", increment("1.6", sticky, manifest));
        assertEquals("2.4.0", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.4.0", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.4.0", increment("2.3.8.1.5.6", sticky, manifest));
                
        assertEquals("2.1.0", increment("1", sticky, manifest));
        assertEquals("101.1.0", increment("100", sticky, manifest));
    }

    public void testIncrementBranchManifest() throws Exception {
        boolean manifest = true;
        int sticky = 2;
        
        assertEquals("1.0.1", increment("1.0", sticky, manifest));
        assertEquals("1.1.1", increment("1.1", sticky, manifest));
        assertEquals("2.2.1", increment("2.2", sticky, manifest));
        assertEquals("3.11.1", increment("3.11", sticky, manifest));
        assertEquals("203.214.1", increment("203.214", sticky, manifest));
        
        assertEquals("1.0.5", increment("1.0.4", sticky, manifest));
        assertEquals("1.1.7", increment("1.1.6", sticky, manifest));
        assertEquals("2.2.8", increment("2.2.7", sticky, manifest));
        assertEquals("3.11.10", increment("3.11.9", sticky, manifest));
        assertEquals("203.214.1001", increment("203.214.1000", sticky, manifest));
        
        assertEquals("2.3.9", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5.6", sticky, manifest));
                
        assertEquals("1.0.1", increment("1", sticky, manifest));
        assertEquals("100.0.1", increment("100", sticky, manifest));
    }

    public void testIncrementBranchSVB() throws Exception {
        boolean manifest = false;
        int sticky = 2;
        
        assertEquals("1.1.1", increment("1.0", sticky, manifest));
        assertEquals("1.2.1", increment("1.1", sticky, manifest));
        assertEquals("2.3.1", increment("2.2", sticky, manifest));
        assertEquals("3.12.1", increment("3.11", sticky, manifest));
        assertEquals("203.215.1", increment("203.214", sticky, manifest));
                
        assertEquals("1.0.1", increment("1.0.0", sticky, manifest));
        assertEquals("1.1.1", increment("1.1.0", sticky, manifest));
        assertEquals("2.2.1", increment("2.2.0", sticky, manifest));
        assertEquals("3.11.1", increment("3.11.0", sticky, manifest));
        assertEquals("203.214.1", increment("203.214.0", sticky, manifest));
                
        assertEquals("2.3.9", increment("2.3.8.1", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5", sticky, manifest));
        assertEquals("2.3.9", increment("2.3.8.1.5.6", sticky, manifest));
                
        assertEquals("2.0.1", increment("1", sticky, manifest));
        assertEquals("101.0.1", increment("100", sticky, manifest));
    }
    
    public void testIncrementLevel4() {
        assertIncrement("1.2.3.4.5.6.7", 4, true, "1.2.3.4.6");
        assertIncrement("1.0", 4, true, "1.0.0.0.1");
        assertIncrement("1.2.3.4.5", 4, true, "1.2.3.4.6");
    }

    private static void assertIncrement(String old, int stickyLevel, boolean manifest, String res) {
        String r = IncrementSpecificationVersions.increment(old, stickyLevel, manifest);
        assertEquals("Old: " + old + " stickyLevel: " + stickyLevel + " manifest: " + manifest, res, r);
    }

}
