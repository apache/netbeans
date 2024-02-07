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
package org.netbeans.modules.java.mx.project.suitepy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

final class Parse {
    private Parse() {
    }

    static MxSuite parse(URL u) throws IOException {
        if (u == null) {
            return null;
        }
        return new Parse().parseImpl(u);
    }

    private MxSuite parseImpl(URL u) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(u.openStream()))) {
            for (;;) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                line = line.replaceAll("(\\s*)#.*", "$1");
                sb.append(line).append("\n");
            }
        }
        String text = jsonify(sb.toString());
        Object value;
        try {
            final JSONParser p = new JSONParser();
            value = p.parse(text);
        } catch (ParseException ex) {
            throw new IOException("Cannot parse " + u, ex);
        }
        Value suite = new Value(value);
        return Meta.create(MxSuite.class).cast(suite);
    }

    private static String jsonify(String content) {
        String text = content.replaceFirst("\\s*suite *= *\\{", "{");
        text = text.replace("True", "true").replace("False", "false");
        text = text.replaceAll(",\\s*(\\]|\\})", "$1");
        text = replaceQuotes("\"\"\"", text);
        text = replaceQuotes("'''", text);
        text = replaceQuotes("'", text);
        return text;
    }

    private static String replaceQuotes(String quoteSeq, String imports) {
        Matcher m = Pattern.compile(quoteSeq + "(.*?)" + quoteSeq, Pattern.DOTALL).matcher(imports);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "\"");
            sb.append(m.group(1).replace("\n", "\\n"));
            sb.append("\"");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    static List<String> objectKeys(JSONObject o) {
        return new ArrayList<>(o.keySet());
    }

    private static final class Handler implements InvocationHandler {
        private final Value value;

        public Handler(Value value) {
            this.value = value;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(value, args);
            }

            final Meta<?> type = Meta.create(method.getGenericReturnType());
            String key = method.getName();
            if (!hasMember(value, key)) {
                if (type.isMap()) {
                    return Collections.emptyMap();
                }
                if (type.isList()) {
                    return Collections.emptyList();
                }
                if (type.clazz == boolean.class) {
                    return false;
                }
                return null;
            }
            Value result = value.getMember(key);
            return type.cast(result);
        }

        private static boolean hasMember(Value value, String key) {
            return value.hasMember(key);
        }
    }

    private static final class Meta<T> {
        final Class<T> clazz;
        final Type type;

        Meta(Class<T> clazz, Type type) {
            this.clazz = clazz;
            this.type = type;
        }

        public static <T> Meta<T> create(Class<T> clazz) {
            return new Meta<>(clazz, clazz);
        }

        public static Meta<?> create(Type type) {
            if (type instanceof Class<?>) {
                return create((Class<?>)type);
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                return new Meta<>((Class<?>) pt.getRawType(), pt);
            }
            throw new IllegalStateException();
        }

        public Meta<?> getComponent(int index) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type ct = pt.getActualTypeArguments()[index];
                return new Meta<>((Class<?>)ct, ct);
            }
            TypeVariable<? extends Class<?>> ct = clazz.getTypeParameters()[index];
            throw new IllegalStateException("" + ct);
        }

        boolean isInterface() {
            return clazz.isInterface();
        }

        T cast(Value result) {
            Object val;
            if (clazz == Map.class) {
                val = new HandlerMap(result, getComponent(0), getComponent(1));
            } else if (clazz == List.class) {
                val = new HandlerList<>(result, getComponent(0));
            } else if (clazz.isInterface()) {
                val = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new Handler(result));
            } else if (clazz == boolean.class) {
                @SuppressWarnings("unchecked")
                final T asBoolean = (T) Boolean.valueOf(result.asString());
                return asBoolean;
            } else {
                val = result.asString();
            }
            return clazz.cast(val);
        }

        boolean isMap() {
            return clazz == Map.class;
        }

        boolean isList() {
            return clazz == List.class;
        }

    }

    private static final class HandlerMap extends AbstractMap<String, Object> {
        private final Value value;
        private final Meta<?> keyType;
        private final Meta<?> valueType;

        HandlerMap(Value result, Meta<?> keyType, Meta<?> valueType) {
            this.value = result;
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            Set<Entry<String, Object>> set = new TreeSet<>();
            for (String key : value.getMemberKeys()) {
                set.add(new HE(key));
            }
            return set;
        }

        private final class HE implements Entry<String, Object>, Comparable<HE> {
            final String key;

            public HE(String key) {
                this.key = key;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object getValue() {
                return valueType.cast(value.getMember(key));
            }

            @Override
            public Object setValue(Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int compareTo(HE o) {
                return key.compareTo(o.key);
            }
        }
    }

    private static final class HandlerList<T> extends AbstractList<T> {
        private final Value value;
        private final Meta<T> elementType;

        HandlerList(Value value, Meta<T> elementType) {
            this.value = value;
            this.elementType = elementType;
        }

        @Override
        public T get(int index) {
            return elementType.cast(value.getArrayElement(index));
        }

        @Override
        public int size() {
            return value.getArraySize();
        }

    }

    private final class Value {
        private final Object obj;
        private final Map<String,Value> members = new HashMap<>();
        private Number arraySize;
        private String asString;

        Value(Object obj) {
            this.obj = obj;
        }

        Value getMember(String key) {
            Value value = members.get(key);
            if (value == null && obj instanceof JSONObject) {
                value = new Value(((JSONObject)obj).get(key));
                members.put(key, value);
            }
            return value;
        }

        Value getArrayElement(int index) {
            if (obj instanceof List<?>) {
                return new Value(((List<?>)obj).get(index));
            }
            return getMember("" + index);
        }

        int getArraySize() {
            if (arraySize == null) {
                arraySize = ((JSONArray) obj).size();
            }
            return arraySize.intValue();
        }

        Iterable<String> getMemberKeys() {
            final List<?> keys = objectKeys((JSONObject) obj);
            Value names = new Value(keys);
            final int namesCount = keys.size();
            List<String> arrOfNames = new ArrayList<>(namesCount);
            for (int i = 0; i < namesCount; i++) {
                arrOfNames.add(names.getArrayElement(i).asString());
            }
            return arrOfNames;
        }

        String asString() {
            if (asString == null) {
                asString = obj.toString();
            }
            return asString;
        }

        boolean hasMember(String key) {
            return getMember(key).obj != null;
        }
    }
}
