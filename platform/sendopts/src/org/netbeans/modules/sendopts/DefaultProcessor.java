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

package org.netbeans.modules.sendopts;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.openide.util.Lookup;

/** Processor that is configured from a map, usually from a layer.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class DefaultProcessor extends OptionProcessor {
    private static final Option defArgs = Option.defaultArguments();
    private final String clazz;
    private final Object instance;
    private final Set<Option> options;

    private DefaultProcessor(
        String clazz, Object instance, Set<Option> arr
    ) {
        this.clazz = clazz;
        this.instance = instance;
        this.options = Collections.unmodifiableSet(arr);
    }

    private static Option createOption(String type, Character shortName, String longName, String displayName, String description) {
        Option o = null;
        if (shortName == null) {
            shortName = Option.NO_SHORT_NAME;
        }
        switch (Type.valueOf(type)) {
            case withoutArgument: o = Option.withoutArgument(shortName, longName); break;
            case requiredArgument: o = Option.requiredArgument(shortName, longName); break;
            case optionalArgument: o = Option.optionalArgument(shortName, longName); break;
            case additionalArguments: o = Option.additionalArguments(shortName, longName); break;
            default: assert false;
        }
        if (displayName != null) {
            String[] arr = fixBundles(displayName.split("#", 2)); // NOI18N
            o = Option.displayName(o, arr[0], arr[1]);
        }
        if (description != null) {
            String[] arr = fixBundles(description.split("#", 2)); // NOI18N
            o = Option.shortDescription(o, arr[0], arr[1]);
        }
        return o;
    }
    
    public static OptionProcessor create(Class<?> clazz, Object instance) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("class", clazz.getName());
        if (instance != null) {
            map.put("instance", instance);
        }
        int cnt = 1;
        for (Field e : clazz.getFields()) {
            Arg o = e.getAnnotation(Arg.class);
            if (o == null) {
                continue;
            }
            Description d = e.getAnnotation(Description.class);

            if (o.shortName() != Option.NO_SHORT_NAME) {
                map.put(cnt + ".shortName", o.shortName());
            }
            if (!o.longName().isEmpty()) {
                map.put(cnt + ".longName", o.longName());
            }
            if (e.getType() == boolean.class) {
                map.put(cnt + ".type", "withoutArgument");
            } else if (String.class == e.getType()) {
                if (o.defaultValue().equals("\u0000")) {
                    map.put(cnt + ".type", "requiredArgument");
                } else {
                    map.put(cnt + ".type", "optionalArgument");
                }
            } else {
                if (!String[].class.equals(e.getType())) {
                    throw new IllegalStateException("Field type has to be either boolean, String or String[]! " + e);
                }
                map.put(cnt + ".type", "additionalArguments");
            }
            if (o.implicit()) {
                map.put(cnt + ".implicit", true);
            }
            if (d != null) {
                writeBundle(map, cnt + ".displayName", d.displayName(), e);
                writeBundle(map, cnt + ".shortDescription", d.shortDescription(), e);
            }
            cnt++;
        }
        return create(map);
    }
    
    static OptionProcessor create(Map<?,?> map) {
        String c = (String) map.get("class");
        Set<Option> arr = new LinkedHashSet<Option>();
        for (int cnt = 1; ; cnt++) {
            Character shortName = (Character) map.get(cnt + ".shortName"); // NOI18N
            String longName = (String) map.get(cnt + ".longName"); // NOI18N
            if (shortName == null && longName == null) {
                break;
            }
            String type = (String) map.get(cnt + ".type"); // NOI18N
            String displayName = (String)map.get(cnt + ".displayName"); // NOI18N
            String description = (String)map.get(cnt + ".shortDescription"); // NOI18N
            arr.add(createOption(type, shortName, longName, displayName, description));
            if (Boolean.TRUE.equals(map.get(cnt + ".implicit"))) { // NOI18N
                arr.add(defArgs);
            }
        }
        Object instance = map.get("instance"); // NOI18N
        return new DefaultProcessor(c, instance, arr);
    }
    

    @Override
    protected Set<Option> getOptions() {
        return options;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        try {
            ClassLoader l = findClassLoader();
            Class<?> realClazz;
            Object inst;
            if (instance == null) {
                realClazz = Class.forName(clazz, true, l);
                inst = realClazz.getDeclaredConstructor().newInstance();
            } else {
                realClazz = instance.getClass();
                inst = instance;
            }
            Map<Option,Field> map = processFields(realClazz, options);
            for (Map.Entry<Option, String[]> entry : optionValues.entrySet()) {
                final Option option = entry.getKey();
                Type type = Type.valueOf(option);
                Field f = map.get(option);
                assert f != null : "No field for option: " + option;
                switch (type) {
                    case withoutArgument:
                        f.setBoolean(inst, true); break;
                    case requiredArgument:
                        f.set(inst, entry.getValue()[0]); break;
                    case optionalArgument:
                        if (entry.getValue().length == 1) {
                            f.set(inst, entry.getValue()[0]);
                        } else {
                            f.set(inst, f.getAnnotation(Arg.class).defaultValue());
                        }
                        break;
                    case additionalArguments:
                        f.set(inst, entry.getValue()); break;
                    case defaultArguments:
                        f.set(inst, entry.getValue()); break;
                }
            }
            if (inst instanceof Runnable) {
                ((Runnable)inst).run();
            }
            if (inst instanceof ArgsProcessor) {
                ((ArgsProcessor)inst).process(env);
            }
        } catch (CommandException exception) {
            throw exception;
        } catch (Exception exception) {
            throw (CommandException)new CommandException(10, exception.getLocalizedMessage()).initCause(exception);
        }
    }

    public static ClassLoader findClassLoader() {
        ClassLoader l = null;
        try {
            l = findClassLoaderFromLookup();
        } catch (LinkageError ex) {
            // OK, lookup is not on classpath
        }
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = DefaultProcessor.class.getClassLoader();
        }
        return l;
    }

    private static ClassLoader findClassLoaderFromLookup() {
        return Lookup.getDefault().lookup(ClassLoader.class);
    }
    
    private static Map<Option,Field> processFields(Class<?> type, Set<Option> options) {
        Map<Option,Field> map = new HashMap<Option, Field>();
        for (Field f : type.getFields()) {
            Arg arg = f.getAnnotation(Arg.class);
            if (arg == null) {
                continue;
            }
            Option o = null;
            for (Option c : options) {
                char shortN = (char)OptionImpl.Trampoline.DEFAULT.getShortName(c);
                String longN = OptionImpl.Trampoline.DEFAULT.getLongName(c);
                
                if (shortN == (int)arg.shortName() && equalStrings(longN, arg)) {
                    o = c;
                    break;
                }
            }
            assert o != null : "No option for field " + f + " options: " + options;
            map.put(o, f);
            if (arg.implicit()) {
                map.put(defArgs, f);
            }
        }
        assert map.size() == options.size() : "Map " + map + " Options " + options;
        return map;
    }
    private static boolean equalStrings(String longN, Arg arg) {
        if (longN == null) {
            return arg.longName().isEmpty();
        } else {
            return longN.equals(arg.longName());
        }
    }

    private static enum Type {
        withoutArgument, requiredArgument, optionalArgument, 
        additionalArguments, defaultArguments;
        
        public static Type valueOf(Option o) {
            OptionImpl impl = OptionImpl.Trampoline.DEFAULT.impl(o);
            switch (impl.argumentType) {
                case 0: return withoutArgument;
                case 1: return requiredArgument;
                case 2: return optionalArgument;
                case 3: return additionalArguments;
                case 4: return defaultArguments;
            }
            assert false;
            return null;
        }
    }
    private static void writeBundle(Map<String,Object> f, String key, String value, Field e) throws IllegalStateException {
        if (value.isEmpty()) {
            return;
        }
        if (value.startsWith("#")) {
            Package pkg = e.getDeclaringClass().getPackage();
            value = pkg.getName() + ".Bundle" + value;
        }
        f.put(key, value);
    }
    private static String[] fixBundles(String[] oneOrTwo) {
        if (oneOrTwo.length == 2) {
            return oneOrTwo;
        } else {
            return new String[] { OptionImpl.NO_BUNDLE, oneOrTwo[0] };
        }
    }
}
