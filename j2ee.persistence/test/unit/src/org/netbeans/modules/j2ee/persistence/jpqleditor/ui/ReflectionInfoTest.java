/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.jpqleditor.ui;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ReflectionInfoTest {

    public ReflectionInfoTest() {
    }

    @Test
    public void testSingleResult() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(1));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getIndex());
        assertNull(ri.get(0).getPropertyName());
    }

    @Test
    public void testSimpleObject() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(new BaseObject()));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getIndex());
        assertEquals("id", ri.get(0).getPropertyName());
    }

    @Test
    public void testInheritetObject() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(new DemoObject()));
        assertEquals(3, ri.size());
        assertNull(ri.get(0).getIndex());
        assertEquals("demoBool", ri.get(0).getPropertyName());
        assertNull(ri.get(1).getIndex());
        assertEquals("id", ri.get(1).getPropertyName());
        assertNull(ri.get(2).getIndex());
        assertEquals("title", ri.get(2).getPropertyName());
    }

    @Test
    public void testMixedList() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(new BaseObject[]{new DemoObject2(), new DemoObject()}));
        assertEquals(4, ri.size());
        assertNull(ri.get(0).getIndex());
        assertEquals("demoBool", ri.get(0).getPropertyName());
        assertNull(ri.get(1).getIndex());
        assertEquals("id", ri.get(1).getPropertyName());
        assertNull(ri.get(2).getIndex());
        assertEquals("title", ri.get(2).getPropertyName());
        assertNull(ri.get(3).getIndex());
        assertEquals("titleId", ri.get(3).getPropertyName());
    }

    @Test
    public void testMixedRowArray() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(new Object[]{1, new DemoObject()}));
        assertEquals(4, ri.size());
        assertEquals(0, ri.get(0).getIndex().intValue());
        assertNull(ri.get(0).getPropertyName());
        assertEquals(1, ri.get(1).getIndex().intValue());
        assertEquals("demoBool", ri.get(1).getPropertyName());
        assertEquals(1, ri.get(2).getIndex().intValue());
        assertEquals("id", ri.get(2).getPropertyName());
        assertEquals(1, ri.get(3).getIndex().intValue());
        assertEquals("title", ri.get(3).getPropertyName());
    }

    @Test
    public void testNullRowLeadsToNonArray() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                new Object[]{1, new DemoObject()},
                null,
                new Object[]{2, new DemoObject2()}
        ));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertNull(ri.get(0).getIndex());
    }
    
    @Test
    public void testNullValues() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                null,
                null,
                null
        ));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertNull(ri.get(0).getIndex());
    }
    
    @Test
    public void testNullValueArrays() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                new Object[] {null, null},
                new Object[] {null, null},
                new Object[] {null, null}
        ));
        assertEquals(2, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertEquals(0, ri.get(0).getIndex().intValue());
        assertNull(ri.get(1).getPropertyName());
        assertEquals(1, ri.get(1).getIndex().intValue());
    }
    
    @Test
    public void testPartitialNullValueArrays() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                new Object[] {null, null},
                new Object[] {1, new BaseObject()},
                new Object[] {null, null}
        ));
        assertEquals(3, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertEquals(0, ri.get(0).getIndex().intValue());
        assertNull(ri.get(1).getPropertyName());
        assertEquals(1, ri.get(1).getIndex().intValue());
        assertEquals("id", ri.get(2).getPropertyName());
        assertEquals(1, ri.get(2).getIndex().intValue());
    }
    
    public static class BaseObject {

        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class DemoObject2 extends BaseObject {

        private int titleId;

        public int getTitleId() {
            return titleId;
        }

        public void setTitleId(int titleId) {
            this.titleId = titleId;
        }

    }

    public static class DemoObject extends BaseObject {

        private String title;
        private boolean demoBool;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isDemoBool() {
            return demoBool;
        }

        public void setDemoBool(boolean demoBool) {
            this.demoBool = demoBool;
        }
    }
}
