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
package org.netbeans.modules.php.editor.csl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import static org.netbeans.modules.csl.api.ElementKind.CLASS;
import static org.netbeans.modules.csl.api.ElementKind.INTERFACE;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.ParserTestBase;
import org.openide.filesystems.FileObject;

public abstract class OverridingMethodsImplTestBase extends ParserTestBase {

    public OverridingMethodsImplTestBase(String testName) {
        super(testName);
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        FileObject testFile = getTestFile("testfiles/" + filename + ".php");
        Source testSource = getTestSource(testFile);
        final PhpStructureScanner structureScanner = new PhpStructureScanner() {

            @Override
            protected boolean isResolveDeprecatedElements() {
                return true;
            }
        };
        // see ide/csl.api/src/org/netbeans/modules/csl/editor/overridden/ComputeAnnotations.java
        final OverridingMethodsImpl overridingMethods = new OverridingMethodsImpl();
        final Map<ElementHandle, Collection<? extends DeclarationFinder.AlternativeLocation>> overriding = new HashMap<>();
        final Map<ElementHandle, Collection<? extends DeclarationFinder.AlternativeLocation>> overridden = new HashMap<>();
        UserTask task = new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult info = (PHPParseResult) resultIterator.getParserResult();
                if (info != null) {
                    Set<ElementHandle> seen = new HashSet<>();
                    Map<ElementHandle, ElementHandle> node2Parent = new HashMap<>();
                    List<StructureItem> todo = new LinkedList<>(structureScanner.scan(info));
                    while (!todo.isEmpty()) {
                        StructureItem i = todo.remove(0);
                        if (StructureItem.isInherited(i)) {
                            continue;
                        }
                        todo.addAll(i.getNestedItems());
                        for (StructureItem nested : i.getNestedItems()) {
                            if (!node2Parent.containsKey(nested.getElementHandle())) {
                                node2Parent.put(nested.getElementHandle(), i.getElementHandle());
                            }
                        }
                        if (seen.add(i.getElementHandle())) {
                            if (i.getElementHandle().getKind() != ElementKind.CLASS && i.getElementHandle().getKind() != ElementKind.INTERFACE) {
                                Collection<? extends DeclarationFinder.AlternativeLocation> ov = overridingMethods.overrides(info, i.getElementHandle());
                                if (ov != null && !ov.isEmpty()) {
                                    overriding.put(i.getElementHandle(), ov);
                                }
                            }
                            if (overridingMethods.isOverriddenBySupported(info, i.getElementHandle())) {
                                Collection<? extends DeclarationFinder.AlternativeLocation> on = overridingMethods.overriddenBy(info, i.getElementHandle());
                                if (on != null && !on.isEmpty()) {
                                    overridden.put(i.getElementHandle(), on);
                                }
                            }
                        }
                    }
                }
            }
        };

        Map<String, ClassPath> classPaths = createClassPathsForTest();
        if (classPaths == null || classPaths.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }

        StringBuilder sb = new StringBuilder();
        if (!overriding.isEmpty()) {
            print(sb, overriding, true);
        }
        if (!overridden.isEmpty()) {
            print(sb, overridden, false);
        }
        return sb.toString();
    }

    private void print(StringBuilder sb, final Map<ElementHandle, Collection<? extends DeclarationFinder.AlternativeLocation>> overriding, boolean isOverriding) {
        if (sb.length() > 0) {
            sb.append("\n");
        }
        if (isOverriding) {
            sb.append("[Overrides]\n");
        } else {
            sb.append("[Overridden]\n");
        }
        Map<String, List<String>> overridingNames = new LinkedHashMap<>();
        overridingNames.put("Constant", new ArrayList<>());
        overridingNames.put("Field", new ArrayList<>());
        overridingNames.put("Method", new ArrayList<>());
        overridingNames.put("Type", new ArrayList<>());
        for (Map.Entry<ElementHandle, Collection<? extends DeclarationFinder.AlternativeLocation>> entry : overriding.entrySet()) {
            ElementHandle elementHandle = entry.getKey();
            Collection<? extends DeclarationFinder.AlternativeLocation> values = entry.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(elementHandle.getName()).append(" ");
            List<String> types = new ArrayList<>();
            for (DeclarationFinder.AlternativeLocation value : values) {
                switch (elementHandle.getKind()) {
                    case CLASS:
                    case INTERFACE:
                        types.add(value.getElement().getName());
                        break;
                    default:
                        types.add(value.getElement().getIn());
                        break;
                }
            }
            switch (elementHandle.getKind()) {
                case CLASS:
                case INTERFACE:
                    // noop
                    break;
                default:
                    stringBuilder.append(": ").append(elementHandle.getIn()).append(" ");
                    break;
            }
            Collections.sort(types);
            for (String fileName : types) {
                stringBuilder.append("(").append((isOverriding ? "from " : "by ")).append(fileName).append(")");
            }
            switch (elementHandle.getKind()) {
                case CONSTANT:
                    overridingNames.get("Constant").add(stringBuilder.toString());
                    break;
                case FIELD:
                    overridingNames.get("Field").add(stringBuilder.toString());
                    break;
                case METHOD:
                    overridingNames.get("Method").add(stringBuilder.toString());
                    break;
                case CLASS: // no break
                case INTERFACE:
                    overridingNames.get("Type").add(stringBuilder.toString());
                    break;
                default:
                    break;
            }
        }
        for (Map.Entry<String, List<String>> entry : overridingNames.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            sb.append("[").append(entry.getKey()).append("]\n");
            List<String> value = entry.getValue();
            Collections.sort(value);
            for (String name : value) {
                sb.append(name).append("\n");
            }
        }
    }

}
