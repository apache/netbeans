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
package org.netbeans.modules.java.hints.declarative.idebinding;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.hints.declarative.Condition;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.FixTextDescription;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.HintTextDescription;
import org.netbeans.modules.java.hints.spiimpl.Hacks;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class HintsTask extends ParserResultTask<Result> {

    @Override
    public void run(final Result result, SchedulerEvent event) {
        final DeclarativeHintsParser.Result res = ParserImpl.getResult(result);
        final List<ErrorDescription> errors;

        FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        if (res != null) {
            errors = computeErrors(res, result.getSnapshot().getText(), fileObject);
        } else {
            errors = Collections.emptyList();
        }

        HintsController.setErrors(fileObject, HintsTask.class.getName(), errors);
    }

    static List<ErrorDescription> computeErrors(@NonNull final DeclarativeHintsParser.Result res, @NonNull final CharSequence hintCode, @NonNull final FileObject file) {
        final List<ErrorDescription> errors = new LinkedList<>();

        errors.addAll(res.errors);

        ClasspathInfo cpInfo = ClasspathInfo.create(file);
        
        try {
            FileObject scratch = FileUtil.createMemoryFileSystem().getRoot().createData("Scratch.java");
            
            JavaSource.create(cpInfo, scratch).runUserActionTask((CompilationController parameter) -> {

                parameter.toPhase(Phase.RESOLVED);
                String[] importsArray = res.importsBlock != null ?
                        new String[] {hintCode.subSequence(res.importsBlock[0], res.importsBlock[1]).toString()} : new String[0];
                
                for (HintTextDescription hd : res.hints) {

                    String code = hintCode.subSequence(hd.textStart, hd.textEnd).toString();
                    Collection<Diagnostic<? extends JavaFileObject>> parsedErrors = new LinkedList<>();
                    Scope s = Utilities.constructScope(parameter, conditions2Constraints(parameter, hd.conditions), Arrays.asList(importsArray));
                    Tree parsed = Utilities.parseAndAttribute(parameter, code, s, parsedErrors);

                    for (Diagnostic<? extends JavaFileObject> d : parsedErrors) {
                        errors.add(ErrorDescriptionFactory.createErrorDescription(
                                        d.getKind() == Kind.ERROR ? Severity.ERROR : Severity.WARNING,
                                        d.getMessage(null),
                                        file,
                                        (int) (hd.textStart + d.getStartPosition()),
                                        (int) (hd.textStart + d.getEndPosition())));
                    }
                    
                    if (parsed != null && ExpressionTree.class.isAssignableFrom(parsed.getKind().asInterface())) {
                        TypeMirror type = parameter.getTrees().getTypeMirror(new TreePath(new TreePath(parameter.getCompilationUnit()), parsed));
                        
                        if (type != null && !VOID_LIKE.contains(type.getKind())) {
                            for (FixTextDescription df : hd.fixes) {
                                String fixCode = hintCode.subSequence(df.fixSpan[0], df.fixSpan[1]).toString().trim();
                                
                                if (fixCode.isEmpty()) {
                                    errors.add(ErrorDescriptionFactory.createErrorDescription(
                                                    Severity.WARNING,
                                                    NbBundle.getMessage(HintsTask.class, "ERR_RemoveExpression"),
                                                    file,
                                                    hd.textStart,
                                                    hd.textEnd));
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return errors;
    }
    
    private static Map<String, TypeMirror> conditions2Constraints(CompilationInfo info, List<Condition> conditions) {
        Map<String, TypeMirror> constraints = new HashMap<>();

        for (Entry<String, String> e : org.netbeans.modules.java.hints.declarative.Utilities.conditions2Constraints(conditions).entrySet()) {
            TypeMirror designedType = Hacks.parseFQNType(info, e.getValue());

            if (designedType == null || designedType.getKind() == TypeKind.ERROR) {
                continue ;
            }

            constraints.put(e.getKey(), designedType);
        }
        
        return constraints;
    }
    
    private static final Set<TypeKind> VOID_LIKE = EnumSet.of(TypeKind.VOID, TypeKind.ERROR, TypeKind.OTHER);
    
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    @MimeRegistration(mimeType=DeclarativeHintTokenId.MIME_TYPE, service=TaskFactory.class)
    public static final class FactoryImpl extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new HintsTask());
        }
        
    }

}
