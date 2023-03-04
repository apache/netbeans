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
package org.netbeans.modules.java.editor.javadoc;

import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.java.editor.base.javadoc.JavadocTestSupport;

/**
 *
 * @author mjayan
 */
public class JavaDocCompletionTaskTest extends JavadocTestSupport {

    public JavaDocCompletionTaskTest(String name) {
        super(name);
    }

    public void testMarkupTagHint() throws Exception {
        String code = "System.out.println(arg); //@";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        List<String> inlineAttr = new ArrayList() {
            {
                add("highlight");
                add("replace");
                add("link");
                add("start");
                add("end");
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testMarkupTagHintNoMatch() throws Exception {
        String code = "System.out.println(arg); //@qwerty ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        assertEquals(list, listStr);
    }

    public void testHighlightTagHint() throws Exception {
        String code = "System.out.println(arg); //@highlight ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        String value = " = \"<value>\"";
        List<String> inlineAttr = new ArrayList() {
            {
                add("substring" + value);
                add("regex" + value);
                add("region" + value);
                add("type" + value);
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testReplaceTagHint() throws Exception {
        String code = "System.out.println(arg); //@replace ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        String value = " = \"<value>\"";
        List<String> inlineAttr = new ArrayList() {
            {
                add("substring" + value);
                add("regex" + value);
                add("region" + value);
                add("replacement" + value);
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testLinkTagHint() throws Exception {
        String code = "System.out.println(arg); //@link ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        String value = " = \"<value>\"";
        List<String> inlineAttr = new ArrayList() {
            {
                add("substring" + value);
                add("regex" + value);
                add("region" + value);
                add("target" + value);
                add("type" + value);
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testStartTagHint() throws Exception {
        String code = "System.out.println(arg); //@start ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        String value = " = \"<value>\"";
        List<String> inlineAttr = new ArrayList() {
            {
                add("region" + value);
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testEndTagHint() throws Exception {
        String code = "System.out.println(arg); //@end ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        String value = " = \"<value>\"";
        List<String> inlineAttr = new ArrayList() {
            {
                add("region" + value);
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testMultipleTags() throws Exception {
        String code = "System.out.println(arg); //@highlight region @";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        List<String> inlineAttr = new ArrayList() {
            {
                add("highlight");
                add("replace");
                add("link");
                add("start");
                add("end");
            }
        };
        assertEquals(listStr, inlineAttr);
    }

    public void testMultipleAttr() throws Exception {
        String code = "System.out.println(arg); //@highlight region=\"<value>\" ";
        JavadocCompletionTask task = JavadocCompletionTask.create(0, new JavadocCompletionItem.Factory(), false, null);
        task.insideInlineSnippet(code);
        List<JavadocCompletionItem> list = task.getResults();
        assertNotNull(list);
        List<String> listStr = new ArrayList<>();
        for (JavadocCompletionItem item : list) {
            listStr.add(item.getSortText().toString());
        }
        String value = " = \"<value>\"";
        List<String> inlineAttr = new ArrayList() {
            {
                add("substring" + value);
                add("regex" + value);
                add("region" + value);
                add("type" + value);
            }
        };
        assertEquals(listStr, inlineAttr);
    }
}
