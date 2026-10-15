/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.htmlui;

import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * @author Jaroslav Tulach
 */
public class HTMLViewProcessorTest {
    @Test public void needActionId() throws Exception {
        String html = "<html><body>"
                + "</body></html>";
        String code = "package x.y.z;\n"
                + "import org.netbeans.api.htmlui.OpenHTMLRegistration;\n"
                + "public class X {\n"
                + "  @OpenHTMLRegistration(url=\"empty.html\", displayName=\"X\")\n"
                + "  public static void someMethod() {\n"
                + "  }\n"
                + "}\n";

        Compile c = Compile.create("empty.html", html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("ActionID")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about ActionID:" + msgs);
        }
    }
    
    @Test public void failIfHTMLPageDoesNotExist() throws Exception {
        String html = "<html><body>"
                + "</body></html>";
        String code = "package x.y.z;\n"
                + "import org.netbeans.api.htmlui.OpenHTMLRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "class X {\n"
                + "  @ActionID(category=\"test\", id=\"test.id.at.x\")\n"
                + "  @OpenHTMLRegistration(url=\"does-not-exist.html\", displayName=\"X\")\n"
                + "  public static void someMethod() {\n"
                + "  }\n"
                + "}\n";

        Compile c = Compile.create("different.html", html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Cannot find resource")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about Cannot find resource:" + msgs);
        }
    }

    @Test public void methodIsNotStatic() throws Exception {
        String html = "<html><body>"
                + "</body></html>";
        String code = "package x.y.z;\n"
                + "import org.netbeans.api.htmlui.OpenHTMLRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "public final class X {\n"
                + "  @ActionID(category=\"test\", id=\"test.id.at.x\")\n"
                + "  @OpenHTMLRegistration(url=\"page.html\", displayName=\"X\")\n"
                + "  public void someMethod() {\n"
                + "  }\n"
                + "}\n";

        Compile c = Compile.create("page.html", html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("needs to be static")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about static:" + msgs);
        }
    }

    @Test public void methodNeedsToBePublic() throws Exception {
        String html = "<html><body>"
                + "</body></html>";
        String code = "package x.y.z;\n"
                + "import org.netbeans.api.htmlui.OpenHTMLRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "public class X {\n"
                + "  @ActionID(category=\"test\", id=\"test.id.at.x\")\n"
                + "  @OpenHTMLRegistration(url=\"page.html\", displayName=\"X\")\n"
                + "  static void someMethod() {\n"
                + "  }\n"
                + "}\n";

        Compile c = Compile.create("page.html", html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("needs to be public")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about public:" + msgs);
        }
    }

    @Test public void methodNeedsToBeInPublicClass() throws Exception {
        String html = "<html><body>"
                + "</body></html>";
        String code = "package x.y.z;\n"
                + "import org.netbeans.api.htmlui.OpenHTMLRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "final class X {\n"
                + "  @ActionID(category=\"test\", id=\"test.id.at.x\")\n"
                + "  @OpenHTMLRegistration(url=\"page.html\", displayName=\"X\")\n"
                + "  public static void someMethod() {\n"
                + "  }\n"
                + "}\n";

        Compile c = Compile.create("page.html", html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("needs to be public")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about public:" + msgs);
        }
    }

    @Test public void methodWithNoParamsIsOK() throws Exception {
        String html = "<html><body>"
                + "</body></html>";
        String code = "package x.y.z;\n"
                + "import org.netbeans.api.htmlui.OpenHTMLRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "public class X {\n"
                + "  @ActionID(category=\"test\", id=\"test.id.at.x\")\n"
                + "  @OpenHTMLRegistration(url=\"page.html\", displayName=\"X\")\n"
                + "  public static void someMethod(int x) {\n"
                + "  }\n"
                + "}\n";

        Compile c = Compile.create("page.html", html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("no arguments")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about arguments:" + msgs);
        }
    }
}
