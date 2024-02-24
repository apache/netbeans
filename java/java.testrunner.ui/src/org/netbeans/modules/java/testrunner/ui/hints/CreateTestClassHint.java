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
package org.netbeans.modules.java.testrunner.ui.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_CreateTestClassHint", description = "#DESC_CreateTestClassHint", category = "suggestions", hintKind = Hint.Kind.ACTION, severity = Severity.HINT)
@Messages({
    "DN_CreateTestClassHint=Create Test Class",
    "DESC_CreateTestClassHint=Create a test class for the selected source class."
})
public class CreateTestClassHint {

    @TriggerTreeKind(Tree.Kind.CLASS)
    @Messages("ERR_CreateTestClassHint=Create Test Class")
    public static ErrorDescription computeWarning(HintContext context) {
	TreePath tp = context.getPath();
        ClassTree cls = (ClassTree) tp.getLeaf();
        CompilationInfo info = context.getInfo();
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        int startPos = (int) sourcePositions.getStartPosition(tp.getCompilationUnit(), cls);
        int caret = context.getCaretLocation();
        String code = context.getInfo().getText();
	if (startPos < 0 || caret < 0 || caret < startPos || caret >= code.length()) {
            return null;
        }

        String headerText = code.substring(startPos, caret);
        int idx = headerText.indexOf('{'); //NOI18N
        if (idx >= 0) {
            return null;
        }

        ClassPath cp = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject fileObject = info.getFileObject();
        if(!fileObject.isValid()) { //File is probably deleted
            return null;
        }
        FileObject root = cp.findOwnerRoot(fileObject);
        if (root == null) { //File not part of any project
            return null;
        }

	Collection<? extends TestCreatorProvider> providers = Lookup.getDefault().lookupAll(TestCreatorProvider.class);
        Map<Object, List<String>> validCombinations = Utils.getValidCombinations(info, null);
        if (validCombinations == null) { // no TestCreatorProvider found
            return null;
        }
        for (TestCreatorProvider provider : providers) {
            if (provider.enable(new FileObject[]{fileObject}) && !validCombinations.isEmpty()) {
                List<Fix> fixes = new ArrayList<Fix>();
                Fix fix;
                for (Entry<Object, List<String>> entrySet : validCombinations.entrySet()) {
                    Object location = entrySet.getKey();
                    for (String testingFramework : entrySet.getValue()) {
                        fix = new CreateTestClassFix(new FileObject[]{fileObject}, location, testingFramework);
                        fixes.add(fix);
                    }
                }
                validCombinations.clear();
                return ErrorDescriptionFactory.forTree(context, context.getPath(), Bundle.ERR_CreateTestClassHint(), fixes.toArray(new Fix[0]));
            }
        }
        validCombinations.clear();
	return null;
    }

    private static final class CreateTestClassFix implements Fix {
	FileObject[] activatedFOs;
	Object location;
	String testingFramework;

	public CreateTestClassFix(FileObject[] activatedFOs, Object location, String testingFramework) {
	    this.activatedFOs = activatedFOs;
	    this.location = location;
	    this.testingFramework = testingFramework;
	}

	@Override
	@Messages({
	    "# {0} - the testing framework to be used, e.g. JUnit, TestNG,...",
	    "# {1} - the location where the test class will be created",
	    "FIX_CreateTestClassHint=Create Test Class [{0} in {1}]"})
	public String getText() {
	    return Bundle.FIX_CreateTestClassHint(testingFramework, Utils.getLocationText(location));
	}

	@Override
	public ChangeInfo implement() throws Exception {
	    TestCreatorPanelDisplayer.getDefault().displayPanel(activatedFOs, location, testingFramework);
	    return null;
	}
    }
}
