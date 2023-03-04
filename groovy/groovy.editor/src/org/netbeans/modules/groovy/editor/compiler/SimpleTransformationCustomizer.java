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
package org.netbeans.modules.groovy.editor.compiler;

import groovy.lang.GroovyClassLoader;
import groovy.transform.CompilationUnitAware;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.netbeans.modules.groovy.editor.api.parser.ApplyGroovyTransformation;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;

/**
 * Simple {@link ParsingCompilerCustomizer} implementation: adds or disables Groovy AST
 * transformations, based on contents of {@link #PARSING_COMPILER_CONFIG} config folder.
 * 
 * Files in the folder can be named:
 * <ul>
 * <li>as a fully qualified name of the class that implements ASTTransformation interface.
 * The file's extension determines what will be done (disable, enable).
 * <li>arbitrarily, the attributes {@code enable} and {@code disable} must list
 * fully qualified name(s) of class(es) affected
 * </ul>
 * In both cases {@code apply} String-valued attribute determines when the instruction is applied.
 * Currently valid names are:
 * <ul>
 * <li>index - applied during indexing
 * <li>parse - applied when parsing
 * </ul>
 * 
 * @author sdedic
 */
public final class SimpleTransformationCustomizer implements ParsingCompilerCustomizer {
    
    private static final Logger LOG = Logger.getLogger(SimpleTransformationCustomizer.class.getName());
    
    /**
     * Name of the folder that defines disabled transformations.
     */
    public static final String PARSING_COMPILER_CONFIG = "Parser/Transformations"; // NOI18N

    /**
     * Configuration for indexing
     */
    private final Cfg indexing = new Cfg();

    /**
     * Configuration for parsing
     */
    private final Cfg parsing = new Cfg();

    /**
     * Data holder: disabled transformations.
     */
    private static class Cfg {
        private final Set<String> disableTransformations = new HashSet<>();
        private final Collection<String> addTransformations = new LinkedHashSet<>();
    }
    
    private SimpleTransformationCustomizer() {
    }

    @Override
    public CompilerConfiguration configureParsingCompiler(Context ctx, CompilerConfiguration cfg) {
        Cfg c = GroovyUtils.isIndexingTask(ctx.getConsumerTask()) ? indexing : parsing;
        if (!c.disableTransformations.isEmpty()) {
            Set<String> disabled = cfg.getDisabledGlobalASTTransformations();
            if (disabled == null) {
                disabled = new HashSet<>();
            } else {
                disabled = new HashSet<>(disabled);
            }
            disabled.addAll(c.disableTransformations);
            cfg.setDisabledGlobalASTTransformations(disabled);
        }
        return cfg;
    }
    
    @Override
    public void decorateCompilation(Context ctx, CompilationUnit cu) {
        Cfg c = GroovyUtils.isIndexingTask(ctx.getConsumerTask()) ? indexing : parsing;
        GroovyClassLoader transformLoader = cu.getTransformLoader();
        Collection<String> clist = c.addTransformations;
        if (clist == null || clist.isEmpty()) {
            return;
        }
        for (String cn : clist) {
            try {
                // similar to Groovy ASTTransformationVisitor.addPhaseOperationsForGlobalTransforms
                Class<?> gTransClass = transformLoader.loadClass(cn, false, true, false);
                GroovyASTTransformation transformAnnotation = gTransClass.getAnnotation(GroovyASTTransformation.class);
                if (transformAnnotation == null) {
                    LOG.log(Level.WARNING, 
                        "Transform Class {0} is not annotated by {1}", new Object[] {
                            gTransClass.getName(), GroovyASTTransformation.class.getName()
                        });
                    continue;
                }
                if (ASTTransformation.class.isAssignableFrom(gTransClass)) {
                    ASTTransformation instance = (ASTTransformation) gTransClass.getDeclaredConstructor().newInstance();
                    if (instance instanceof CompilationUnitAware) {
                        ((CompilationUnitAware) instance).setCompilationUnit(cu);
                    }
                    CompilationUnit.ISourceUnitOperation suOp = source -> instance.visit(new ASTNode[]{source.getAST()}, source);
                    cu.addNewPhaseOperation(suOp, transformAnnotation.phase().getPhaseNumber());
                } else {
                    LOG.log(Level.WARNING, 
                        "Transform Class {0} is not ASTTransformation", gTransClass.getName());
                }
            } catch (CompilationFailedException | IllegalArgumentException | SecurityException | ReflectiveOperationException ex) {
                LOG.log(Level.WARNING, "Failed to load transformation: {0}", cn);
                LOG.log(Level.WARNING, "Class load failed with the following error:", ex);
            }
        }
    }
    
    /**
     * Modes to apply the setting. 
     */
    private static final String ATTR_MODE = "apply"; // NOI18N
    
    /**
     * Apply the setting for indexing
     */
    private static final String VALUE_MODE_INDEXING = ApplyGroovyTransformation.APPLY_INDEX; // NOI18N

    /**
     * Apply the setting for parsing
     */
    private static final String VALUE_MODE_PARSING = ApplyGroovyTransformation.APPLY_PARSE; // NOI18N
    
    /**
     * Attribute of the file, or its extension that disables a transformation.
     */
    private static final String ATTR_DISABLE_TRANSFORMATIONS = "disable"; // NOI18N
    
    /**
     * Enables/adds additional AST transformations.
     */
    private static final String ATTR_ENABLE_TRANSFORMATIONS = "enable"; // NOI18N

    public static SimpleTransformationCustomizer fromLayer(Map<String, Object> values) {
        SimpleTransformationCustomizer instance = new SimpleTransformationCustomizer();
        Object o = values.get(ATTR_MODE);
        boolean applied = false;
        if (o instanceof String) {
            boolean warn = false;
            A: for (String v : o.toString().split(",")) {
                v = v.trim();
                switch (v) {
                    case VALUE_MODE_INDEXING:
                        processMap(values, instance.indexing);
                        applied = true;
                        break;
                    case VALUE_MODE_PARSING:
                        processMap(values, instance.parsing);
                        applied = true;
                        break;
                    default:
                        applied = true;
                        if (!warn) {
                            warn = true;
                            LOG.log(Level.WARNING, "Unexpected value of 'apply': {1}", v);
                        }
                        break;
                }
            }
        } else if (o != null) {
            LOG.log(Level.WARNING, "Unexpected contents of 'apply': {0}", o);
            applied = true;
        }
        if (!applied) {
            // by default apply for parsing.
            processMap(values, instance.parsing);
        }
        return instance;
    }
    
    static void processMap(Map<String, Object> map, Cfg target) {
        Collection<String> disable = findNames(map.get(ATTR_DISABLE_TRANSFORMATIONS));
        if (disable != null) {
            target.disableTransformations.addAll(disable);
            // in addition, remove layer-registered transformations.
            target.addTransformations.removeAll(disable);
        }
        
        Collection<String> enable = findNames(map.get(ATTR_ENABLE_TRANSFORMATIONS));
        if (enable != null) {
            target.addTransformations.addAll(enable);
        }
        
        if (disable == null && enable == null) {
            LOG.log(Level.WARNING, "Entry does not disable or enable transformation: {0}", map);
        }
    }

    static Collection<String> findNames(Object o) {
        Set<String> dtnames = new LinkedHashSet<>();
        if (o instanceof Collection) {
            dtnames.addAll((Collection)o);
        } else if (o instanceof String) {
            for (String s : o.toString().split(",")) { // NOI18N
                s = s.trim();
                if (!s.isEmpty()) {
                    dtnames.add(s);
                }
            }
        } else {
            return null;
        }
        return dtnames;
    }
}
