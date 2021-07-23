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
package org.netbeans.modules.groovy.support.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Position;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.project.SingleMethod;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType="text/x-groovy", service=ComputeTestMethods.class)
public class GroovyComputeTestMethods implements ComputeTestMethods {

    @Override
    public List<TestMethodController.TestMethod> computeTestMethods(Parser.Result parserResult, AtomicBoolean cancel) {
        List<TestMethodController.TestMethod> result = new ArrayList<>();
        if (cancel.get()) {
            return result;
        }
        String text = parserResult.getSnapshot().getText().toString();
        ModuleNode moduleNode = TestMethodUtil.extractModuleNode(parserResult);
        for (ClassNode classNode : moduleNode.getClasses()) {
            ClassNode superClass = classNode.getSuperClass();
            if ("spock.lang.Specification".equals(superClass.getName())) {
                int classStartLine = classNode.getLineNumber();
                int classStartColumn = classNode.getColumnNumber();
                int classOffset = classStartLine > 0 && classStartColumn > 0 ? getOffset(text, classStartLine, classStartColumn) : 0;
                for (MethodNode methodNode : classNode.getMethods()) {
                    for (AnnotationNode annotation : methodNode.getAnnotations()) {
                        if ("org.spockframework.runtime.model.FeatureMetadata".equals(annotation.getClassNode().getName())) {
                            int startLine = methodNode.getLineNumber();
                            int startColumn = methodNode.getColumnNumber();
                            int endLine = methodNode.getLastLineNumber();
                            int endColumn = methodNode.getLastColumnNumber();
                            if (startLine > 0 && startColumn > 0 && endLine > 0 && endColumn > 0) {
                                int startOffset = getOffset(text, startLine, startColumn);
                                int endOffset = getOffset(text, endLine, endColumn);
                                String name = annotation.getMember("name").getText();
                                result.add(new TestMethodController.TestMethod(classNode.getName(),
                                        new SimplePosition(classOffset),
                                        new SingleMethod(parserResult.getSnapshot().getSource().getFileObject(), name),
                                        new SimplePosition(startOffset),
                                        new SimplePosition(startOffset),
                                        new SimplePosition(endOffset)));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static int getOffset(String text, int lineNumber, int columnNumber) {
        int offset = 0;
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length && i < lineNumber; i++) {
            if (i < lineNumber - 1) {
                offset += lines[i].length() + 1;
            } else {
                offset += columnNumber - 1;
            }
        }
        return Math.min(offset, text.length());
    }

    private static class SimplePosition implements Position {

        private final int offset;

        private SimplePosition(int offset) {
            this.offset = offset;
        }

        @Override
        public int getOffset() {
            return offset;
        }
    }
}
