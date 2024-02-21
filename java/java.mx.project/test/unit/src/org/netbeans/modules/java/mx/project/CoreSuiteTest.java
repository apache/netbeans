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
package org.netbeans.modules.java.mx.project;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;

public final class CoreSuiteTest {
    @Test
    public void generateCoreSuite() {
        Assume.assumeTrue("Run main method to generate CoreSuite.java body", false);
    }

    public static void main(String... args) throws Exception {
        // mx version "5.279.0"
        URL u = new URI("https://raw.githubusercontent.com/graalvm/mx/dcfad27487a5d13d406febc92976cf3c026e50dd/mx.mx/suite.py").toURL();
        assert u != null : "mx suite found";
        MxSuite mxSuite = MxSuite.parse(u);

        StringBuilder sb = new StringBuilder();
        dump(sb, "", mxSuite);
        System.out.println(sb.toString());
    }

    private static void dump(StringBuilder sb, String indent, Object obj) throws SecurityException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        if (obj == null) {
            sb.append("null");
        } else if (obj.getClass().getName().startsWith("java.lang.")) {
            if (obj instanceof String) {
                sb.append('"').append(obj).append('"');
            } else {
                sb.append(obj);
            }
        } else if (obj instanceof Map<?,?>) {
            Map<?,?> map = (Map<?,?>) obj;
            if (map.isEmpty()) {
                sb.append("Collections.emptyMap()");
            } else {
                boolean first = true;
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (first) {
                        sb.append("mapOf(").append(e.getKey().getClass().getName());
                        final Class<?> type = e.getValue().getClass().getInterfaces()[0];
                        final String typeName = type.getSimpleName();
                        sb.append(".class, ").append(typeName);
                        sb.append(".class)");
                        first = false;
                    }
                    sb.append(".of(\n" + indent);
                    dump(sb, indent, e.getKey());
                    sb.append(", ");
                    dump(sb, indent + "  ", e.getValue());
                    sb.append(')');
                }
                sb.append(".build()");
            }
        } else if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            sb.append("Arrays.asList(");
            String sep = "";
            for (Object elem : list) {
                sb.append(sep);
                sb.append("\n").append(indent);
                dump(sb, indent + "  ", elem);
                sep = ",";
            }
            sb.append(")");
        } else {
            final Class<?> type = obj.getClass().getInterfaces()[0];
            final String typeName = type.getSimpleName();
            sb.append("create" + typeName + "(");
            String sep = "";
            for (Method m : sortMethods(type.getMethods())) {
                if (Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                sb.append(sep);
                sb.append("\n").append(indent);
                sb.append("/* ");
                sb.append(m.getName());
                sb.append(" */");
                final Object value = m.invoke(obj);
                dump(sb, "  " + indent, value);
                sep = ",";
            }
            sb.append("\n").append(indent).append(")");
        }
    }

    private static Iterable<Method> sortMethods(Method[] methods) {
        List<Method> arr = new ArrayList<>(Arrays.asList(methods));
        arr.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        return arr;
    }
}
