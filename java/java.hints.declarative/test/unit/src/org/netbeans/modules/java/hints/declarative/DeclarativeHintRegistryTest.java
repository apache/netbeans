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

package org.netbeans.modules.java.hints.declarative;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class DeclarativeHintRegistryTest extends NbTestCase {

    public DeclarativeHintRegistryTest(String name) {
        super(name);
    }

    public void testAdditionalConstraints() {
        Collection<Collection<? extends HintDescription>> allHints = DeclarativeHintRegistry.parseHints(null, "$1 :: $1 instanceof java.lang.String ;;").values();

        assertEquals(1, allHints.size());
        assertEquals(1, allHints.iterator().next().size());

        HintDescription hd = allHints.iterator().next().iterator().next();

        assertEquals(new HashSet<>(Arrays.asList("java.lang.String")), hd.getAdditionalConstraints().requiredErasedTypes);
    }

    public void testSuppressWarnings() {
        Collection<Collection<? extends HintDescription>> allHints = DeclarativeHintRegistry.parseHints(null, "$1.isDirectory <!suppress-warnings=isDirectory> :: $1 instanceof java.io.File ;;").values();

        assertEquals(1, allHints.size());
        assertEquals(1, allHints.iterator().next().size());

        HintDescription hd = allHints.iterator().next().iterator().next();

        assertEquals(Arrays.asList("isDirectory"), hd.getMetadata().suppressWarnings);
    }

    public void testEmptyFileShouldHaveHintMetadata() throws Exception {
        clearWorkDir();

        File wd = getWorkDir();
        File hint = new File(wd, "test.hint");

        TestUtilities.copyStringToFile(hint, "");

        Map<HintMetadata, Collection<? extends HintDescription>> allHints = DeclarativeHintRegistry.parseHints(FileUtil.toFileObject(hint), "");

        assertEquals(1, allHints.size());
        assertTrue(allHints.values().iterator().next().isEmpty());

        HintMetadata hm = allHints.keySet().iterator().next();

        assertEquals("test.hint", hm.id);
        assertEquals("test", hm.displayName);
    }

}