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
