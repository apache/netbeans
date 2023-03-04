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
package org.netbeans.modules.css.prep.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

public class CssPreprocessorUtilsTest extends NbTestCase {

    public CssPreprocessorUtilsTest(String name) {
        super(name);
    }

    @Test
    public void testEncodeMappings() {
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/my.sass", "/my.css"),
                Pair.of("/your.sass", "/css"),
                Pair.of("/sass", "/css"),
                Pair.of("/other/sass", "/css"),
                Pair.of("sass", "css"),
                Pair.of(".", "."));
        String encoded =
                "/my.sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/my.css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/your.sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/other/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "." + CssPreprocessorUtils.MAPPING_DELIMITER + ".";
        assertEquals(encoded, CssPreprocessorUtils.encodeMappings(mappings));
    }

    @Test
    public void testDecodeMappings() {
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/my.sass", "/my.css"),
                Pair.of("/your.sass", "/css"),
                Pair.of("/sass", "/css"),
                Pair.of("/other/sass", "/css"),
                Pair.of("sass", "css"),
                Pair.of(".", "."));
        String encoded =
                "/my.sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/my.css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/your.sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/other/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "." + CssPreprocessorUtils.MAPPING_DELIMITER + ".";
        assertEquals(mappings, CssPreprocessorUtils.decodeMappings(encoded));
    }

    @Test
    public void testResolveTarget() {
        File root = new File("/root");
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/my.scss", "/your.css"),
                Pair.of("/scss", "/css"),
                Pair.of("/another/scss", "/another/css"),
                Pair.of(" /space/at/beginning ", " /space/in/output "));
        File file1 = new File(root, "scss/file1.scss");
        assertEquals(new File(root, "css/file1.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file1));
        File file2 = new File(root, "another/scss/file2.scss");
        assertEquals(new File(root, "another/css/file2.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file2));
        File file3 = new File(root, "file3.scss");
        assertNull(CssPreprocessorUtils.resolveTarget(root, mappings, file3));
        File file4 = new File("/file4.scss");
        assertNull(CssPreprocessorUtils.resolveTarget(root, mappings, file4));
        File file5 = new File(root, "/space/at/beginning/file5.scss");
        assertEquals(new File(root, "/space/in/output/file5.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file5));
        File myScss = new File(root, "/my.scss");
        assertEquals(new File(root, "/your.css"), CssPreprocessorUtils.resolveTarget(root, mappings, myScss));
        File yourScss = new File(root, "/your.scss");
        assertNull(CssPreprocessorUtils.resolveTarget(root, mappings, yourScss));

        mappings = Collections.singletonList(Pair.of(".", "."));
        File file0 = new File(root, "hola/file0.scss");
        assertEquals(new File(root, "hola/file0.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file0));
    }

    @Test
    public void testResolveTargetOutsideWebRoot() {
        File root = new File("/root");
        File webRoot = new File(root, "web");
        List<Pair<String, String>> mappings = Collections.singletonList(Pair.of("../scss", "/css"));
        File input1 = new File(root, "scss/file1.scss");
        assertEquals(new File(webRoot, "css/file1.css"), CssPreprocessorUtils.resolveTarget(webRoot, mappings, input1));
        File input2 = new File(root, "scss/subdir/file2.scss");
        assertEquals(new File(webRoot, "css/subdir/file2.css"), CssPreprocessorUtils.resolveTarget(webRoot, mappings, input2));
    }

    @Test
    public void testValidMappings() throws Exception {
        // non-existing folder
        File root = new File("/root");
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/my.scss", "/your.css"),
                Pair.of("/one.scss", "/xyz.css"),
                Pair.of("/two.scss", "/css"),
                Pair.of("/scss", "/css"),
                Pair.of("/another/scss", "/another/css"),
                Pair.of(".", "."));
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator("scss")
                .validate(root, mappings)
                .getResult();
        assertTrue(validationResult.isFaultless());
        // existing folder
        root = getWorkDir();
        File myScss = new File(root, "my.scss");
        if (!myScss.isFile()) {
            assertTrue(myScss.createNewFile());
        }
        File yourCss = new File(root, "your.css");
        if (!yourCss.isFile()) {
            assertTrue(yourCss.createNewFile());
        }
        validationResult = new CssPreprocessorUtils.MappingsValidator("scss")
                .validate(root, mappings)
                .getResult();
        assertTrue(validationResult.isFaultless());
    }

    @Test
    public void testInvalidMappingsFormat() {
        File root = new File("/root");
        Pair<String, String> mapping1 = Pair.of("/sc" + CssPreprocessorUtils.MAPPING_DELIMITER + "ss", "/css");
        Pair<String, String> mapping2 = Pair.of("/scss", "   ");
        Pair<String, String> mapping3 = Pair.of("/scss", "/my.css");
        List<Pair<String, String>> mappings = Arrays.asList(mapping1, mapping2, mapping3);
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator("scss")
                .validate(root, mappings)
                .getResult();
        assertEquals(0, validationResult.getWarnings().size());
        assertEquals(3, validationResult.getErrors().size());
        ValidationResult.Message error1 = validationResult.getErrors().get(0);
        assertEquals("mapping." + mapping1.first(), error1.getSource());
        assertTrue(error1.getMessage(), error1.getMessage().contains(mapping1.first()));
        ValidationResult.Message error2 = validationResult.getErrors().get(1);
        assertEquals("mapping." + mapping2.second(), error2.getSource());
        assertEquals(Bundle.MappingsValidator_warning_output_empty(), error2.getMessage());
        ValidationResult.Message error3 = validationResult.getErrors().get(2);
        assertEquals("mapping.io." + mapping3.second(), error3.getSource());
        assertEquals(Bundle.MappingsValidator_warning_io_conflict(mapping3.first(), mapping3.second()), error3.getMessage());
    }

    @Test
    public void testInvalidRoot() throws Exception {
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator("scss")
                .validate((FileObject) null, Collections.singletonList(Pair.of("/scss", "/css")))
                .getResult();
        assertEquals(0, validationResult.getWarnings().size());
        assertEquals(1, validationResult.getErrors().size());
        ValidationResult.Message error1 = validationResult.getErrors().get(0);
        assertEquals("root", error1.getSource());
        assertEquals(Bundle.MappingsValidator_warning_root_invalid(), error1.getMessage());
    }

}
