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
package org.netbeans.modules.css.lib;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class W3CSelectorsTest extends CssTestBase {

//    private static final String[] TESTS = new String[]{
//        "tests/css3-modsel-1.xml",
//        "tests/css3-modsel-2.xml",
//        "tests/css3-modsel-3.xml",
//        "tests/css3-modsel-3a.xml",
//        "tests/css3-modsel-4.xml",
//        "tests/css3-modsel-5.xml",
//        "tests/css3-modsel-6.xml",
//        "tests/css3-modsel-7.xml",
//        "tests/css3-modsel-7b.xml",
//        "tests/css3-modsel-8.xml",
//        "tests/css3-modsel-9.xml",
//        "tests/css3-modsel-10.xml",
//        "tests/css3-modsel-11.xml",
//        "tests/css3-modsel-13.xml",
//        "tests/css3-modsel-14.xml",
//        "tests/css3-modsel-14b.xml",
//        "tests/css3-modsel-14c.xml",
//        "tests/css3-modsel-14d.xml",
//        "tests/css3-modsel-14e.xml",
//        "tests/css3-modsel-15.xml",
//        "tests/css3-modsel-15b.xml",
//        "tests/css3-modsel-15c.xml",
//        "tests/css3-modsel-16.xml",
//        "tests/css3-modsel-17.xml",
//        "tests/css3-modsel-18.xml",
//        "tests/css3-modsel-18a.xml",
//        "tests/css3-modsel-18b.xml",
//        "tests/css3-modsel-18c.xml",
//        "tests/css3-modsel-19.xml",
//        "tests/css3-modsel-19b.xml",
//        "tests/css3-modsel-20.xml",
//        "tests/css3-modsel-21.xml",
//        "tests/css3-modsel-21b.xml",
//        "tests/css3-modsel-21c.xml",
//        "tests/css3-modsel-22.xml",
//        "tests/css3-modsel-23.xml",
//        "tests/css3-modsel-24.xml",
//        "tests/css3-modsel-25.xml",
//        "tests/css3-modsel-27.xml",
//        "tests/css3-modsel-27a.xml",
//        "tests/css3-modsel-27b.xml",
//        "tests/css3-modsel-28.xml",
//        "tests/css3-modsel-28b.xml",
//        "tests/css3-modsel-29.xml",
//        "tests/css3-modsel-29b.xml",
//        "tests/css3-modsel-30.xml",
//        "tests/css3-modsel-31.xml",
//        "tests/css3-modsel-32.xml",
//        "tests/css3-modsel-33.xml",
//        "tests/css3-modsel-34.xml",
//        "tests/css3-modsel-35.xml",
//        "tests/css3-modsel-36.xml",
//        "tests/css3-modsel-37.xml",
//        "tests/css3-modsel-38.xml",
//        "tests/css3-modsel-39.xml",
//        "tests/css3-modsel-39a.xml",
//        "tests/css3-modsel-39b.xml",
//        "tests/css3-modsel-39c.xml",
//        "tests/css3-modsel-41.xml",
//        "tests/css3-modsel-41a.xml",
//        "tests/css3-modsel-42.xml",
//        "tests/css3-modsel-42a.xml",
//        "tests/css3-modsel-43.xml",
//        "tests/css3-modsel-43b.xml",
//        "tests/css3-modsel-44.xml",
//        "tests/css3-modsel-44b.xml",
//        "tests/css3-modsel-44c.xml",
//        "tests/css3-modsel-44d.xml",
//        "tests/css3-modsel-45.xml",
//        "tests/css3-modsel-45b.xml",
//        "tests/css3-modsel-45c.xml",
//        "tests/css3-modsel-46.xml",
//        "tests/css3-modsel-46b.xml",
//        "tests/css3-modsel-47.xml",
//        "tests/css3-modsel-48.xml",
//        "tests/css3-modsel-49.xml",
//        "tests/css3-modsel-50.xml",
//        "tests/css3-modsel-51.xml",
//        "tests/css3-modsel-52.xml",
//        "tests/css3-modsel-53.xml",
//        "tests/css3-modsel-54.xml",
//        "tests/css3-modsel-55.xml",
//        "tests/css3-modsel-56.xml",
//        "tests/css3-modsel-57.xml",
//        "tests/css3-modsel-57b.xml",
//        "tests/css3-modsel-59.xml",
//        "tests/css3-modsel-60.xml",
//        "tests/css3-modsel-61.xml",
//        "tests/css3-modsel-62.xml",
//        "tests/css3-modsel-63.xml",
//        "tests/css3-modsel-64.xml",
//        "tests/css3-modsel-65.xml",
//        "tests/css3-modsel-66.xml",
//        "tests/css3-modsel-66b.xml",
//        "tests/css3-modsel-67.xml",
//        "tests/css3-modsel-68.xml",
//        "tests/css3-modsel-69.xml",
//        "tests/css3-modsel-70.xml",
//        "tests/css3-modsel-72.xml",
//        "tests/css3-modsel-72b.xml",
//        "tests/css3-modsel-73.xml",
//        "tests/css3-modsel-73b.xml",
//        "tests/css3-modsel-74.xml",
//        "tests/css3-modsel-74b.xml",
//        "tests/css3-modsel-75.xml",
//        "tests/css3-modsel-75b.xml",
//        "tests/css3-modsel-76.xml",
//        "tests/css3-modsel-76b.xml",
//        "tests/css3-modsel-77.xml",
//        "tests/css3-modsel-77b.xml",
//        "tests/css3-modsel-78.xml",
//        "tests/css3-modsel-78b.xml",
//        "tests/css3-modsel-79.xml",
//        "tests/css3-modsel-80.xml",
//        "tests/css3-modsel-81.xml",
//        "tests/css3-modsel-81b.xml",
//        "tests/css3-modsel-82.xml",
//        "tests/css3-modsel-82b.xml",
//        "tests/css3-modsel-83.xml",
//        "tests/css3-modsel-86.xml",
//        "tests/css3-modsel-87.xml",
//        "tests/css3-modsel-87b.xml",
//        "tests/css3-modsel-88.xml",
//        "tests/css3-modsel-88b.xml",
//        "tests/css3-modsel-89.xml",
//        "tests/css3-modsel-90.xml",
//        "tests/css3-modsel-90b.xml",
//        "tests/css3-modsel-91.xml",
//        "tests/css3-modsel-92.xml",
//        "tests/css3-modsel-93.xml",
//        "tests/css3-modsel-94.xml",
//        "tests/css3-modsel-94b.xml",
//        "tests/css3-modsel-95.xml",
//        "tests/css3-modsel-96.xml",
//        "tests/css3-modsel-96b.xml",
//        "tests/css3-modsel-97.xml",
//        "tests/css3-modsel-97b.xml",
//        "tests/css3-modsel-98.xml",
//        "tests/css3-modsel-98b.xml",
//        "tests/css3-modsel-99.xml",
//        "tests/css3-modsel-99b.xml",
//        "tests/css3-modsel-100.xml",
//        "tests/css3-modsel-100b.xml",
//        "tests/css3-modsel-101.xml",
//        "tests/css3-modsel-101b.xml",
//        "tests/css3-modsel-102.xml",
//        "tests/css3-modsel-102b.xml",
//        "tests/css3-modsel-103.xml",
//        "tests/css3-modsel-103b.xml",
//        "tests/css3-modsel-104.xml",
//        "tests/css3-modsel-104b.xml",
//        "tests/css3-modsel-105.xml",
//        "tests/css3-modsel-105b.xml",
//        "tests/css3-modsel-106.xml",
//        "tests/css3-modsel-106b.xml",
//        "tests/css3-modsel-107.xml",
//        "tests/css3-modsel-107b.xml",
//        "tests/css3-modsel-108.xml",
//        "tests/css3-modsel-108b.xml",
//        "tests/css3-modsel-109.xml",
//        "tests/css3-modsel-109b.xml",
//        "tests/css3-modsel-110.xml",
//        "tests/css3-modsel-110b.xml",
//        "tests/css3-modsel-111.xml",
//        "tests/css3-modsel-111b.xml",
//        "tests/css3-modsel-112.xml",
//        "tests/css3-modsel-112b.xml",
//        "tests/css3-modsel-113.xml",
//        "tests/css3-modsel-113b.xml",
//        "tests/css3-modsel-114.xml",
//        "tests/css3-modsel-114b.xml",
//        "tests/css3-modsel-115.xml",
//        "tests/css3-modsel-115b.xml",
//        "tests/css3-modsel-116.xml",
//        "tests/css3-modsel-116b.xml",
//        "tests/css3-modsel-117.xml",
//        "tests/css3-modsel-117b.xml",
//        "tests/css3-modsel-118.xml",
//        "tests/css3-modsel-119.xml",
//        "tests/css3-modsel-120.xml",
//        "tests/css3-modsel-121.xml",
//        "tests/css3-modsel-122.xml",
//        "tests/css3-modsel-123.xml",
//        "tests/css3-modsel-123b.xml",
//        "tests/css3-modsel-124.xml",
//        "tests/css3-modsel-124b.xml",
//        "tests/css3-modsel-125.xml",
//        "tests/css3-modsel-125b.xml",
//        "tests/css3-modsel-126.xml",
//        "tests/css3-modsel-126b.xml",
//        "tests/css3-modsel-127.xml",
//        "tests/css3-modsel-127b.xml",
//        "tests/css3-modsel-128.xml",
//        "tests/css3-modsel-128b.xml",
//        "tests/css3-modsel-129.xml",
//        "tests/css3-modsel-129b.xml",
//        "tests/css3-modsel-130.xml",
//        "tests/css3-modsel-130b.xml",
//        "tests/css3-modsel-131.xml",
//        "tests/css3-modsel-131b.xml",
//        "tests/css3-modsel-132.xml",
//        "tests/css3-modsel-132b.xml",
//        "tests/css3-modsel-133.xml",
//        "tests/css3-modsel-133b.xml",
//        "tests/css3-modsel-134.xml",
//        "tests/css3-modsel-134b.xml",
//        "tests/css3-modsel-135.xml",
//        "tests/css3-modsel-135b.xml",
//        "tests/css3-modsel-136.xml",
//        "tests/css3-modsel-136b.xml",
//        "tests/css3-modsel-137.xml",
//        "tests/css3-modsel-137b.xml",
//        "tests/css3-modsel-138.xml",
//        "tests/css3-modsel-138b.xml",
//        "tests/css3-modsel-139.xml",
//        "tests/css3-modsel-139b.xml",
//        "tests/css3-modsel-140.xml",
//        "tests/css3-modsel-140b.xml",
//        "tests/css3-modsel-141.xml",
//        "tests/css3-modsel-141b.xml",
//        "tests/css3-modsel-142.xml",
//        "tests/css3-modsel-142b.xml",
//        "tests/css3-modsel-143.xml",
//        "tests/css3-modsel-143b.xml",
//        "tests/css3-modsel-144.xml",
//        "tests/css3-modsel-145a.xml",
//        "tests/css3-modsel-145b.xml",
//        "tests/css3-modsel-146a.xml",
//        "tests/css3-modsel-146b.xml",
//        "tests/css3-modsel-147a.xml",
//        "tests/css3-modsel-147b.xml",
//        "tests/css3-modsel-148.xml",
//        "tests/css3-modsel-149.xml",
//        "tests/css3-modsel-149b.xml",
//        "tests/css3-modsel-150.xml",
//        "tests/css3-modsel-151.xml",
//        "tests/css3-modsel-152.xml",
//        "tests/css3-modsel-153.xml",
//        "tests/css3-modsel-154.xml",
//        "tests/css3-modsel-155.xml",
//        "tests/css3-modsel-155a.xml",
//        "tests/css3-modsel-155b.xml",
//        "tests/css3-modsel-155c.xml",
//        "tests/css3-modsel-155d.xml",
//        "tests/css3-modsel-156.xml",
//        "tests/css3-modsel-156b.xml",
//        "tests/css3-modsel-156c.xml",
//        "tests/css3-modsel-157.xml",
//        "tests/css3-modsel-158.xml",
//        "tests/css3-modsel-159.xml",
//        "tests/css3-modsel-160.xml",
//        "tests/css3-modsel-161.xml",
//        "tests/css3-modsel-166.xml",
//        "tests/css3-modsel-166a.xml",
//        "tests/css3-modsel-167.xml",
//        "tests/css3-modsel-167a.xml",
//        "tests/css3-modsel-168.xml",
//        "tests/css3-modsel-168a.xml",
//        "tests/css3-modsel-169.xml",
//        "tests/css3-modsel-169a.xml",
//        "tests/css3-modsel-170.xml",
//        "tests/css3-modsel-170a.xml",
//        "tests/css3-modsel-170b.xml",
//        "tests/css3-modsel-170c.xml",
//        "tests/css3-modsel-170d.xml",
//        "tests/css3-modsel-171.xml",
//        "tests/css3-modsel-172a.xml",
//        "tests/css3-modsel-172b.xml",
//        "tests/css3-modsel-173a.xml",
//        "tests/css3-modsel-173b.xml",
//        "tests/css3-modsel-174a.xml",
//        "tests/css3-modsel-174b.xml",
//        "tests/css3-modsel-175a.xml",
//        "tests/css3-modsel-175b.xml",
//        "tests/css3-modsel-175c.xml",
//        "tests/css3-modsel-176.xml",
//        "tests/css3-modsel-177a.xml",
//        "tests/css3-modsel-177b.xml",
//        "tests/css3-modsel-178.xml",
//        "tests/css3-modsel-179.xml",
//        "tests/css3-modsel-179a.xml",
//        "tests/css3-modsel-180a.xml",
//        "tests/css3-modsel-181.xml",
//        "tests/css3-modsel-182.xml",
//        "tests/css3-modsel-183.xml",
//        "tests/css3-modsel-184a.xml",
//        "tests/css3-modsel-184b.xml",
//        "tests/css3-modsel-184c.xml",
//        "tests/css3-modsel-184d.xml",
//        "tests/css3-modsel-184e.xml",
//        "tests/css3-modsel-184f.xml",
//        "tests/css3-modsel-d1.xml",
//        "tests/css3-modsel-d1b.xml",
//        "tests/css3-modsel-d2.xml",
//        "tests/css3-modsel-d3.xml",
//        "tests/css3-modsel-d4.xml"};
//    private static final String URL_BASE = "http://www.w3.org/Style/CSS/Test/CSS3/Selectors/current/xhtml/";
//    private static final Pattern STYLE_CONTENT_PATTERN = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);

    public W3CSelectorsTest(String name) {
        super(name);
    }

//    public void testMatchPattern() {
//        String input = "jfkdsfjksdjfkdsj<![CDATA[li,p { background-color : lime }]]>fjsdklfhjdskfhjdsfhkjsd";
//        Matcher m = STYLE_CONTENT_PATTERN.matcher(input);
//        assertTrue(m.find());
//    }
//
//    public void testGenerateTests() throws MalformedURLException, IOException {
//
//        StringBuilder code = new StringBuilder();
//        for (int i = 0; i < TESTS.length; i++) {
//            String test = TESTS[i];
//
//            System.out.print(String.format("Generating test %s (%s from %s) ... ", test, i, TESTS.length));
//
//            URLConnection con = new URL(URL_BASE + test).openConnection();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
//            StringBuilder sb = new StringBuilder();
//            Collection<String> lines = new LinkedList<String>();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                lines.add(escape(line));
//            }
//            reader.close();
//
//            for (Iterator<String> itr = lines.iterator(); itr.hasNext();) {
//                sb.append('"');
//                sb.append(itr.next());
//                sb.append('"');
//                sb.append("\n");
//                if (itr.hasNext()) {
//                    sb.append("+");
//                }
//            }
//
//            Matcher matcher = STYLE_CONTENT_PATTERN.matcher(sb);
//            if (matcher.find()) {
//                String css = matcher.group(1);
//                String methodName = test.replaceAll("[-/\\.]", "_");
//
//                code.append("public void ");
//                code.append(methodName);
//                code.append("() throws BadLocationException, ParseException {\n");
//                code.append("\tString code = \"");
//                code.append(css);
//                code.append("\";\n");
//                code.append("\tassertResultOK(TestUtil.parse(code));\n");
//                code.append("}\n");
//                code.append("\n");
//
//            } else {
//                System.out.println("Couldn't find pattern in the file " + test);
//                System.out.println(sb.toString());
//            }
//
//            System.out.println("ok");
//        }
//
//        System.out.println("===================================================================");
//        System.out.println(code);
//    }
//
//    private static String escape(String s) {
//        s = s.replace("\\", "\\\\");
//        s = s.replace("\"", "\\\"");
//        return s;
//    }

    public void tests_css3_modsel_1_xml() throws BadLocationException, ParseException {
        String code = "li,p { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_2_xml() throws BadLocationException, ParseException {
        String code = "address { background-color: lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_3_xml() throws BadLocationException, ParseException {
        String code = "* { color : lime }"
                + "ul, p { color : red }"
                + "*.t1 { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_3a_xml() throws BadLocationException, ParseException {
        String code = "* { color : lime }"
                + "ul, p { color : red }"
                + "*.t1 { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_4_xml() throws BadLocationException, ParseException {
        String code = "#foo { background-color : lime }"
                + "p { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_5_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red }"
                + "p[title] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_6_xml() throws BadLocationException, ParseException {
        String code = "address { background-color : red }"
                + "address[title=\"foo\"] { background-color : lime }"
                + "span[title=\"a\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_7_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red }"
                + "p[class~=\"b\"] { background-color : lime }"
                + "address { background-color : red }"
                + "address[title~=\"foo\"] { background-color : lime }"
                + "span[class~=\"b\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_7b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: lime; }"
                + "[title~=\"hello world\"] { background: red; }"
                + "/* Section 6.3.1: Represents the att attribute whose value is a"
                + "space-separated list of words, one of which is exactly \"val\". If this"
                + "selector is used, the words in the value must not contain spaces"
                + "(since they are separated by spaces). */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_8_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red }"
                + "p[lang|=\"en\"] { background-color : lime }"
                + "address { background-color : red }"
                + "address[lang=\"fi\"] { background-color : lime }"
                + "span[lang|=\"fr\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_9_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red }"
                + "p[title^=\"foo\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_10_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red }"
                + "p[title$=\"bar\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_11_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red }"
                + "p[title*=\"bar\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_13_xml() throws BadLocationException, ParseException {
        String code = "li { background-color : red }"
                + ".t1 { background-color : lime }"
                + "li.t2 { background-color : lime }"
                + ".t3 { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_14_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : red ; border : thick solid red ; padding : 1em }"
                + "p.t1 { background-color : lime }"
                + "p.t2 { border : thick solid green }"
                + ""
                + "div { background: green; color: white; }"
                + "div.teST { background: red; color: yellow; }"
                + "div.te { background: red; color: yellow; }"
                + "div.st { background: red; color: yellow; }"
                + "div.te.st { background: red; color: yellow; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_14b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: green; color: white; }"
                + ".t1.fail { background: red; color: yellow; }"
                + ".fail.t1 { background: red; color: yellow; }"
                + ".t2.fail { background: red; color: yellow; }"
                + ".fail.t2 { background: red; color: yellow; }"
                + "/* Note: This is a valid test even per CSS1, since in CSS1 those rules"
                + "         are invalid and should be dropped. */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_14c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: red; color: yellow; }"
                + "p.t1.t2 { background: green; color: white; }"
                + "div { background: green; color: white; }"
                + "div.t1 { background: red; color: yellow; }"
                + "address { background: red; color: yellow; }"
                + "address.t5.t5 { background: green; color: white; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_14d_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: green; color: white; }"
                + ".t1:not(.t2) { background: red; color: yellow; }"
                + ":not(.t2).t1 { background: red; color: yellow; }"
                + ".t2:not(.t1) { background: red; color: yellow; }"
                + ":not(.t1).t2 { background: red; color: yellow; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_14e_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: green; color: white; }"
                + "p:not(.t1):not(.t2) { background: red; color: yellow; }"
                + "div { background: red; color: yellow; }"
                + "div:not(.t1) { background: green; color: white; }"
                + "address { background: green; color: white; }"
                + "address:not(.t5):not(.t5) { background: red; color: yellow; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_15_xml() throws BadLocationException, ParseException {
        String code = "li { background-color : red }"
                + "#t1 { background-color : lime }"
                + "li#t2 { background-color : lime }"
                + "li#t3 { background-color : lime }"
                + "#t4 { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_15b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: green; color: white; }"
                + "#test#fail { background: red; color: yellow; }"
                + "#fail#test { background: red; color: yellow; }"
                + "#fail { background: red; color: yellow; }"
                + "div { background: red; color: yellow; }"
                + "#pass#pass { background: green; color: white; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_15c_xml() throws BadLocationException, ParseException {
        String code = ""
                + ".warning { color: navy; }"
                + "div { color: red; }"
                + "#Aone#Atwo, #Aone#Athree, #Atwo#Athree { color: green; }"
                + "p { color: green; }"
                + "#Bone#Btwo, #Bone#Bthree, #Btwo#Bthree { color: red; }"
                + "#Cone#Ctwo, #Cone#Cthree, #Ctwo#Cthree { color: red; }"
                + "#Done#Dtwo, #Done#Dthree, #Dtwo#Dthree { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_16_xml() throws BadLocationException, ParseException {
        String code = "p.test a { background-color : red }"
                + "p.test *:link { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_17_xml() throws BadLocationException, ParseException {
        String code = "p.test a { background-color : red }"
                + "p.test *:visited { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_18_xml() throws BadLocationException, ParseException {
        String code = "p:hover { background-color : lime }"
                + "a:hover { background-color : lime }"
                + ""
                + "tr:hover { background-color : green }"
                + "td:hover { background-color : lime }"
                + ""
                + "table { border-spacing: 5px; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_18a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: navy; }"
                + ""
                + ".a a:hover { background: green; color: white; }"
                + ""
                + ".b a:hover { background: red; color: yellow; }"
                + ".b a:link { background: green; color: white; }"
                + ""
                + ".c :link { background: green; color: white; }"
                + ".c :visited:hover { background: red; color: yellow; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_18b_xml() throws BadLocationException, ParseException {
        String code = "div:hover > p:first-child { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_18c_xml() throws BadLocationException, ParseException {
        String code = ""
                + ":link, :visited { color: navy; text-decoration: none; }"
                + ":link:hover span { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_19_xml() throws BadLocationException, ParseException {
        String code = "a:active { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_19b_xml() throws BadLocationException, ParseException {
        String code = "button:active { background: green; color: white; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_20_xml() throws BadLocationException, ParseException {
        String code = "a:focus { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_21_xml() throws BadLocationException, ParseException {
        String code = "p:target { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_21b_xml() throws BadLocationException, ParseException {
        String code = "p { background-color: lime; }"
                + "p:target { background-color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_21c_xml() throws BadLocationException, ParseException {
        String code = ":root { background-color: green; }"
                + ":target { background-color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_22_xml() throws BadLocationException, ParseException {
        String code = "ul > li { background-color : red }"
                + "li:lang(en-GB) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_23_xml() throws BadLocationException, ParseException {
        String code = "button { background-color : red }"
                + "input { background-color : red }"
                + "button:enabled { background-color : lime }"
                + "input:enabled { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_24_xml() throws BadLocationException, ParseException {
        String code = "button { background-color : red }"
                + "input { background-color : red }"
                + "button:disabled { background-color : lime }"
                + "input:disabled { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_25_xml() throws BadLocationException, ParseException {
        String code = "input, span { background-color : red }"
                + "input:checked, input:checked + span { background-color : lime}"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_27_xml() throws BadLocationException, ParseException {
        String code = "html { background-color : red }"
                + "*:root { background-color: lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_27a_xml() throws BadLocationException, ParseException {
        String code = ""
                + ":root:first-child { background-color: red; }"
                + ":root:last-child { background-color: red; }"
                + ":root:only-child { background-color: red; }"
                + ":root:nth-child(1) { background-color: red; }"
                + ":root:nth-child(n) { background-color: red; }"
                + ":root:nth-last-child(1) { background-color: red; }"
                + ":root:nth-last-child(n) { background-color: red; }"
                + ":root:first-of-type { background-color: red; }"
                + ":root:last-of-type { background-color: red; }"
                + ":root:only-of-type { background-color: red; }"
                + ":root:nth-of-type(1) { background-color: red; }"
                + ":root:nth-of-type(n) { background-color: red; }"
                + ":root:nth-last-of-type(1) { background-color: red; }"
                + ":root:nth-last-of-type(n) { background-color: red; }"
                + "p { color: green; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_27b_xml() throws BadLocationException, ParseException {
        String code = "* html { background-color: red; }"
                + "* :root { background-color: red; }"
                + "p { color: green; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_28_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "ul > li:nth-child(odd) { background-color : lime }"
                + "ol > li:nth-child(even) { background-color : lime }"
                + "table.t1 tr:nth-child(-n+4) { background-color : lime }"
                + "table.t2 td:nth-child(3n+1) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_28b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + "ul > li:nth-child(odd) { background-color : red }"
                + "ol > li:nth-child(even) { background-color : red }"
                + "table.t1 tr:nth-child(-n+4) { background-color : red }"
                + "table.t2 td:nth-child(3n+1) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_29_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "ul > li:nth-last-child(odd) { background-color : green }"
                + "ol > li:nth-last-child(even) { background-color : green }"
                + "table.t1 tr:nth-last-child(-n+4) { background-color : green }"
                + "table.t2 td:nth-last-child(3n+1) { background-color : green }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_29b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + "ul > li:nth-last-child(odd) { background-color : red }"
                + "ol > li:nth-last-child(even) { background-color : red }"
                + "table.t1 tr:nth-last-child(-n+4) { background-color : red }"
                + "table.t2 td:nth-last-child(3n+1) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_30_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "p:nth-of-type(3) { background-color : lime }"
                + "dl > :nth-of-type(3n+1) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_31_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "p:nth-last-of-type(3) { background-color : lime }"
                + "dl > :nth-last-of-type(3n+1) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_32_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + ".t1 td:first-child { background-color : lime }"
                + "p > *:first-child { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_33_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + ".t1 td:last-child { background-color : lime }"
                + "p > *:last-child { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_34_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "address { margin-bottom : 1em ; margin-left : 1em }"
                + "address:first-of-type { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_35_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "address { margin-bottom : 1em ; margin-left : 1em }"
                + "address:last-of-type { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_36_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "p:only-child { background-color : lime }"
                + "div.testText > div > p { margin-left : 1em }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_37_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + ".t1 :only-of-type { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_38_xml() throws BadLocationException, ParseException {
        String code = "p:first-line { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_39_xml() throws BadLocationException, ParseException {
        String code = "p:first-letter { font-size : xx-large ; background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_39a_xml() throws BadLocationException, ParseException {
        String code = "p:first-letter { color: lime; font-size: xx-large; }"
                + "p:before { color: red; content: 'T'; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_39b_xml() throws BadLocationException, ParseException {
        String code = "p::first-letter { font-size : xx-large ; background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_39c_xml() throws BadLocationException, ParseException {
        String code = "p::first-letter { color: lime; font-size: xx-large; }"
                + " p::before { color: red; content: 'T'; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_41_xml() throws BadLocationException, ParseException {
        String code = "p::before { background-color : lime ; content : \"GENERATED CONTENT \"}"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_41a_xml() throws BadLocationException, ParseException {
        String code = "p:before { background-color : lime ; content : \"GENERATED CONTENT \"}"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_42_xml() throws BadLocationException, ParseException {
        String code = "p::after { background-color : lime ; content : \"GENERATED CONTENT \"}"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_42a_xml() throws BadLocationException, ParseException {
        String code = "p:after { background-color : lime ; content : \"GENERATED CONTENT \"}"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_43_xml() throws BadLocationException, ParseException {
        String code = ".white { background-color: transparent ! important; }"
                + ".red { background-color: red; }"
                + "div.t1 p { background-color: lime; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_43b_xml() throws BadLocationException, ParseException {
        String code = ".white { background-color: transparent ! important; }"
                + ".green { background-color: lime; }"
                + "div.t1 p { background-color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_44_xml() throws BadLocationException, ParseException {
        String code = ".white { background-color: transparent ! important; }"
                + ".red { background-color: red; }"
                + "div > p.test { background-color: lime; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_44b_xml() throws BadLocationException, ParseException {
        String code = ".white { background-color: transparent ! important; }"
                + ".green { background-color: lime; }"
                + "div > p.test { background-color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_44c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  .fail > div { background: red; color: yellow; }"
                + "  .control { background: green; color: white; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_44d_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  #fail > div { background: red; }"
                + "  p { background: green; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_45_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "div.stub > p + p { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_45b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color: lime; }"
                + ".white { background-color: transparent ! important; }"
                + "div.stub > p + p { background-color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_45c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  .fail + div { background: red; }"
                + "  .control { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_46_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "div.stub > p ~ p { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_46b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + "div.stub > p ~ p { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_47_xml() throws BadLocationException, ParseException {
        String code = "div.stub span, div.stub address, div.stub *|q, div.stub *|r { background-color: red; }"
                + "address, *|q, *|r { display: block; margin: 1em; }"
                + "div.stub *:not(p) { background-color: lime; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_48_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > *|* { background-color : lime ; display : block ;"
                + "                 margin-bottom : 1em }"
                + "div.stub > *|*:not(*) { background-color : red }"
                + "/* yes, the rule just above selects nothing... That's the point */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_49_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > *|* { background-color : lime ; display : block ;"
                + "                 margin-bottom : 1em }"
                + "div.stub > *|*:not() { background-color : red }"
                + "/* yes, the rule just above selects nothing... That's the point */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_50_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "a|* { color : red ; display : block ; margin-bottom : 1em }"
                + "div.stub *|*:not([test]) { color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_51_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "div.stub > p {color : red }"
                + "div.stub > a|* { color : red ; display : block ; margin-bottom : 1em }"
                + "div.stub *|*:not([test=\"1\"]) { color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_52_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub p { color : red }"
                + "div.stub > a|*, div.stub > b|* { color : red ; display : block ; margin-bottom : 1em }"
                + "div.stub *|*:not([test~=\"foo\"]) { color : lime }"
                + "div.stub *|p:not([class~=\"foo\"]) { color : lime }"
                + "div.stub b|*[test~=\"foo2\"] { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_53_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub p { color : red }"
                + "div.stub > a|*, div.stub > b|* { color : red ; display : block ; margin-bottom : 1em }"
                + "div.stub *|*:not([test|=\"foo-bar\"]) { color : lime }"
                + "div.stub *|p:not([lang|=\"en-us\"]) { color : lime }"
                + "div.stub b|*[test|=\"foo2-bar\"] { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_54_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { color : red }"
                + "div.stub *:not([title^=\"si on\"]) { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_55_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { color : red }"
                + "div.stub *:not([title$=\"tait\"]) { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_56_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { color : red }"
                + "div.stub *:not([title*=\" on\"]) { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_57_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "div.stub *:not([a|title]) {background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_57b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "div.stub *:not([a|title]) {background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_59_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { color : red }"
                + "div.stub *:not(.foo) { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_60_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { color : red }"
                + "div.stub *:not(#foo) { color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_61_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { background-color : red }"
                + "div.stub *:not(:link) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_62_xml() throws BadLocationException, ParseException {
        String code = "div.stub > * { background-color : red }"
                + "div.stub *:not(:visited) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_63_xml() throws BadLocationException, ParseException {
        String code = "div.stub * { color: lime; text-decoration: none; }"
                + "div.stub > * > *:not(:hover) { color: black }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_64_xml() throws BadLocationException, ParseException {
        String code = "div.stub * { color : lime }"
                + "div.stub > * > *:not(:active) { color : black }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_65_xml() throws BadLocationException, ParseException {
        String code = "a:not(:focus) { background-color: transparent; }"
                + "a { background-color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_66_xml() throws BadLocationException, ParseException {
        String code = "p { background-color: navy; color: white; }"
                + "p:not(:target) { background-color: white; color: black; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_66b_xml() throws BadLocationException, ParseException {
        String code = "p { background-color: red; }"
                + "p:not(:target) { background-color: lime; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_67_xml() throws BadLocationException, ParseException {
        String code = "div.stub * { background-color : red  }"
                + "div.stub *:not(:lang(fr)) { background-color : green }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_68_xml() throws BadLocationException, ParseException {
        String code = "button { background-color : red }"
                + "input { background-color : red }"
                + "button:not(:enabled) { background-color : lime }"
                + "input:not(:enabled)  { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_69_xml() throws BadLocationException, ParseException {
        String code = "button { background-color : red }"
                + "input { background-color : red }"
                + "button:not(:disabled) { background-color : lime }"
                + "input:not(:disabled) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_70_xml() throws BadLocationException, ParseException {
        String code = "input, span { background-color : red }"
                + "input:not(:checked), input:not(:checked) + span { background-color : lime}";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_72_xml() throws BadLocationException, ParseException {
        String code = "p:not(:root) { background-color: lime; }"
                + "div * { background-color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_72b_xml() throws BadLocationException, ParseException {
        String code = "html:not(:root), test:not(:root) { background-color: red; }"
                + "p { background-color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_73_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "ul > li:not(:nth-child(odd)) { background-color : lime }"
                + "ol > li:not(:nth-child(even)) { background-color : lime }"
                + "table.t1 tr:not(:nth-child(-n+4)) { background-color : lime }"
                + "table.t2 td:not(:nth-child(3n+1)) { background-color : lime }"
                + "table.t1 td, table.t2 td { border : thin black solid }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_73b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important; }"
                + "ul > li:not(:nth-child(odd)) { background-color : red }"
                + "ol > li:not(:nth-child(even)) { background-color : red }"
                + "table.t1 tr:not(:nth-child(-n+4)) { background-color : red }"
                + "table.t2 td:not(:nth-child(3n+1)) { background-color : red }"
                + "table.t1 td, table.t2 td { border : thin black solid }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_74_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "ul > li:not(:nth-last-child(odd)) { background-color : lime }"
                + "ol > li:not(:nth-last-child(even)) { background-color : lime }"
                + "table.t1 tr:not(:nth-last-child(-n+4)) { background-color : lime }"
                + "table.t2 td:not(:nth-last-child(3n+1)) { background-color : lime }"
                + "table.t1 td, table.t2 td { border : thin black solid }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_74b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important; }"
                + "ul > li:not(:nth-last-child(odd)) { background-color : red }"
                + "ol > li:not(:nth-last-child(even)) { background-color : red }"
                + "table.t1 tr:not(:nth-last-child(-n+4)) { background-color : red }"
                + "table.t2 td:not(:nth-last-child(3n+1)) { background-color : red }"
                + "table.t1 td, table.t2 td { border : thin black solid }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_75_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "p:not(:nth-of-type(3)) { background-color : lime }"
                + "dl > *:not(:nth-of-type(3n+1)) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_75b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + "p:not(:nth-of-type(3)) { background-color : red }"
                + "dl > *:not(:nth-of-type(3n+1)) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_76_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "p:not(:nth-last-of-type(3)) { background-color : lime }"
                + "dl > *:not(:nth-last-of-type(3n+1)) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_76b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + "p:not(:nth-last-of-type(3)) { background-color : red }"
                + "dl > *:not(:nth-last-of-type(3n+1)) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_77_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + ".t1 td:not(:first-child) { background-color : lime }"
                + "p > *:not(:first-child) { background-color : lime }"
                + "table.t1 td { border : thin black solid }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_77b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + ".t1 td:not(:first-child) { background-color : red }"
                + "p > *:not(:first-child) { background-color : red }"
                + "table.t1 td { border : thin black solid }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_78_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + ".t1 td:not(:last-child) { background-color : lime }"
                + "p > *:not(:last-child) { background-color : lime }"
                + "table.t1 td { border : thin black solid }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_78b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + ".t1 td:not(:last-child) { background-color : red }"
                + "p > *:not(:last-child) { background-color : red }"
                + "table.t1 td { border : thin black solid }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_79_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "address { margin-bottom : 1em ; margin-left : 1em }"
                + "address:not(:first-of-type) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_80_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "address { margin-bottom : 1em ; margin-left : 1em }"
                + "address:not(:last-of-type) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_81_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + "p:not(:only-child) { background-color : lime }"
                + "div.testText > div > p { margin-left : 1em }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_81b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + "p:not(:only-child) { background-color : lime }"
                + "div.testText > div > p { margin-left : 1em }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_82_xml() throws BadLocationException, ParseException {
        String code = ".red { background-color : red }"
                + ".t1 *:not(:only-of-type) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_82b_xml() throws BadLocationException, ParseException {
        String code = ".green { background-color : lime ! important }"
                + ".t1 *:not(:only-of-type) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_83_xml() throws BadLocationException, ParseException {
        String code = "p { background-color : lime }"
                + "p:not(:not(p)) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_86_xml() throws BadLocationException, ParseException {
        String code = "p { color: red; }"
                + "blockquote > div p { color: green; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_87_xml() throws BadLocationException, ParseException {
        String code = "p { color: red; }"
                + "blockquote + div ~ p { color: green; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_87b_xml() throws BadLocationException, ParseException {
        String code = "p { color: green ! important; }"
                + "blockquote + div ~ p { color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_88_xml() throws BadLocationException, ParseException {
        String code = "p { color: red; }"
                + "blockquote + div p { color: green; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_88b_xml() throws BadLocationException, ParseException {
        String code = "p { color: green ! important; }"
                + "blockquote + div p { color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_89_xml() throws BadLocationException, ParseException {
        String code = "p { color: red; }"
                + "blockquote div > p { color: green; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_90_xml() throws BadLocationException, ParseException {
        String code = "p { color: red; }"
                + "blockquote ~ div + p { color: green; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_90b_xml() throws BadLocationException, ParseException {
        String code = "p { color: green ! important; }"
                + "blockquote ~ div + p { color: red; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_91_xml() throws BadLocationException, ParseException {
        String code = "@namespace test url(http://www.example.org/a);"
                + "testa { background-color : red }"
                + "test|testa { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_92_xml() throws BadLocationException, ParseException {
        String code = "@namespace test url(http://www.example.org/b);"
                + "div.myTest * { background-color : red }"
                + "div.myTest *|testA { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_93_xml() throws BadLocationException, ParseException {
        String code = "@namespace test url(http://www.example.org/b);"
                + "*|testA { background-color : red }"
                + "|testA {background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_94_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "p, q { display : block ; margin-bottom : 1em }"
                + "b|* { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_94b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "p, q { display : block ; margin-bottom : 1em }"
                + "b|* { background-color : red }"
                + "[test] { background-color: lime; }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_95_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.test * { background-color : red ; display : block ; margin-bottom : 1em }"
                + "div.test *|* { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_96_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.green * { background-color : red }"
                + "div.test * { display : block ; margin-bottom : 1em }"
                + "div.test |* { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_96b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.green * { background-color : lime ! important }"
                + "div.test * { display : block ; margin-bottom : 1em }"
                + "div.test |* { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_97_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : red }"
                + "*[a|title] {background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_97b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : lime ! important }"
                + "*[a|title] {background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_98_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|t { background-color : red }"
                + "*[a|title=\"foo\"] {background-color : lime }"
                + "*[a|title=footwo] {background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_98b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : lime ! important }"
                + "*[a|title=\"foo\"] {background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_99_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[a|foo~=\"bar\"], *|*[|class~=\"bar\"] { background-color : lime }"
                + "*|*[html|class~=\"bar\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_99b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[a|foo~=\"bar\"], *|*[html|class~=\"bar\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_100_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[a|foo|=\"bar\"], *|*[html|lang|=\"en\"] { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_100b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[a|foo|=\"bar\"], *|*[html|lang|=\"en\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_101_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[a|title^=\"si on\"], *|*[title^=\"si on\"] { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_101b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[a|title^=\"si on\"], *|*[title^=\"si on\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_102_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[a|title$=\"tait\"], p[|title$=\"tait\"] { background-color : lime }"
                + "*|*[|title$=\"tait\"], *|*[html|title$=\"tait\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_102b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[a|title$=\"tait\"], *|*[html|title$=\"tait\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_103_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[a|title*=\"hanta\"], p[|title*=\"hanta\"] { background-color : lime }"
                + "*|*[|title*=\"hanta\"], *|*[html|title*=\"hanta\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_103b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[a|title*=\"hanta\"], *|*[html|title*=\"hanta\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_104_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "*|*[*|title] { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_104b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "*|*[*|title] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_105_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "*|*[*|title=\"si on chantait\"] { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_105b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "*|*[*|title=\"si on chantait\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_106_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "*|*[*|class~=\"deux\"], *|*[*|foo~=\"deux\"] { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_106b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "*|*[*|class~=\"deux\"], *|*[*|foo~=\"deux\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_107_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[*|lang|=\"en\"], *|*[a|foo|=\"un-d\"] { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_107b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[*|lang|=\"en\"], *|*[a|foo|=\"un-d\"] { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_108_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "*|*[*|title^=\"si on\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_108b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "*|*[*|title^=\"si on\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_109_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "*|*[*|title$=\"tait\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_109b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "*|*[*|title$=\"tait\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_110_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "*|*[*|title*=\"on ch\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_110b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "*|*[*|title*=\"on ch\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_111_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[|title] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_111b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[|title] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_112_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[|title=\"si on chantait\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_112b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[|title=\"si on chantait\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_113_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|p, *|q { background-color : red }"
                + "*|*[|class~=\"foo\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_113b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|p, *|q { background-color : lime ! important }"
                + "*|*[|class~=\"foo\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_114_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|p, *|q { background-color : red }"
                + "*|*[|lang|=\"foo-bar\"], *|*[|myattr|=\"tat-tut\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_114b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|p, *|q { background-color : lime ! important }"
                + "*|*[|lang|=\"foo-bar\"], *|*[|myattr|=\"tat-tut\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_115_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[|title^=\"si on\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_115b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[|title^=\"si on\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_116_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[|title$=\"tait\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_116b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[|title$=\"tait\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_117_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : red }"
                + "*|*[|title*=\"on ch\"] { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_117b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p, *|r { background-color : lime ! important }"
                + "*|*[|title*=\"on ch\"] { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_118_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|l { display : block ; margin-bottom : 1em }"
                + "div.test * { background-color : red }"
                + "div.test *:not(a|p) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_119_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.test *:not(*|div) { display : block ; margin-bottom : 1em ;"
                + "                            background-color : red }"
                + "div.test > *:not(*|p):not(*|div) { background-color : lime }"
                + "div.stub > *:not(*|div) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_120_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > * { display : block ; margin-bottom : 1em ;"
                + "                            background-color : red }"
                + "div.stub > *:not(|p) { background-color : lime }"
                + "div.stub > *|l > *:not(|p) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_121_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > *|* { color : red ; display : block ;"
                + "                 margin-bottom : 1em }"
                + "div.stub > *|*:not(a|*) { color : green  }"
                + "div.stub v { color : green }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_122_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > *|* { background-color : lime ; display : block ;"
                + "                 margin-bottom : 1em }"
                + "div.stub > *|*:not(*|*) { background-color : red }"
                + "/* yes, the rule just above selects nothing... That's the point */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_123_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > *|* { color : red ; display : block ;"
                + "                 margin-bottom : 1em }"
                + "div.stub > *|*:not(|*) { color : green }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_123b_xml() throws BadLocationException, ParseException {
        String code = "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "div.stub > *|* { color : green ; display : block ;"
                + "                 margin-bottom : 1em }"
                + "div.stub > *|*:not(|*) { color : red ! important }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_124_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : red }"
                + "div.stub *:not([a|title=\"foo\"]) {background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_124b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p, *|r, *|s { background-color : lime ! important }"
                + "div.stub *:not([a|title=\"foo\"]) {background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_125_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : red }"
                + "div.stub *|*:not([a|foo~=\"bar\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_125b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : lime ! important }"
                + "div.stub *|*:not([a|foo~=\"bar\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_126_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : red }"
                + "div.stub *|*:not([a|foo|=\"bar\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_126b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : lime ! important }"
                + "div.stub *|*:not([a|foo|=\"bar\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_127_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : red }"
                + "div.stub *|*:not([a|title^=\"si on\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_127b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : lime ! important }"
                + "div.stub *|*:not([a|title^=\"si on\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_128_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : red }"
                + "div.stub *|*:not([a|title$=\"tait\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_128b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : lime ! important }"
                + "div.stub *|*:not([a|title$=\"tait\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_129_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : red }"
                + "div.stub *|*:not([a|title*=\"hanta\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_129b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q, *|s { background-color : lime ! important }"
                + "div.stub *|*:not([a|title*=\"hanta\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_130_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : red }"
                + "div.stub *|*:not([*|title]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_130b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : lime ! important }"
                + "div.stub *|*:not([*|title]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_131_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : red }"
                + "div.stub *|*:not([*|title=\"si on chantait\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_131b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : lime ! important }"
                + "div.stub *|*:not([*|title=\"si on chantait\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_132_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p.deu, *|q { background-color : red }"
                + "div.stub html|*:not([*|class~=\"deux\"]),"
                + "   div.stub *|*:not(html|*):not([*|foo~=\"deux\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_132b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p.deu, *|q { background-color : lime ! important }"
                + "div.stub html|*:not([*|class~=\"deux\"]),"
                + "   div.stub *|*:not(html|*):not([*|foo~=\"deux\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_133_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p.foo, *|q, *|s { background-color : red }"
                + "div.stub html|*:not([*|lang|=\"en\"]),"
                + "  div.stub *|*:not(html|*):not([a|foo|=\"un-d\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_133b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s { display : block ; margin-bottom : 1em }"
                + "*|p.foo, *|q, *|s { background-color : lime ! important }"
                + "div.stub html|*:not([*|lang|=\"en\"]),"
                + "  div.stub *|*:not(html|*):not([a|foo|=\"un-d\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_134_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p.red, *|q, *|t { background-color : red }"
                + "div.stub *|*:not([*|title^=\"si on\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_134b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p.red, *|q, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([*|title^=\"si on\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_135_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p.red, *|q, *|t { background-color : red }"
                + "div.stub *|*:not([*|title$=\"tait\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_135b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p.red, *|q, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([*|title$=\"tait\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_136_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p.red, *|q, *|t { background-color : red }"
                + "div.stub *|*:not([*|title*=\"on ch\"]) { background-color : lime }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_136b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|p.red, *|q, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([*|title*=\"on ch\"]) { background-color : red }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_137_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : red }"
                + "div.stub *|*:not([|title]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_137b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|q { background-color : lime ! important }"
                + "div.stub *|*:not([|title]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_138_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : red }"
                + "div.stub *|*:not([|title=\"si on chantait\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_138b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([|title=\"si on chantait\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_139_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|address, *|r { background-color : red }"
                + "div.stub *|*:not([|class~=\"foo\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_139b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|address, *|r { background-color : lime ! important }"
                + "div.stub *|*:not([|class~=\"foo\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_140_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|address, *|r { background-color : red }"
                + "div.stub *|*:not([|lang|=\"foo-bar\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_140b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|address, *|q, *|r { display : block ; margin-bottom : 1em }"
                + "*|address, *|r { background-color : lime ! important }"
                + "div.stub *|*:not([|lang|=\"foo-bar\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_141_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : red }"
                + "div.stub *|*:not([|title^=\"si on\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_141b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([|title^=\"si on\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_142_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : red }"
                + "div.stub *|*:not([|title$=\"tait\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_142b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([|title$=\"tait\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_143_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : red }"
                + "div.stub *|*:not([|title*=\"on ch\"]) { background-color : lime }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_143b_xml() throws BadLocationException, ParseException {
        String code = "@namespace a url(http://www.example.org/a);"
                + "@namespace b url(http://www.example.org/b);"
                + "@namespace html url(http://www.w3.org/1999/xhtml);"
                + "*|p, *|q, *|r, *|s, *|t{ display : block ; margin-bottom : 1em }"
                + "*|q, *|s, *|t { background-color : lime ! important }"
                + "div.stub *|*:not([|title*=\"on ch\"]) { background-color : red }";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_144_xml() throws BadLocationException, ParseException {
        String code = "div :not(:enabled):not(:disabled) { background: lime; }"
                + "p { background : red;}";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_145a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  line { display: block; }"
                + "  [type~=odd] { background: red; }"
                + "  line:nth-of-type(odd) { background: lime; }"
                + "  [hidden] { display: none; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_145b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  line { display: block; }"
                + "  [type~=odd] { background: lime ! important; }"
                + "  line:nth-of-type(odd) { background: red; }"
                + "  [hidden] { display: none; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_146a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  line { display: block; }"
                + "  [type~=match] { background: red; }"
                + "  line:nth-child(3n-1) { background: lime; }"
                + "  [hidden] { display: none; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_146b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  line { display: block; }"
                + "  [type~=match] { background: lime ! important; }"
                + "  line:nth-child(3n-1) { background: red; }"
                + "  [hidden] { display: none; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_147a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  line { display: block; }"
                + "  [type~=match] { background: red; }"
                + "  line:nth-last-of-type(3n-1) { background: lime; }"
                + "  [hidden] { visibility: collapse; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_147b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  line { display: block; }"
                + "  [type~=match] { background: lime ! important; }"
                + "  line:nth-last-of-type(3n-1) { background: red; }"
                + "  [hidden] { visibility: collapse; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_148_xml() throws BadLocationException, ParseException {
        String code = ""
                + " p { background: lime; }"
                + " p:empty { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_149_xml() throws BadLocationException, ParseException {
        String code = ""
                + " address:empty { background: lime; }"
                + " address { background: red; margin: 0; height: 1em; }"
                + " .text { margin: -1em 0 0 0; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_149b_xml() throws BadLocationException, ParseException {
        String code = ""
                + " address:empty { background: lime; }"
                + " address { background: red; margin: 0; height: 1em; }"
                + " .text { margin: -1em 0 0 0; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_150_xml() throws BadLocationException, ParseException {
        String code = ""
                + " address:empty { background: lime; }"
                + " address { background: red; margin: 0; height: 1em; }"
                + " .text { margin: -1em 0 0 0; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_151_xml() throws BadLocationException, ParseException {
        String code = ""
                + " address { background: lime; margin: 0; height: 1em; }"
                + " address:empty { background: red; }"
                + " .text { margin: -1em 0 0 0; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_152_xml() throws BadLocationException, ParseException {
        String code = ""
                + " address { background: lime; margin: 0; height: 1em; }"
                + " address:empty { background: red; }"
                + " .text { margin: -1em 0 0 0; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_153_xml() throws BadLocationException, ParseException {
        String code = ""
                + " address { background: red; margin: 0; height: 1em; display: block; }"
                + " address:empty { background: lime; }"
                + " .text { margin: -1em 0 0 0; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_154_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  p, { background: red; }" //weird, looks like this should not pass
                + "";
        assertResult(TestUtil.parse(code), 1);
    }

    public void tests_css3_modsel_155_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  .5cm { background: red; }"
                + "";
        assertResult(TestUtil.parse(code), 1);
    }

    public void tests_css3_modsel_155a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  .\5cm { background: red; }"
                + "";
        assertResult(TestUtil.parse(code), 3);
    }

    public void tests_css3_modsel_155b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  .two\\ words { background: red; }"
                + ""
                + "  /* the \".\" and \"~=\" forms match on a space separated list of words."
                + "  In such a list, a word containing a space can never match, since it"
                + "  would by definition be two words. */"
                + ""
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_155c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  .one.word { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_155d_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  .one\\.word { background: lime; }"
                + "  p { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_156_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  foo & address, p { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_156b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  foo & address, p { background: red; }"
                + "  p { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_156c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  foo & address, p { background: red ! important; }"
                + "  p { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_157_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  [*=test] { background: red; }"
                + "";
        assertResult(TestUtil.parse(code), 1);
    }

    public void tests_css3_modsel_158_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  [*|*=test] { background: red; }"
                + "";
        assertResult(TestUtil.parse(code), 1);
    }

    public void tests_css3_modsel_159_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  ::selection { background: lime; }"
                + "  :selection { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_160_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  p:subject { background: red; } /* this is not valid CSS, and if UAs"
                + "  implemented the experimental :subject pseudo-class they should have"
                + "  used the :-vnd-ident syntax. */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_161_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { background: lime; }"
                + "  p   * { background: lime; }"
                + "  p > * { background: lime; }"
                + "  p + * { background: lime; }"
                + "  p ~ * { background: lime; }"
                + ""
                + "  /* let's try some pseudos that are not valid CSS but are likely to"
                + "  be implemented as extensions in some UAs. These should not be"
                + "  recognised, as UAs implementing such extensions should use the"
                + "  :-vnd-ident syntax. */"
                + ""
                + "  :canvas { background: red; }"
                + "  :viewport { background: red; }"
                + "  :window { background: red; }"
                + "  :menu { background: red; }"
                + "  :table { background: red; }"
                + "  :select { background: red; }"
                + "  ::canvas { background: red; }"
                + "  ::viewport { background: red; }"
                + "  ::window { background: red; }"
                + "  ::menu { background: red; }"
                + "  ::table { background: red; }"
                + "  ::select { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_166_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p:first-letter { background-color: red; }"
                + "  p::first-letter { background-color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_166a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p::first-letter { background-color: red; }"
                + "  p:first-letter { background-color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_167_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p:first-line { background-color: red; }"
                + "  p::first-line { background-color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_167a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p::first-line { background-color: red; }"
                + "  p:first-line { background-color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_168_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  span:before { background-color: red; content: 'FAILED'; }"
                + "  span::before { background-color: lime; content: 'PASSED'; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_168a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  span::before { background-color: red; content: 'FAILED'; }"
                + "  span:before { background-color: lime; content: 'PASSED'; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_169_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  span:after { background-color: red; content: 'FAILED'; }"
                + "  span::after { background-color: lime; content: 'PASSED'; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_169a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  span::after { background-color: red; content: 'FAILED'; }"
                + "  span:after { background-color: lime; content: 'PASSED'; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_170_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  span { color: red; }"
                + "  span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span, span { color: green } /* 2049 */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_170a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  .span { color: red; }"
                + "  .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span, .span { color: green } /* 2049 */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_170b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  .span { color: red; }"
                + "  .span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span.span { color: green } /* 2049 */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_170c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p.span { color: red; }"
                + "  p:not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span):not(.span) { color: green } /* 2049 */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_170d_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { color: red; }"
                + "  p:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child:first-child { color: green } /* 2049 */"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_171_xml() throws BadLocationException, ParseException {
        String code = ""
                + " p { color: green; }"
                + " .fail { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_172a_xml() throws BadLocationException, ParseException {
        String code = ""
                + " tests, tests * { display: block; color: green; }"
                + " testA[|attribute] { color: red; }"
                + " testB[|attribute=\"fail\"] { color: red; }"
                + " testC[|attribute~=\"fail\"] { color: red; }"
                + " testD[|attribute^=\"fail\"] { color: red; }"
                + " testE[|attribute*=\"fail\"] { color: red; }"
                + " testF[|attribute$=\"fail\"] { color: red; }"
                + " testG[|attribute|=\"fail\"] { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_172b_xml() throws BadLocationException, ParseException {
        String code = ""
                + " @namespace url(http://css.example.net/);"
                + " tests, tests * { display: block; color: green; }"
                + " testA[|attribute] { color: red; }"
                + " testB[|attribute=\"fail\"] { color: red; }"
                + " testC[|attribute~=\"fail\"] { color: red; }"
                + " testD[|attribute^=\"fail\"] { color: red; }"
                + " testE[|attribute*=\"fail\"] { color: red; }"
                + " testF[|attribute$=\"fail\"] { color: red; }"
                + " testG[|attribute|=\"fail\"] { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_173a_xml() throws BadLocationException, ParseException {
        String code = ""
                + " tests, tests * { display: block; color: red; }"
                + " testA[*|attribute] { color: green; }"
                + " testB[*|attribute=\"pass\"] { color: green; }"
                + " testC[*|attribute~=\"pass\"] { color: green; }"
                + " testD[*|attribute^=\"pass\"] { color: green; }"
                + " testE[*|attribute*=\"pass\"] { color: green; }"
                + " testF[*|attribute$=\"pass\"] { color: green; }"
                + " testG[*|attribute|=\"pass\"] { color: green; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_173b_xml() throws BadLocationException, ParseException {
        String code = ""
                + " tests, tests * { display: block; color: red; }"
                + " testA[*|attribute] { color: green; }"
                + " testB[*|attribute=\"pass\"] { color: green; }"
                + " testC[*|attribute~=\"pass\"] { color: green; }"
                + " testD[*|attribute^=\"pass\"] { color: green; }"
                + " testE[*|attribute*=\"pass\"] { color: green; }"
                + " testF[*|attribute$=\"pass\"] { color: green; }"
                + " testG[*|attribute|=\"pass\"] { color: green; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_174a_xml() throws BadLocationException, ParseException {
        String code = ""
                + " tests, tests * { display: block; color: red; }"
                + " testA[*|attribute=\"pass\"] { color: green; }"
                + " testB[*|attribute=\"pass\"] { color: green; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_174b_xml() throws BadLocationException, ParseException {
        String code = ""
                + " tests, tests * { display: block; color: green }"
                + " testA:not([*|attribute=\"pass\"]) { color: red; }"
                + " testB:not([*|attribute=\"pass\"]) { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_175a_xml() throws BadLocationException, ParseException {
        String code = ""
                + " p { color: green; }"
                + " .13 { color: red; }"
                + "";
        assertResult(TestUtil.parse(code), 1);
    }

    public void tests_css3_modsel_175b_xml() throws BadLocationException, ParseException {
        String code = ""
                + " p { color: green; }"
                + " .\13 { color: red; }"
                + "";
        assertResult(TestUtil.parse(code), 2);
    }

    public void tests_css3_modsel_175c_xml() throws BadLocationException, ParseException {
        String code = ""
                + " p { color: red; }"
                + " .\31 \33 { color: green; }"
                + "";
        assertResult(TestUtil.parse(code), 4);
    }

    public void tests_css3_modsel_176_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { background: red; color: yellow; }"
                + "p:not(#other).class:not(.fail).test#id#id { background: green; color: white; }"
                + "div { background: green; color: white; }"
                + "div:not(#theid).class:not(.fail).test#theid#theid { background: red; color: yellow; }"
                + "div:not(#other).notclass:not(.fail).test#theid#theid { background: red; color: yellow; }"
                + "div:not(#other).class:not(.test).test#theid#theid { background: red; color: yellow; }"
                + "div:not(#other).class:not(.fail).nottest#theid#theid { background: red; color: yellow; }"
                + "div:not(#other).class:not(.fail).nottest#theid#other { background: red; color: yellow; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_177a_xml() throws BadLocationException, ParseException {
        String code = ""
                + " p:selection { color: yellow; background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_177b_xml() throws BadLocationException, ParseException {
        String code = ""
                + " div { color: green; }"
                + " p::first-child { color: yellow; background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_178_xml() throws BadLocationException, ParseException {
        String code = ""
                + " div { color: green; }"
                + " p:not(:first-line) { color: yellow; background: red; }"
                + " p:not(:after) { color: yellow; background: red; content: ' THIS TEST HAS FAILED! '; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_179_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { color: green; }"
                + "  span:first-line { background: red; color: yellow; font-size: 4em; }"
                + "  span::first-line { background: red; color: yellow; font-size: 4em; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_179a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { color: green; }"
                + "  p:first-line { background: red; color: yellow; font-size: 4em; }"
                + "  p::first-line { background: red; color: yellow; font-size: 4em; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_180a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "  p { color: green; }"
                + "  p:first-letter { background: red; color: yellow; font-size: 4em; }"
                + "  p::first-letter { background: red; color: yellow; font-size: 4em; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_181_xml() throws BadLocationException, ParseException {
        String code = ""
                + " .cs { color: green; }"
                + " .cs P { background: red; color: yellow; }"
                + " .cs .a { background: red; color: yellow; }"
                + " .cs .span1 span { background: red; color: yellow; }"
                + " .cs .span2 { color: red; }"
                + " .cs .span2 SPAN { color: green; }"
                + " .cs .span2 span { background: red; color: yellow; }"
                + " .ci { color: red; }"
                + " .ci P { background: green; color: white; }"
                + " .ci .a { background: green; color: white; }"
                + " .ci .span1 span { background: green; color: white; }"
                + " .ci .span2 SPAN { background: green; color: white; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_182_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: green; }"
                + "foo\\:bar { background: red; color: yellow; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_183_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: green; }"
                + "..test { background: red; color: yellow; }"
                + ".foo..quux { background: red; color: yellow; }"
                + ".bar. { background: red; color: yellow; }"
                + "";
        assertResult(TestUtil.parse(code), 1);
    }

    public void tests_css3_modsel_184a_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: lime; }"
                + "p[class$=\"\"] { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_184b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: lime; }"
                + "p[class^=\"\"] { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_184c_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: lime; }"
                + "p[class*=\"\"] { color: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_184d_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: red; }"
                + "p:not([class$=\"\"]) { color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_184e_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: red; }"
                + "p:not([class^=\"\"]) { color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_184f_xml() throws BadLocationException, ParseException {
        String code = ""
                + "p { color: red; }"
                + "p:not([class*=\"\"]) { color: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_d1_xml() throws BadLocationException, ParseException {
        String code = ""
                + "   #test { background: red; display: block; padding: 1em; }"
                + "   #test:not(:empty) { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_d1b_xml() throws BadLocationException, ParseException {
        String code = ""
                + "   #test1 { background: red; display: block; padding: 1em; margin: 1em; }"
                + "   #test1:empty { background: lime; }"
                + "   #test2 { background: lime; display: block; padding: 1em; margin: 1em; }"
                + "   #test2:empty { background: red; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_d2_xml() throws BadLocationException, ParseException {
        String code = ""
                + "   #test { background: red; display: block; padding: 1em; }"
                + "   #stub ~ div div + div > div { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_d3_xml() throws BadLocationException, ParseException {
        String code = ""
                + "   [test] { background: red; display: block; padding: 1em; }"
                + "   stub ~ [|attribute^=start]:not([|attribute~=mid])[|attribute*=dle][|attribute$=end] ~ t { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }

    public void tests_css3_modsel_d4_xml() throws BadLocationException, ParseException {
        String code = ""
                + "   #two:first-child { background: red; }"
                + "   #three:last-child { background: lime; }"
                + "";
        assertResultOK(TestUtil.parse(code));
    }
}