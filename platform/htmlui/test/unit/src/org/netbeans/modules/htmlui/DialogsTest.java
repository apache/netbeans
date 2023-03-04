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
import org.netbeans.modules.htmlui.impl.EnsureJavaFXPresent;
import org.netbeans.modules.htmlui.impl.HtmlToolkit;
import org.netbeans.modules.htmlui.impl.SwingFXViewer;
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

    @BeforeClass(timeOut = 9000)
    public static void initializeContext() throws Exception {
        if (!EnsureJavaFXPresent.check()) {
            return;
        }
        final JFXPanel p = new JFXPanel();
        final URL u = DialogsTest.class.getResource("/org/netbeans/modules/htmlui/impl/empty.html");
        assertNotNull(u, "empty.html found");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView v = new WebView();
                Scene s = new Scene(v);
                p.setScene(s);
                HtmlToolkit.getDefault().load(v, u, new Runnable() {
                    @Override
                    public void run() {
                        ctx = BrwsrCtx.findDefault(DialogsTest.class);
                        down.countDown();
                    }
                }, DialogsTest.class.getClassLoader(), new Object[0]);
            }
        });
        down.await();
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
    }

    @Test(timeOut = 9000)
    public void parseButtons() throws Throwable {
        EnsureJavaFXPresent.checkAndThrow();
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



                    JButton[] arr = new MockButtons().array();
                    assertEquals(arr.length, 2, "Two buttons");
                    assertButtonName(arr[0], "OK", "id of 1st button parsed");
                    assertButtonName(arr[1], "Cancel", "id of 2nd button parsed");
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

    @Test(timeOut = 9000)
    public void noDefinedButtonsMeanOKCancel() throws Throwable {
        EnsureJavaFXPresent.checkAndThrow();
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

                    JButton[] arr = new MockButtons().array();
                    assertEquals(arr.length, 2, "Two buttons");
                    assertButtonName(arr[0], "OK", "id of 1st default button");
                    assertButtonName(arr[1], null, "id of 2nd default button");

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


    @HTMLDialog(url = "impl/simple.html", className = "TestPages")
    static void showDialog() {
        String ret = TestPages.showDialog();
    }

    @HTMLDialog(url = "http://www.netbeans.org", className = "TestPages")
    static HTMLDialog.OnSubmit showDialog(int x, String[] y, DialogsTest t) {
        return (id) -> {
            TestPages.showDialog(10, y, null);
            return true;
        };
    }

    static void assertButtonName(JButton b, String exp, String msg) {
        assertNotNull(b, msg);
        String n = b.getName();
        if (exp == null) {
            assertNull(n, msg);
            return;
        }
        assertTrue(n.startsWith("dialog-buttons-"), "Starts with prefix: " + n);
        String r = n.substring(15);
        assertEquals(r, exp, msg);
    }

    private static final class MockButtons extends Buttons<SwingFXViewer.SFXView, JButton> {
        MockButtons() {
            super(new SwingFXViewer() {
                @Override
                public JButton createButton(SwingFXViewer.SFXView view, String id) {
                    JButton b = new JButton();
                    b.setName(id);
                    return b;
                }
            }, null, null);
        }

        final JButton[] array() {
            return buttons().toArray(new JButton[0]);
        }
    }
}
