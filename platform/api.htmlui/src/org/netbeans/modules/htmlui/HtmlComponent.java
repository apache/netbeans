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
package org.netbeans.modules.htmlui;

import java.awt.BorderLayout;
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
import javax.swing.JComponent;
import net.java.html.js.JavaScriptBody;
import static org.netbeans.modules.htmlui.HtmlToolkit.LOG;
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
    private final JComponent p = HtmlToolkit.getDefault().newPanel();
    private /* final */ Object webView;
    private final InstanceContent ic;
    private Object value;
    private final Map<String,Object> cache = new HashMap<>();
    
    HtmlComponent() {
        ic = new InstanceContent();
        associateLookup(new AbstractLookup(ic));
        setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER);
    }
    
    public void loadFX(URL pageUrl, final Class<?> clazz, final String m, Object... ctx) {
        webView = HtmlToolkit.getDefault().initHtmlComponent(p, this::setDisplayName);
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = clazz.getClassLoader();
        }
        HtmlToolkit.getDefault().load(webView, pageUrl, new Runnable() {
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
