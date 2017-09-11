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

package org.netbeans.modules.editor;

import java.net.URL;
import java.awt.Color;
import junit.framework.TestCase;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationLoadingTest extends TestCase {

    public AnnotationLoadingTest(String testName) {
        super(testName);
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp() throws Exception {
        EditorTestLookup.setLookup(
                new URL[] {
            EditorTestConstants.EDITOR_LAYER_URL,
                    getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/resources/annotations-test-layer.xml")
        },
                new Object[] {},
                getClass().getClassLoader()
                );
                
                AnnotationTypes.getTypes().registerLoader(new AnnotationsTest.AnnotationsLoader());
    }

    public void testAnnotationStatusAndColorRead() {
//            <file name="test-annotation-1">
//                <attr name="AnnotationStatus" stringvalue="error" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_ERROR, AnnotationTypes.getTypes().getType("test-annotation-1").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-1").isUseCustomSidebarColor());
        assertEquals(0, AnnotationTypes.getTypes().getType("test-annotation-1").getPriority());
//            <file name="test-annotation-2">
//                <attr name="AnnotationStatus" stringvalue="warning" />
//                <attr name="priority" stringvalue="100" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_WARNING, AnnotationTypes.getTypes().getType("test-annotation-2").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-2").isUseCustomSidebarColor());
        assertEquals(100, AnnotationTypes.getTypes().getType("test-annotation-2").getPriority());
//            <file name="test-annotation-3">
//                <attr name="AnnotationStatus" stringvalue="ok" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_OK, AnnotationTypes.getTypes().getType("test-annotation-3").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-3").isUseCustomSidebarColor());
//            <file name="test-annotation-4">
//                <attr name="AnnotationStatus" stringvalue="error" />
//                <attr name="color" stringvalue="0x000000" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_ERROR, AnnotationTypes.getTypes().getType("test-annotation-4").getSeverity());
        assertEquals(true, AnnotationTypes.getTypes().getType("test-annotation-4").isUseCustomSidebarColor());
        assertEquals(new Color(0, 0, 0), AnnotationTypes.getTypes().getType("test-annotation-4").getCustomSidebarColor());
//            <file name="test-annotation-5">
//                <attr name="AnnotationStatus" stringvalue="warning" />
//                <attr name="color" stringvalue="0x000000" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_WARNING, AnnotationTypes.getTypes().getType("test-annotation-5").getSeverity());
        assertEquals(true, AnnotationTypes.getTypes().getType("test-annotation-5").isUseCustomSidebarColor());
        assertEquals(new Color(0, 0, 0), AnnotationTypes.getTypes().getType("test-annotation-5").getCustomSidebarColor());
//            <file name="test-annotation-6">
//                <attr name="AnnotationStatus" stringvalue="ok" />
//                <attr name="color" stringvalue="0x000000" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_OK, AnnotationTypes.getTypes().getType("test-annotation-6").getSeverity());
        assertEquals(true, AnnotationTypes.getTypes().getType("test-annotation-6").isUseCustomSidebarColor());
        assertEquals(new Color(0, 0, 0), AnnotationTypes.getTypes().getType("test-annotation-6").getCustomSidebarColor());
//            <file name="test-annotation-7">
//                <attr name="AnnotationStatus" stringvalue="ok" />
//                <attr name="color" stringvalue="unparseable" />
//            </file>
        //not possible, throws exception
//        assertEquals(AnnotationType.Severity.STATUS_OK, AnnotationTypes.getTypes().getType("test-annotation-7").getSeverity());
//        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-7").isUseCustomSidebarColor());
//            <file name="test-annotation-8">
//                <attr name="AnnotationStatus" stringvalue="unknown" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_NONE, AnnotationTypes.getTypes().getType("test-annotation-8").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-8").isUseCustomSidebarColor());
//            <file name="test-annotation-browseable-1">
//                <attr name="AnnotationStatus" stringvalue="unknown" />
//                <attr name="browseable" boolvalue="true" />
//            </file>
        assertTrue(AnnotationTypes.getTypes().getType("test-annotation-browseable-1").isBrowseable());
//            <file name="test-annotation-browseable-2">
//                <attr name="AnnotationStatus" stringvalue="unknown" />
//                <attr name="browseable" boolvalue="false" />
//            </file>
        assertFalse(AnnotationTypes.getTypes().getType("test-annotation-browseable-2").isBrowseable());
    }
    
//    public void testEquals() {
//        AnnotationStatus testAnnotationStatus = StatusForAnnotationTypeQuery.getDefault().getStatusForAnnotationType("test-annotation-5");
//        
//        assertFalse(testAnnotationStatus.equals(null));
//        assertFalse(testAnnotationStatus.equals("test"));
//        assertFalse(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_OK)));
//        assertFalse(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_WARNING)));
//        assertFalse(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_OK, new Color(0, 0, 0))));
//        
//        assertTrue(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_WARNING, new Color(0, 0, 0))));
//    }
    
    public void testNotFoundAnnotation() {
        assertNull(AnnotationTypes.getTypes().getType("non-existent"));
    }
    
}
