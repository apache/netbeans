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

package org.netbeans.modules.java.hints.providers.code;

import com.sun.source.tree.Tree.Kind;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.ClassWrapper;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.FieldWrapper;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.MethodWrapper;
import org.netbeans.modules.java.hints.providers.code.ReflectiveCustomizerProvider.OptionDescriptor;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.providers.spi.HintProvider;
import org.netbeans.modules.java.hints.providers.spi.Trigger.Kinds;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerOptions;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=HintProvider.class)
public class CodeHintProviderImpl implements HintProvider {

    private static final Logger LOG = Logger.getLogger(WorkerImpl.class.getName());
    
    @Override
    public Map<HintMetadata, ? extends Collection<? extends HintDescription>> computeHints() {
        return computeHints(findLoader(), "META-INF/nb-hints/hints");
    }

    private Map<HintMetadata, ? extends Collection<? extends HintDescription>> computeHints(ClassLoader l, String path) {
        Map<HintMetadata, Collection<HintDescription>> result = new HashMap<>();
        
        for (ClassWrapper c : FSWrapper.listClasses()) {
            try {
                processClass(c, result);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }

        return result;
    }

    static ClassLoader findLoader() {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

        if (l == null) {
            return CodeHintProviderImpl.class.getClassLoader();
        }

        return l;
    }

    public static void processClass(ClassWrapper clazz, Map<HintMetadata, Collection<HintDescription>> result) throws SecurityException {
        Hint metadata = clazz.getAnnotation(Hint.class);
        HintMetadata hm;
        
        if (metadata != null) {
            String id = metadata.id();

            if (id == null || id.length() == 0) {
                id = clazz.getName();
            }
            hm = fromAnnotation(id, clazz, null, metadata);
        } else {
            hm = null;
        }
        
        for (MethodWrapper m : clazz.getMethods()) {
            Hint localMetadataAnnotation = m.getAnnotation(Hint.class);
            HintMetadata localMetadata;

            if (localMetadataAnnotation != null) {
                String localID = localMetadataAnnotation.id();

                if (localID == null || localID.length() == 0) {
                    localID = clazz.getName() + "." + m.getName();
                }

                localMetadata = fromAnnotation(localID, clazz, m, localMetadataAnnotation);
            } else {
                localMetadata = hm;
            }

            if (localMetadata != null) {
                processMethod(result, m, localMetadata);
            }
        }
    }

    private static HintMetadata fromAnnotation(String id, ClassWrapper clazz, MethodWrapper method, Hint metadata) {
        HintMetadata hm = HintMetadata.Builder.create(id)
                                              .setDescription(metadata.displayName(), metadata.description())
                                              .setCategory(metadata.category())
                                              .setEnabled(metadata.enabled())
                                              .setSeverity(metadata.severity())
                                              .setKind(metadata.hintKind())
                                              .setCustomizerProvider(createCustomizerProvider(clazz, method, id, metadata))
                                              .addSuppressWarnings(metadata.suppressWarnings())
                                              .addOptions(Options.fromHintOptions(metadata.options()).toArray(new Options[0]))
                                              .setSourceVersion(metadata.minSourceVersion())
                                              .build();
        return hm;
    }

    private static CustomizerProvider createCustomizerProvider(ClassWrapper clazz, MethodWrapper method, String id, Hint hint) {
        Class<? extends CustomizerProvider> customizerClass = hint.customizerProvider();

        if (customizerClass != CustomizerProvider.class) {
            return new DelegatingCustomizerProvider(customizerClass);
        }

        Set<String> allowedOptions = null;

        if (method != null) {
            UseOptions useOptions = method.getAnnotation(UseOptions.class);

            if (useOptions == null) return null;

            allowedOptions = new HashSet<>(Arrays.asList(useOptions.value()));
        }

        List<OptionDescriptor> declarativeOptions = new ArrayList<>();

        for (FieldWrapper fw : clazz.getFields()) {
            BooleanOption option = fw.getAnnotation(BooleanOption.class);
            IntegerOption iOption = fw.getAnnotation(IntegerOption.class);
            
            String key = fw.getConstantValue();

            if (key == null) continue;
            if (allowedOptions != null && !allowedOptions.contains(key)) continue;
            
            Object defValue;
            String displayName;
            String tooltip;
            if (option != null) {
                defValue = option.defaultValue();
                displayName = option.displayName();
                tooltip = option.tooltip();
            } else if (iOption != null) {
                defValue = iOption.defaultValue();
                displayName = iOption.displayName();
                tooltip = iOption.tooltip();
            } else {
                return null;
            }
            
            declarativeOptions.add(
                new OptionDescriptor(
                    key, 
                    defValue,
                    displayName,
                    tooltip,
                    option != null ? option : iOption)
            );
        }

        return !declarativeOptions.isEmpty() ? new ReflectiveCustomizerProvider(clazz.getName(), id, declarativeOptions) : null;
    }
    
    static void processMethod(Map<HintMetadata, Collection<HintDescription>> hints, MethodWrapper m, HintMetadata metadata) {
        //XXX: combinations of TriggerTreeKind and TriggerPattern?
        processTreeKindHint(hints, m, metadata);
        processPatternHint(hints, m, metadata);
    }
    
    private static void processTreeKindHint(Map<HintMetadata, Collection<HintDescription>> hints, MethodWrapper m, HintMetadata metadata) {
        TriggerTreeKind kindTrigger = m.getAnnotation(TriggerTreeKind.class);
        
        if (kindTrigger == null) {
            return ;
        }
        TriggerOptions opts = m.getAnnotation(TriggerOptions.class);
        Worker w = new WorkerImpl(m.getClazz().getName(), m.getName());

        Set<Kind> kinds = EnumSet.noneOf(Kind.class);
        
        kinds.addAll(Arrays.asList(kindTrigger.value()));

        addHint(hints, metadata, HintDescriptionFactory.create()
                                                       .setTrigger(new Kinds(kinds))
                                                       .setTriggerOptions(opts == null ? null : opts.value())
                                                       .setWorker(w)
                                                       .setMetadata(metadata)
                                                       .produce());
    }
    
    private static void processPatternHint(Map<HintMetadata, Collection<HintDescription>> hints, MethodWrapper m, HintMetadata metadata) {
        TriggerPattern patternTrigger = m.getAnnotation(TriggerPattern.class);

        if (patternTrigger != null) {
            processPatternHint(hints, patternTrigger, m, metadata);
            return ;
        }

        TriggerPatterns patternTriggers = m.getAnnotation(TriggerPatterns.class);

        if (patternTriggers != null) {
            for (TriggerPattern pattern : patternTriggers.value()) {
                processPatternHint(hints, pattern, m, metadata);
            }
        }
    }

    private static void processPatternHint(Map<HintMetadata, Collection<HintDescription>> hints, TriggerPattern patternTrigger, MethodWrapper m, HintMetadata metadata) {
        String pattern = patternTrigger.value();
        Map<String, String> constraints = new HashMap<>();

        for (ConstraintVariableType c : patternTrigger.constraints()) {
            constraints.put(c.variable(), c.type());
        }
        TriggerOptions opts = m.getAnnotation(TriggerOptions.class);
        PatternDescription pd = PatternDescription.create(pattern, constraints);

        addHint(hints, metadata, HintDescriptionFactory.create()
                                                       .setTrigger(pd)
                                                       .setTriggerOptions(opts == null ? null : opts.value())
                                                       .setWorker(new WorkerImpl(m.getClazz().getName(), m.getName()))
                                                       .setMetadata(metadata)
                                                       .produce());
    }

    private static void addHint(Map<HintMetadata, Collection<HintDescription>> hints, HintMetadata metadata, HintDescription hint) {
        hints.computeIfAbsent(metadata, k -> new LinkedList<>())
             .add(hint);
    }

    //accessed by tests:
    static final class WorkerImpl implements Worker {

        private final String className;
        private final String methodName;

        public WorkerImpl(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        private final AtomicReference<Method> methodRef = new AtomicReference<>();
        private Set<FileObject> exceptionThrownFor;

        @Override
        public Collection<? extends ErrorDescription> createErrors(org.netbeans.spi.java.hints.HintContext ctx) {
            try {
                Method method = methodRef.get();

                if (method == null) {
                    methodRef.set(method = getMethod());
                }
                
                Object result = method.invoke(null, ctx);

                if (result == null) {
                    return null;
                }

                if (result instanceof Iterable) {
                    List<ErrorDescription> out = new LinkedList<>();

                    for (ErrorDescription ed : NbCollections.iterable(NbCollections.checkedIteratorByFilter(((Iterable) result).iterator(), ErrorDescription.class, false))) {
                        out.add(ed);
                    }

                    return out;
                }

                if (result instanceof ErrorDescription) {
                    return Collections.singletonList((ErrorDescription) result);
                }

                //XXX: log if result was ignored...
            } catch (IllegalAccessException | IllegalArgumentException | ClassNotFoundException | NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                boolean newOccurrence;
                
                synchronized (this) {
                    if (exceptionThrownFor == null) exceptionThrownFor = new WeakSet<>();
                    newOccurrence = exceptionThrownFor.add(ctx.getInfo().getFileObject());
                }
                
                //When a hint crashes with an exception, it is likely it will crash
                //again and again. There is no point in annoying the user with an exception dialog
                //after each reparse - throwing away all but the first exception:
                if (newOccurrence) {
                    LOG.log(Level.INFO, className + "." + methodName, ex);
                    //so that the exceptions are categorized better:
                    Exceptions.printStackTrace(ex.getCause());
                }
            }

            return null;
        }

        //used by tests:
        Method getMethod() throws NoSuchMethodException, ClassNotFoundException {
            return FSWrapper.resolveMethod(className, methodName);
        }

    }

    private static final class DelegatingCustomizerProvider implements CustomizerProvider {

        private final Class<? extends CustomizerProvider> component;

        public DelegatingCustomizerProvider(Class<? extends CustomizerProvider> component) {
            this.component = component;
        }

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            try {
                return component.getDeclaredConstructor().newInstance().getCustomizer(prefs);
            } catch (ReflectiveOperationException ex) {
                Exceptions.printStackTrace(ex);
            }

            return new JPanel();
        }

    }

}
