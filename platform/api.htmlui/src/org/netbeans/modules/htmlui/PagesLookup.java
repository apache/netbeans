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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.js.JavaScriptBody;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public final class PagesLookup extends AbstractLookup {
    private static final Logger LOG = Logger.getLogger(PagesLookup.class.getName());

    private final ClassLoader loader;
    private final Object value;
    private final InstanceContent ic;
    private final Map<String, Object> cache = new HashMap<>();

    public PagesLookup(ClassLoader loader, Object value) {
        this(loader, value, new InstanceContent());
    }

    private PagesLookup(ClassLoader loader, Object value, InstanceContent ic) {
        super(ic);
        this.ic = ic;
        this.loader = loader;
        this.value = value;
        if (value != null) {
            ic.add(value);
        }
        listenOnContext(this);
    }

    private Class<?> loadClass(String c) throws ClassNotFoundException {
        return Class.forName(c, true, loader);
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
                    Object inst = c.remove((String) o);
                    if (inst == null) {
                        try {
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
                            cache.put((String) o, inst);
                        } catch (Throwable ex) {
                            LOG.log(Level.WARNING, "Cannot associate context class " + o, ex);
                            continue;
                        }
                    }
                    instances.add(inst);
                }
            }
            for (Map.Entry<String, Object> entry : c.entrySet()) {
                if (entry.getValue() instanceof Closeable) {
                    try {
                        ((Closeable) entry.getValue()).close();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Error when closing" + entry.getValue(), ex);
                    }
                    cache.remove(entry.getKey());
                }
            }
        }
        ic.set(instances, null);
    }

    @JavaScriptBody(args = {"onChange"}, javacall = true, body = "" +
        " if (typeof ko === 'undefined') return;\n" +
        " var data = ko.dataFor(window.document.body);\n" +
        " if (typeof data === 'undefined') return;\n" +
        " if (typeof data.context === 'undefined') return;\n" +
        " function update(value) {\n" +
        "   onChange.@org.netbeans.modules.htmlui.PagesLookup::onChange([Ljava/lang/Object;)(value);\n" +
        " }\n" +
        " data.context.subscribe(update);\n" +
        " update(data.context());\n"
    )
    private static native void listenOnContext(PagesLookup onChange);
}
