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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import javax.lang.model.SourceVersion;

/**
 *
 * @author mjayan
 */
public class ConvertToNestedRecordPatternTest extends NbTestCase {

    public ConvertToNestedRecordPatternTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        if (!isRecordClassPresent()) {
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "record Rect(ColoredPoint upperLeft,ColoredPoint lr) {}\n"
                        + "record ColoredPoint(Point p, Color c) {}\n"
                        + "record Point(int x, int y){}\n"
                        + "enum Color {RED,GREEN,BLUE}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint ul, ColoredPoint lr)) {\n"
                        + "            Point p = ul.p();\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToNestedRecordPattern.class)
                .findWarning("7:25-7:63:verifier:" + Bundle.ERR_ConvertToNestedRecordPattern())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "record Rect(ColoredPoint upperLeft,ColoredPoint lr) {}\n"
                        + "record ColoredPoint(Point p, Color c) {}\n"
                        + "record Point(int x, int y){}\n"
                        + "enum Color {RED,GREEN,BLUE}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint(Point p, Color c) ul, ColoredPoint(Point p1, Color c1) lr)) {\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }

    public void testMultipleNested() throws Exception {
        if (!isRecordClassPresent()) {
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "record Rect(ColoredPoint upperLeft) {}\n"
                        + "record ColoredPoint(Point p, Color c) {}\n"
                        + "record Point(int x, int y){}\n"
                        + "enum Color {RED,GREEN,BLUE}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint(Point p, Color c) ul)) {\n"
                        + "            int x = p.x();\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToNestedRecordPattern.class)
                .findWarning("7:25-7:64:verifier:" + Bundle.ERR_ConvertToNestedRecordPattern())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "record Rect(ColoredPoint upperLeft) {}\n"
                        + "record ColoredPoint(Point p, Color c) {}\n"
                        + "record Point(int x, int y){}\n"
                        + "enum Color {RED,GREEN,BLUE}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint(Point(int x, int y) p, Color c) ul)) {\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }

    public void testUserVar() throws Exception {
        if (!isRecordClassPresent()) {
            return;
        }
        HintTest.create()
                .input("package test;\n"
                        + "record Rect(ColoredPoint upperLeft,ColoredPoint lr,ColoredPoint ur) {}\n"
                        + "record ColoredPoint(Point p, Color c) {}\n"
                        + "record Point(int x, int y){}\n"
                        + "enum Color {RED,GREEN,BLUE}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint(Point p, Color c) ul, ColoredPoint lr, ColoredPoint(Point p1, Color c1) ur)) {\n"
                        + "            int xVal = p.x();\n"
                        + "            int y1 = p.y();\n"
                        + "            Point p2 = lr.p();\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToNestedRecordPattern.class)
                .findWarning("7:25-7:118:verifier:" + Bundle.ERR_ConvertToNestedRecordPattern())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "record Rect(ColoredPoint upperLeft,ColoredPoint lr,ColoredPoint ur) {}\n"
                        + "record ColoredPoint(Point p, Color c) {}\n"
                        + "record Point(int x, int y){}\n"
                        + "enum Color {RED,GREEN,BLUE}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint(Point(int xVal, int y1) p, Color c) ul, ColoredPoint(Point p2, Color c2) lr, ColoredPoint(Point(int x, int y) p1, Color c1) ur)) {\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }

    private boolean isRecordClassPresent() {
        try {
            Class.forName("java.lang.Record");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
