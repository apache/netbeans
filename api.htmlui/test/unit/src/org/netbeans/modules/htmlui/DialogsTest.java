/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.htmlui;

import java.awt.EventQueue;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JButton;
import javax.swing.JFrame;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;
import org.netbeans.api.htmlui.HTMLDialog;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class DialogsTest {
    private static final CountDownLatch down = new CountDownLatch(1);
    private static BrwsrCtx ctx;

    public DialogsTest() {
    }

    @BeforeClass
    public static void initializeContext() throws Exception {
        final JFXPanel p = new JFXPanel();
        final URL u = DialogsTest.class.getResource("/org/netbeans/api/htmlui/empty.html");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView v = new WebView();
                Scene s = new Scene(v);
                p.setScene(s);
                NbBrowsers.load(v, u, new Runnable() {
                    @Override
                    public void run() {
                        ctx = BrwsrCtx.findDefault(DialogsTest.class);
                        down.countDown();
                    }
                }, null);
            }
        });
        down.await();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
    }

    @Test public void parseButtons() throws Throwable {
        final Throwable[] ex = { null };
        final JButton[] buttons = { null, null };
        final CountDownLatch done = new CountDownLatch(1);
        ctx.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String body =
                        "    <button>Normal button in a text</button>" +
                        "    <button hidden=true id='OK' disabled=true>Agree</button>" +
                        "    <button hidden=true id='Cancel'>Disagree</button>";
                    setBody(body);
                    
                    JButton[] arr = Buttons.buttons();
                    assertEquals(arr.length, 2, "Two buttons");
                    assertEquals(arr[0].getName(), "OK", "id of 1st button parsed");
                    assertEquals(arr[1].getName(), "Cancel", "id of 2nd button parsed");
                    assertEquals(arr[0].getText(), "Agree", "text of 1st button parsed");
                    assertEquals(arr[1].getText(), "Disagree", "text of 2nd button parsed");
                    
                    assertFalse(arr[0].isEnabled(), "OK is disabled");
                    assertTrue(arr[1].isEnabled(), "Cancel is enabled");
                    
                    setDisabled("OK", false);
                    
                    String prev = setText("OK", "Fine");
                    assertEquals(prev, "Agree");
                    
                    buttons[0] = arr[0];
                    buttons[1] = arr[1];
                } catch (Throwable t) {
                    ex[0] = t;
                } finally {
                    done.countDown();
                }
            }
        });
        done.await();
        if (ex[0] != null) {
            throw ex[0];
        }
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertEquals(buttons[0].getText(), "Fine", "Text of OK changed");
                assertTrue(buttons[0].isEnabled(), "OK is now enabled");
            }
        });
    }
    
    @Test public void noDefinedButtonsMeanOKCancel() throws Throwable {
        final Throwable[] ex = { null };
        final CountDownLatch done = new CountDownLatch(1);
        ctx.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String body =
                        "    <button>Normal button in a text</button>" +
// no dialog buttons defined:                            
//                        "    <button hidden=true id='OK' disabled=true>Agree</button>" +
//                        "    <button hidden=true id='Cancel'>Disagree</button>" +
                        "";
                    setBody(body);
                    
                    JButton[] arr = Buttons.buttons();
                    assertEquals(arr.length, 2, "Two buttons");
                    assertEquals(arr[0].getName(), "OK", "id of 1st default button");
                    assertEquals(arr[1].getName(), null, "id of 2nd default button");
                    
                    assertTrue(arr[0].isEnabled(), "OK is enabled");
                    assertTrue(arr[1].isEnabled(), "Cancel is enabled");
                } catch (Throwable t) {
                    ex[0] = t;
                } finally {
                    done.countDown();
                }
            }
        });
        done.await();
        if (ex[0] != null) {
            throw ex[0];
        }
    }
    
    @JavaScriptBody(args = "b", body = "window.document.getElementsByTagName('body')[0].innerHTML = b;")
    private static native void setBody(String b);
    
    @JavaScriptBody(args = { "id", "state" }, body = "window.document.getElementById(id).disabled = state;")
    private static native void setDisabled(String id, boolean state);
    
    @JavaScriptBody(args = { "id", "t" }, body = ""
            + "var prev = window.document.getElementById(id).innerHTML;\n"
            + "window.document.getElementById(id).innerHTML = t;\n"
            + "return prev;\n"
    )
    private static native String setText(String id, String t);
    
    
    @HTMLDialog(url = "simple.html", className = "TestPages") 
    static void showDialog() {
        String ret = TestPages.showDialog();
    }

    @HTMLDialog(url = "http://www.netbeans.org", className = "TestPages") 
    static void showDialog(int x, String[] y, DialogsTest t) {
        String ret = TestPages.showDialog(10, y, null);
    }

}
