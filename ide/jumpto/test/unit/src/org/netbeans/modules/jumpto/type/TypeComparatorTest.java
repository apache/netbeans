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
package org.netbeans.modules.jumpto.type;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public class TypeComparatorTest extends NbTestCase {

    public TypeComparatorTest(final String name) {
        super(name);
    }

    public void testSorting() {

        TypeComparator tc = TypeComparator.create(GoToSettings.SortingType.LEVENSHTEIN, "JTC", false, false);   //NOI18N
        final TypeDescriptor[] tds = new TypeDescriptor[] {
            new MockTypeDescriptor("JavaTextComponent"),    //NOI18N
            new MockTypeDescriptor("JTextComponent"),       //NOI18N
        };
        Arrays.sort(tds, tc);
        assertEquals(
                Arrays.asList(
                    "JTextComponent",       //NOI18N
                    "JavaTextComponent"     //NOI18N
                ),
                Arrays.stream(tds)
                .map(TypeDescriptor::getSimpleName)
                .collect(Collectors.toList()));
    }

    private static class MockTypeDescriptor extends TypeDescriptor {
        private final String name;

        MockTypeDescriptor(String name) {
            this.name = name;
        }

        @Override
        public String getSimpleName() {
            return name;
        }

        @Override
        public String getOuterName() {
            return "";
        }

        @Override
        public String getTypeName() {
            return name;
        }

        @Override
        public String getContextName() {
            return "";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getProjectName() {
            return null;
        }

        @Override
        public Icon getProjectIcon() {
            return null;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public int getOffset() {
            return -1;
        }

        @Override
        public void open() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String toString() {
            return name;
        }

    }
}
