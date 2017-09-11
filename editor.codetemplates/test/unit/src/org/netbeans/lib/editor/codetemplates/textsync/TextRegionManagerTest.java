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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;


/**
 * Test TextRegionManager correctness.
 *
 * @author mmetelka
 */
public class TextRegionManagerTest extends NbTestCase {

    public TextRegionManagerTest(java.lang.String testName) {
        super(testName);
    }

    public void testAddRegions() throws Exception {
        Document doc = new BaseDocument(false, "");
        TextRegionManager textRegionManager = TextRegionManager.get(doc, true);
        //                   0         1
        //                   01234567890123456879
        doc.insertString(0, "abc def abc ghi", null);
        TextRegion region1 = new TextRegion(0, 3);
        TextRegion region2 = new TextRegion(8, 11);
        TextSync textSync1 = new TextSync(region1, region2);
        TextRegion region3 = new TextRegion(0, 7);
        TextSync textSync2 = new TextSync(region3);
        TextSyncGroup textSyncGroup = new TextSyncGroup(textSync1, textSync2);
        textRegionManager.addGroup(textSyncGroup, 0);
        
        // Check contents
        List<TextRegion<?>> regions = textRegionManager.regions(); // pkg-private method
        TextRegion<?> region = regions.get(0);
        assertRegion(region, 0, 7);
        assertRegion(region.regions().get(0), 0, 3);
        assertRegion(regions.get(1), 8, 11);
        textRegionManager.stopGroupEditing(textSyncGroup);
        assertTextRegionManagerEmpty(textRegionManager);
        
        TextRegion textRegionOverlap = new TextRegion(2,4);
        TextSync textSyncOverlap = new TextSync(textRegionOverlap);
        textSyncGroup.addTextSync(textSyncOverlap);
        try {
            textRegionManager.addGroup(textSyncGroup, 0);
        } catch (IllegalArgumentException e) {
            // Expected
        }
        textSyncGroup.removeTextSync(textSyncOverlap);
        assertTextRegionManagerEmpty(textRegionManager);
        
        TextRegion textRegionWrapAll = new TextRegion(0, 15);
        TextSync textSyncWrapAll = new TextSync(textRegionWrapAll);
        textSyncGroup.addTextSync(textSyncWrapAll);
        textRegionManager.addGroup(textSyncGroup, 0);

        region = regions.get(0);
        assertRegion(region, 0, 15);
        regions = region.regions();
        region = regions.get(0);
        assertRegion(region, 0, 7);
        assertRegion(region.regions().get(0), 0, 3);
        assertRegion(regions.get(1), 8, 11);
    }
    
    private void assertRegion(TextRegion region, int startOffset, int endOffset) {
        assertEquals(startOffset, region.startOffset());
    }
    
    private void assertTextRegionManagerEmpty(TextRegionManager textRegionManager) {
        assertEquals(0, textRegionManager.regions().size());
    }
    
}
