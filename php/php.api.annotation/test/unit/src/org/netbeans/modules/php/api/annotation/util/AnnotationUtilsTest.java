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
package org.netbeans.modules.php.api.annotation.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@RunWith(Enclosed.class)
public class AnnotationUtilsTest {

    private static final String ANNOTATION_NAME = "Annotation";

    @RunWith(Parameterized.class)
    public static class IsTypeAnnotationValidTest {

        @Parameters(name = "{index}: {0} is a type annotation of {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {"\\Foo\\Bar\\Baz\\" + ANNOTATION_NAME, ANNOTATION_NAME},
                {"Foo\\Bar\\Baz\\" + ANNOTATION_NAME, ANNOTATION_NAME},
                {ANNOTATION_NAME, ANNOTATION_NAME},
                {ANNOTATION_NAME.toLowerCase(), ANNOTATION_NAME},
                {"Foo\\Bar\\Baz\\" + ANNOTATION_NAME.toLowerCase(), ANNOTATION_NAME},
            });
        }

        @Parameter(0)
        public String lineToCheck;

        @Parameter(1)
        public String annotationName;

        @Test
        public void test() throws Exception {
            assertTrue(AnnotationUtils.isTypeAnnotation(lineToCheck, annotationName));
        }
    }

    @RunWith(Parameterized.class)
    public static class IsTypeAnnotationInvalidTest {

        @Parameters(name = "{index}: {0} is not a type annotation of {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {ANNOTATION_NAME + "\\Foo\\Bar\\Baz\\", ANNOTATION_NAME},
                {"\\Foo\\Bar" + ANNOTATION_NAME + "\\Baz\\", ANNOTATION_NAME},
            });
        }

        @Parameter(0)
        public String lineToCheck;

        @Parameter(1)
        public String annotationName;

        @Test
        public void test() throws Exception {
            assertFalse(AnnotationUtils.isTypeAnnotation(lineToCheck, annotationName));
        }
    }

    @RunWith(Parameterized.class)
    public static class IsTypeAnnotationNotNullableTest {

        @Parameters(name = "{index}: invoking isTypeAnnotation with [{0}, {1}] throws NullPointerException")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {null, ""},
                {"", null},
            });
        }

        @Parameter(0)
        public String lineToCheck;

        @Parameter(1)
        public String annotationName;

        @Test
        public void test() throws Exception {
            try {
                AnnotationUtils.isTypeAnnotation(lineToCheck, annotationName);
                fail();
            } catch (NullPointerException ex) {}
        }
    }

    @RunWith(Parameterized.class)
    public static class ExtractTypesFromParametersTest {

        @Parameters(name = "{index}: extracting types from {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {
                    "DiscriminatorMap({\"person\" = \" Person \", \"employee\" = \" Employee \"})",
                    new HashSet<String>() {{
                        add("");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(31, 37), "Person");
                        put(new OffsetRange(56, 64), "Employee");
                    }},
                },
                {
                    "DiscriminatorMap({\"person\" = Person, \"employee\" = Employee })",
                    new HashSet<String>() {{
                        add("");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(29, 35), "Person");
                        put(new OffsetRange(50, 58), "Employee");
                    }},
                },
                {
                    "DiscriminatorMap({\"person\" = Person::class, \"employee\" = \"Employee\" })",
                    new HashSet<String>() {{
                        add("");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(29, 35), "Person");
                        put(new OffsetRange(58, 66), "Employee");
                    }},
                },
                {
                    "DiscriminatorMap({\"person\" = \" My\\Person \", \"employee\" = \" \\Full\\Q\\Employee \"})",
                    new HashSet<String>() {{
                        add("");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(31, 40), "My\\Person");
                        put(new OffsetRange(59, 75), "\\Full\\Q\\Employee");
                    }},
                },
                {
                    "DiscriminatorMap({person = My\\Person, employee=\\Full\\Q\\Employee})",
                    new HashSet<String>() {{
                        add("");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(27, 36), "My\\Person");
                        put(new OffsetRange(47, 63), "\\Full\\Q\\Employee");
                    }},
                },
                {
                    "ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")",
                    new HashSet<String>() {{
                        add("targetEntity");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(24, 28), "Cart");
                    }},
                },
                {
                    "ManyToOne(targetEntity=Cart, cascade={\"all\"}, fetch=\"EAGER\")",
                    new HashSet<String>() {{
                        add("targetEntity");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(23, 27), "Cart");
                    }},
                },
                {
                    "ManyToOne(targetEntity=\"\\Foo\\Cart\", cascade={\"all\"}, fetch=\"EAGER\")",
                    new HashSet<String>() {{
                        add("targetEntity");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(24, 33), "\\Foo\\Cart");
                    }},
                },
                {
                    "ManyToOne(targetEntity=\"\\Foo\\Cart::class \" , cascade={\"all\"}, fetch=\"EAGER\")",
                    new HashSet<String>() {{
                        add("targetEntity");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(24, 33), "\\Foo\\Cart");
                    }},
                },
                {
                    "ManyToOne(targetEntity = \\Foo\\Cart, cascade={\"all\"}, fetch=\"EAGER\")",
                    new HashSet<String>() {{
                        add("targetEntity");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(25, 34), "\\Foo\\Cart");
                    }},
                },
                {
                    "@Enum(class=\"Visibility\")",
                    new HashSet<String>() {{
                        add("class");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(13, 23), "Visibility");
                    }},
                },
                {
                    "@Enum(class=\"Visibility::class\")",
                    new HashSet<String>() {{
                        add("class");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(13, 23), "Visibility");
                    }},
                },
                {
                    "@Enum(class=Visibility::class)",
                    new HashSet<String>() {{
                        add("class");
                    }},
                    new HashMap<OffsetRange, String>() {{
                        put(new OffsetRange(12, 22), "Visibility");
                    }},
                },
                {
                    // If there's a leading quote, then there must be a trailing quote as well. The other way around would work though.
                    "@Enum(class=\"Visibility)",
                    new HashSet<String>() {{
                        add("class");
                    }},
                    new HashMap<OffsetRange, String>(),
                },
            });
        }

        @Parameter(0)
        public String line;

        @Parameter(1)
        public Set<String> parameterNameRegexs;

        @Parameter(2)
        public Map<OffsetRange, String> expected;

        @Test
        public void test() throws Exception {
            Map<OffsetRange, String> actual = AnnotationUtils.extractTypesFromParameters(line, parameterNameRegexs);
            assertNotNull(actual);
            assertEquals(expected, actual);
        }
    }

    @RunWith(Parameterized.class)
    public static class ExtractTypesFromParametersNotNullableTest {

        @Parameters(name = "{index}: invoking extractTypesFromParameters with [{0}, {1}] throws NullPointerException")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {null, Collections.EMPTY_SET},
                {"", null},
            });
        }

        @Parameter(0)
        public String line;

        @Parameter(1)
        public Set<String> parameterNameRegexs;

        @Test
        public void test() throws Exception {
            try {
                AnnotationUtils.extractTypesFromParameters(line, parameterNameRegexs);
                fail();
            } catch (NullPointerException ex) {}
        }
    }

}
