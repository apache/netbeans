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
package org.netbeans.modules.htmlui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import net.java.html.js.JavaScriptBody;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@TopComponent.Description(
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "browser"
)
public final class HtmlComponent extends TopComponent  {
    private static final Logger LOG = Logger.getLogger(HtmlComponent.class.getName());
    private final NbFxPanel p = new NbFxPanel();
    private /* final */ WebView v;
    private final InstanceContent ic;
    private Object value;
    private final Map<String,Object> cache = new HashMap<>();
    
    HtmlComponent() {
        ic = new InstanceContent();
        associateLookup(new AbstractLookup(ic));
        setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER);
    }
    
    final WebView getWebView() {
        return v;
    }
    
    public void loadFX(URL pageUrl, final Class<?> clazz, final String m, Object... ctx) {
        initFX();
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = clazz.getClassLoader();
        }
        NbBrowsers.load(v, pageUrl, new Runnable() {
            @Override
            public void run() {
                try {
                    HtmlComponent hc = HtmlComponent.this;
                    Method method = clazz.getMethod(m);
                    Object value = method.invoke(null);
                    if (value != null) {
                        hc.value = value;
                        hc.ic.add(value);
                    }
                    listenOnContext(hc);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Can't load " + m + " from " + clazz, ex);
                }
            }
        }, loader, ctx);
    }
    

    private void initFX() {
        Platform.setImplicitExit(false);
        v = new WebView();
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp, Color.ALICEBLUE);

        class X implements ChangeListener<String>, Runnable {

            private String title;

            public X() {
                super();
            }

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                title = v.getEngine().getTitle();
                EventQueue.invokeLater(this);
            }

            @Override
            public void run() {
                if (title != null) {
                    HtmlComponent.this.setDisplayName(title);
                }
            }
        }
        final X x = new X();
        v.getEngine().titleProperty().addListener(x);
        Platform.runLater(x);
        bp.setCenter(v);
        p.setScene(scene);
    }

    final void onChange(Object[] values) {
        List<Object> instances = new ArrayList<>();
        if (this.value != null) {
            instances.add(this.value);
        }
        if (values != null) {
            HashMap<String, Object> c = new HashMap<>(cache);
            for (Object o : values) {
                if (o instanceof String) {
                    Object inst = c.remove((String)o);
                    if (inst == null) try {
                        Class<?> cookie = loadClass((String) o);
                        Constructor<?>[] arr = cookie.getConstructors();
                        if (arr.length != 1) {
                            LOG.log(Level.WARNING, "Class {0} should have one public constructor. Found {1}", new Object[]{cookie, Arrays.toString(arr)});
                            continue;
                        }
                        Constructor<?> cnstr = arr[0];
                        if (cnstr.getParameterTypes().length == 1) {
                            inst = cnstr.newInstance(value);
                        } else {
                            inst = cnstr.newInstance();
                        }
                        cache.put((String)o, inst);
                    } catch (Throwable ex) {
                        LOG.log(Level.WARNING, "Cannot associate context class " + o, ex);
                        continue;
                    }
                    instances.add(inst);
                }
            }
            for (Map.Entry<String, Object> entry : c.entrySet()) {
                if (entry.getValue() instanceof Closeable) {
                    try {
                        ((Closeable)entry.getValue()).close();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Error when closing" + entry.getValue(), ex);
                    }
                    cache.remove(entry.getKey());
                }
            }
        }
        ic.set(instances, null);
    }

    @JavaScriptBody(args = {"onChange"}, javacall = true, body = ""
        + "if (typeof ko === 'undefined') return;\n"
        + "var data = ko.dataFor(window.document.body);\n"
        + "if (typeof data === 'undefined') return;\n"
        + "if (typeof data.context === 'undefined') return;\n"
        + "data.context.subscribe(function(value) {\n"
        + "  onChange.@org.netbeans.modules.htmlui.HtmlComponent::onChange([Ljava/lang/Object;)(value);\n"
        + "});\n"
        + "onChange.@org.netbeans.modules.htmlui.HtmlComponent::onChange([Ljava/lang/Object;)(data.context());\n"
    )
    private static native void listenOnContext(HtmlComponent onChange);

    static Class loadClass(String c) throws ClassNotFoundException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = Pages.class.getClassLoader();
        }
        return Class.forName(c, true, l);
    }
}
