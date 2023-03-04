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
package org.netbeans.modules.javafx2.editor.parser;

import org.netbeans.modules.javafx2.editor.completion.model.FxTreeUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;
import org.netbeans.modules.javafx2.editor.ErrorReporter;
import org.netbeans.modules.javafx2.editor.completion.model.FxInclude;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.javafx2.editor.parser.processors.EventResolver;
import org.netbeans.modules.javafx2.editor.parser.processors.ImportProcessor;
import org.netbeans.modules.javafx2.editor.parser.processors.IncludeResolver;
import org.netbeans.modules.javafx2.editor.parser.processors.NamedInstancesCollector;
import org.netbeans.modules.javafx2.editor.parser.processors.PropertyResolver;
import org.netbeans.modules.javafx2.editor.parser.processors.ReferenceResolver;
import org.netbeans.modules.javafx2.editor.parser.processors.ScriptResolver;
import org.netbeans.modules.javafx2.editor.parser.processors.TypeResolver;
import org.netbeans.modules.javafx2.editor.parser.processors.ValueChecker;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXException;

/**
 *
 * @author sdedic
 */
class FxmlParser extends Parser implements ErrorReporter {
    private FxmlParserResult result;
    
    private Collection<ErrorMark>   problems = new ArrayList<ErrorMark>();
    
    private FxModel model;
    
    private CompilationInfo info;
    
    private Snapshot snapshot;
    
    private BuildEnvironment env;
    
    private List<ModelBuilderStep> steps;

    @Override
    public void addError(ErrorMark em) {
        problems.add(em);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        this.snapshot = snapshot;
        TokenHierarchy<?> h = (TokenHierarchy<?>)snapshot.getTokenHierarchy();
        XmlLexerParser tokenParser = new XmlLexerParser(h);
        FxModelBuilder builder = new FxModelBuilder();
        
        FileObject fo = snapshot.getSource().getFileObject();
        if (fo != null) {
            builder.setBaseURL(fo.toURL());
        }
        
        tokenParser.setContentHandler(builder);
        
        try {
            tokenParser.parse();
        } catch (SAXException ex) {
            throw new ParseException("Parsing failed", ex);
        }
        
        final ClasspathInfo cpInfo = ClasspathInfo.create(snapshot.getSource().getFileObject());
        
        problems.addAll(tokenParser.getErrors());
        problems.addAll(builder.getErrors());
        model = builder.getModel();
        
        class UT extends UserTask implements ClasspathInfoProvider {

            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                JavacParserResult res = (JavacParserResult)resultIterator.getParserResult();
                info = res.get(CompilationInfo.class);
                env = createBuildEnvironment();
                initModelSteps();

                for (ModelBuilderStep step : steps) {
                    FxNodeVisitor visitor = step.createVisitor(env);
                    model.accept(visitor);
                }
            }
        };
        
        UT ut = new UT();
        
        ParserManager.parse("text/x-java", ut);
        
        result = new ResultImpl(snapshot, model, problems, h);
        
        env = null;
        steps = null;
        snapshot = null;
        model = null;
        info = null;
        problems = new ArrayList<ErrorMark>();
    }
    
    private static final class ResultImpl extends FxmlParserResult {
        private ImportProcessor importProcessor;
        
        public ResultImpl(Snapshot _snapshot, FxModel sourceModel, Collection<ErrorMark> problems, TokenHierarchy<?>  h) {
            super(_snapshot, sourceModel, problems, h);
        }

        @Override
        public FxNewInstance resolveInstance(FxInclude include) {
            // FIXME
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        protected FxTreeUtilities createTreeUtilities() {
            return new FxTreeUtilities(ModelAccessor.INSTANCE, getSourceModel(), getTokenHierarchy());
        }

        @Override
        public Set<String> resolveClassName(CompilationInfo info, String className) {
            if (importProcessor == null) {
                importProcessor = new ImportProcessor(getTokenHierarchy(), null, getTreeUtilities());
                importProcessor.load(info, getSourceModel());
            }
            return importProcessor.resolveTypeName(info, className);
        }
    }
    
    private void initModelSteps() {
        steps = new ArrayList<ModelBuilderStep>();
        steps.add(new NamedInstancesCollector());
        steps.add(new IncludeResolver());
        steps.add(new ScriptResolver());
        steps.add(new TypeResolver());
        steps.add(new ReferenceResolver());
        steps.add(new PropertyResolver());
        steps.add(new EventResolver());
        steps.add(new ValueChecker());
    }
    
    @SuppressWarnings("unchecked")
    private BuildEnvironment createBuildEnvironment() {
        BuildEnvironment env = new BuildEnvironment();
        env.setAccessor(ModelAccessor.INSTANCE);
        env.setBeanProvider(FxBean.getBeanProvider(info));
        env.setCompilationInfo(info);
        env.setHierarchy((TokenHierarchy<XMLTokenId>)snapshot.getTokenHierarchy());
        env.setModel(model);
        env.setReporter(this);
        env.setTreeUtilities(new FxTreeUtilities(ModelAccessor.INSTANCE, model, env.getHierarchy()));
        
        return env;
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
}
