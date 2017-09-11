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

package org.netbeans.lib.editor.util.swing;

import org.netbeans.junit.NbTestCase;

public class BlockCompareTest extends NbTestCase {

    public BlockCompareTest(String testName) {
        super(testName);
    }

    public void testBlockCompare() throws Exception {
        BlockCompare bc;

        try {
            bc = BlockCompare.get(1,0,1,1);
            fail("AssertionError expected");
        } catch (AssertionError e) {
            // Expected
        }

        try {
            bc = BlockCompare.get(1,1,1,0);
            fail("AssertionError expected");
        } catch (AssertionError e) {
            // Expected
        }

        bc = BlockCompare.get(1,1,1,1);
        assertTrue(bc.before());
        assertTrue(bc.after());
        assertTrue(bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,2,3,4);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(3,4,1,2);
        assertTrue(!bc.before());
        assertTrue(bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,3,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(bc.overlap());
        assertTrue(bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,4,1,3);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,4,2,3);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,3,1,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(bc.inside());
        assertTrue(bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,3,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(bc.inside());
        assertTrue(bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,4,2,3);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,4,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,4,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,2,4,4);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(5,8,6,6);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,2,2,3);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,3,1,2);
        assertTrue(!bc.before());
        assertTrue(bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());
        
        // Test invalid bounds
        bc = BlockCompare.get(2,1,4,4);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,2,4,3);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(bc.invalidY());

        bc = BlockCompare.get(2,1,4,3);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(bc.invalidX());
        assertTrue(bc.invalidY());

        bc.toString(); // just test that it does not throw an exception
    }

}
