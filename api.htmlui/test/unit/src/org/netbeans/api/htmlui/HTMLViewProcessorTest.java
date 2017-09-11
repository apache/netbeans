/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package org.netbeans.api.htmlui;

import java.io.IOException;
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
