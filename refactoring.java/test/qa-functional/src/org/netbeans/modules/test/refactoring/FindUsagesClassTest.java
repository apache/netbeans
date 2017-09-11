/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.test.refactoring;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.test.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesDialogOperator;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;


/**
 <p>
 @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class FindUsagesClassTest extends FindUsagesTestCase {


	public FindUsagesClassTest(String name){
		super(name);
	}

	public static Test suite(){
		return JellyTestCase.emptyConfiguration().
				addTest(FindUsagesClassTest.class, "testFUClass").
				addTest(FindUsagesClassTest.class, "testSearchInComments").
				addTest(FindUsagesClassTest.class, "testFUDirectSubClass").
				addTest(FindUsagesClassTest.class, "testFUSubClass").
				//				 addTest(FindUsagesClassTest.class, "testPersistence").
				//				 addTest(FindUsagesClassTest.class, "testCollapseTree").
				//				 addTest(FindUsagesClassTest.class, "testShowLogical").
				//				 addTest(FindUsagesClassTest.class, "testNext").
				//				 addTest(FindUsagesClassTest.class, "testPrev").
				addTest(FindUsagesClassTest.class, "testOpenOnSelecting").
				addTest(FindUsagesClassTest.class, "testCancel").
				addTest(FindUsagesClassTest.class, "testTabNamesClass").
				suite();
	}


	public void testFUClass(){
		findUsages("fu", "FUClass", 48, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
	}

	public void testSearchInComments(){
		findUsages("fu", "SubtypeC", 50, 19, FIND_USAGES | SEARCH_IN_COMMENTS);
	}

	public void testFUDirectSubClass(){
		findUsages("fu", "FindSubtype", 47, 15, FIND_DIRECT_SUBTYPES_ONLY | SEARCH_IN_COMMENTS);
	}

	public void testFUSubClass(){
		findUsages("fu", "FindSubtype", 47, 15, FIND_ALL_SUBTYPES | SEARCH_IN_COMMENTS);
	}

	/*
	public void testPersistence(){
		String fileName = "FUClass";
		openSourceFile("fu", fileName);
		EditorOperator editor = new EditorOperator(fileName);
		editor.setCaretPosition(48, 19);
		editor.select(48, 19, 20);
		new FindUsagesAction().performPopup(editor);

		FindUsagesDialogOperator fuDialog = new FindUsagesDialogOperator();
		fuDialog.getSearchInComments().setSelected(false);
		fuDialog.getFindDirectSubtypesOnly().setSelected(false);
		fuDialog.setScope(null);
		fuDialog.find();
		new EventTool().waitNoEvent(5000);

		RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
		result.refresh();

		fuDialog = new FindUsagesDialogOperator();
		ref(fuDialog.getSearchInComments().isSelected());
		ref(fuDialog.getFindDirectSubtypesOnly().isSelected());
		ref(fuDialog.getSelectedScopeItem());
		fuDialog.getSearchInComments().setSelected(true);
		fuDialog.getFindUsages().setSelected(true);
		fuDialog.setScope(REFACTORING_TEST);
		fuDialog.find();
		new EventTool().waitNoEvent(2000);

		result = RefactoringResultOperator.getFindUsagesResult();
		result.refresh();

		fuDialog = new FindUsagesDialogOperator();
		ref(fuDialog.getSearchInComments().isSelected());
		ref(fuDialog.getFindUsages().isSelected());
		ref(fuDialog.getSelectedScopeItem());
		fuDialog.cancel();
		result.close();
	}


	public void testCollapseTree(){
		browseChild = false;
		findUsages("fu", "FUClass", 48, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;

		RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
		ref(result.getPreviewTree().getRowCount());
		result.collapse();
		new EventTool().waitNoEvent(1000);

		ref(result.getPreviewTree().getRowCount());
		result.collapse();
		new EventTool().waitNoEvent(1000);

		ref(result.getPreviewTree().getRowCount());
	}

	public void testShowLogical(){
		browseChild = false;
		findUsages("fu", "FUClass", 48, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;
		RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();

		result.logical();
		new EventTool().waitNoEvent(2000);

		ref(result.physicalIsSelected());
		browseRoot(result.getPreviewTree());
		result.physical();
		new EventTool().waitNoEvent(2000);

		ref(result.physicalIsSelected());
		browseRoot(result.getPreviewTree());
		result.logical();
		new EventTool().waitNoEvent(2000);
	}


	public void testNext(){        // Unstable, ordering can be different
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		EditorOperator.closeDiscardAll();
		browseChild = false;
		findUsages("fu", "FindSubtype", 47, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;
		try {
			RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();

			if (!result.physicalIsSelected()) {
				result.physical();
			}
			JTreeOperator preview = new JTreeOperator(result.getPreviewTree());
			preview.selectRow(0);

			for (int i = 0; i < 5; i++) {
				result.next();
				new EventTool().waitEvent(2000);

				String file = getFileForSelectedNode(preview).replaceAll(".java", "");
				EditorOperator edt = new EditorOperator(file);
				String txt = edt.txtEditorPane().getSelectionStart() + " " + edt.txtEditorPane().getSelectionEnd() + " " + edt.txtEditorPane().getSelectedText();
				if (map.get(file) == null) {
					map.put(file, new LinkedList<String>());
				}
				map.get(file).add(txt);
			}
			refMap(map);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}
	
	public void testPrev(){        // Unstable, ordering can be different
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		EditorOperator.closeDiscardAll();

		browseChild = false;
		findUsages("fu", "FindSubtype", 47, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;

		try {
			RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
			if (!result.physicalIsSelected()) {
				result.physical();
			}
			JTreeOperator preview = new JTreeOperator(result.getPreviewTree());
			preview.selectRow(0);

			for (int i = 0; i < 5; i++) {
				result.previous();
				new EventTool().waitEvent(2000);

				String file = getFileForSelectedNode(preview).replaceAll(".java", "");
				EditorOperator edt = new EditorOperator(file);
				String txt = edt.txtEditorPane().getSelectionStart() + " " + edt.txtEditorPane().getSelectionEnd() + " " + edt.txtEditorPane().getSelectedText();
				if (map.get(file) == null) {
					map.put(file, new LinkedList<String>());
				}
				map.get(file).add(txt);
			}
			refMap(map);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}
	*/
	public void testOpenOnSelecting(){
		browseChild = false;
		findUsages("fu", "FindSubtype", 47, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;

		RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
		if (!result.physicalIsSelected()) {
			result.physical();
		}
		JTreeOperator preview = new JTreeOperator(result.getPreviewTree());
		preview.selectRow(7);
		new EventTool().waitNoEvent(2000);

		String file = getFileForSelectedNode(preview);
		TreePath selectionPath = preview.getSelectionPath();
		preview.clickOnPath(selectionPath, 2);
		new EventTool().waitNoEvent(2000);

		EditorOperator edt = new EditorOperator(file);
		edt.verify();
	}

	public void testCancel(){
		browseChild = false;
		findUsages("fu", "FUClass", 48, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;

		RefactoringResultOperator furo = RefactoringResultOperator.getFindUsagesResult();
		int tabCount = furo.getTabCount();
		EditorOperator editor = new EditorOperator("FUClass");
		editor.setCaretPosition(48, 19);
		editor.select(48, 19, 19);
		new FindUsagesAction().performPopup(editor);

		FindUsagesDialogOperator findUsagesClassOperator = new FindUsagesDialogOperator();
		findUsagesClassOperator.cancel();
		assertEquals(furo.getTabCount(), tabCount);
	}

	public void testTabNamesClass(){
		browseChild = false;
		findUsages("fu", "FUClass", 48, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		findUsages("fu", "FUClass", 48, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
		browseChild = true;
		RefactoringResultOperator furo = RefactoringResultOperator.getFindUsagesResult();
		JTabbedPaneOperator tabbedPane = furo.getTabbedPane();
		assertNotNull(tabbedPane);
		String title = tabbedPane.getTitleAt(tabbedPane.getTabCount() - 1);
		ref(title);
	}

}
