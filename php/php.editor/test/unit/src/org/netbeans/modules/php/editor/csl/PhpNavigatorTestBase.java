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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.ParserTestBase;
import org.netbeans.modules.php.editor.parser.TestHtmlFormatter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class PhpNavigatorTestBase extends ParserTestBase {

    public PhpNavigatorTestBase(String testName) {
        super(testName);
    }

    private static final Comparator<StructureItem> STRUCTURE_ITEM_COMPARATOR = (StructureItem o1, StructureItem o2) -> {
        String path1 = o1.getElementHandle().getFileObject().getPath();
        String path2 = o2.getElementHandle().getFileObject().getPath();
        int result = path1.compareToIgnoreCase(path2);
        if (result == 0) {
            long position1 = o1.getPosition();
            long position2 = o2.getPosition();
            result = Long.compare(position1, position2);
        }
        return result;
    };

    @Override
    protected String getTestResult(String filename) throws Exception {
        StringBuilder sb = new StringBuilder();
        FileObject testFile = getTestFile("testfiles/" + filename + ".php");

        Source testSource = getTestSource(testFile);

        final PhpStructureScanner instance = new PhpStructureScanner() {

            @Override
            protected boolean isResolveDeprecatedElements() {
                return true;
            }
        };
        final List<StructureItem> result = new ArrayList<>();
        UserTask task = new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult info = (PHPParseResult) resultIterator.getParserResult();
                if (info != null) {
                    result.addAll(instance.scan(info));
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

        result.sort(STRUCTURE_ITEM_COMPARATOR);

        for (StructureItem structureItem : result) {
            structureItem.getNestedItems().sort(STRUCTURE_ITEM_COMPARATOR);
            sb.append(printStructureItem(structureItem, 0));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String printStructureItem(StructureItem structureItem, int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(indent));
        if (structureItem instanceof StructureItem.InheritedItem) {
            if (((StructureItem.InheritedItem) structureItem).isInherited()) {
                sb.append("(Inherited) ");
            }
        }
        sb.append(structureItem.getName());
        sb.append(" [");
        sb.append(structureItem.getPosition());
        sb.append(", ");
        sb.append(structureItem.getEndPosition());
        sb.append("] : ");
        HtmlFormatter formatter = new TestHtmlFormatter();
        sb.append(structureItem.getHtml(formatter));
        List<? extends StructureItem> nestedItems = structureItem.getNestedItems();
        nestedItems.sort(STRUCTURE_ITEM_COMPARATOR);
        for (StructureItem item : nestedItems) {
            sb.append("\n");
            sb.append(printStructureItem(item, indent + 1));
        }
        return sb.toString();
    }

    private String indent(int indent) {
        String text = "|-";
        for (int i = 0; i < indent; i++) {
            text = text + "-";
        }
        return text;
    }

}
