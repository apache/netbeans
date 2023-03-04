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
package org.netbeans.modules.refactoring.java.spi.ui;

import javax.swing.Action;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Delegate which creates a refactoring UI and runs a refactoring.  To be
 * used by implementors of Java refactorings.
 * <p>
 * <b>Usage:</b><br>
 * Implement this class to provide a RefactoringUI for your refactoring.
 * Include a static method on your implementation which instantiates an
 * instance of your JavaRefactoringActionDelegate and passes it to
 * <code>JavaRefactoringActionDelegate.createAction()</code>.
 * <p>
 * Register an action in your module&lsquo;s XML Layer, in the folder for
 * the menu (toolbar button, keybinding, etc.) where you want it to appear -
 * typically <code>Menu/Refactorings</code>.  Set the
 * <code>instanceCreate</code> attribute to the fully qualified path to the
 * static method you created (which should return <code>javax.swing.Action</code>.
 * <p>
 * <b>Example Registration</b><br>
 * <pre>
 *  &lt;filesystem&gt;
 *    &lt;folder name=&quot;Menu&quot;&gt;
 *      &lt;folder name=&quot;Refactoring&quot;&gt;
 *        &lt;file name=&quot;MyRefactoring.shadow&quot;&gt;
 *          &lt;attr name=&quot;originalFile&quot;
 *                stringvalue=&quot;Actions/Refactoring/MyRefactoring.instance&quot;/&gt;
 *         &lt;/file&gt;
 *      &lt;/folder&gt;
 *    &lt;/folder&gt;
 *    &lt;folder name=&quot;Actions&quot;&gt;
 *      &lt;folder name=&quot;Refactoring&quot;&gt;
 *        &lt;file name=&quot;MyRefactoring.instance&quot;&gt;
 *          &lt;attr name=&quot;instanceCreate&quot; methodvalue=&quot;com.foo.MyClass.myStaticMethod&quot;/&gt;
 *        &lt;/file&gt;
 *      &lt;/folder&gt;
 *    &lt;/folder&gt;
 *  &lt;/filesystem&gt;
 *  </pre>
 * @author Tim Boudreau
 */
public abstract class JavaRefactoringActionDelegate {
    private final boolean requiresSelection;
    private final String name;

    /**
     * Create a new refactoring action delegate which does not require a
     * text selection, with the passed name.
     * @param name The display name of the action
     */
    protected JavaRefactoringActionDelegate(String name) {
        this (name, false);
    }

    /**
     * Create a new refactoring action delegate which may require a
     * text selection, with the passed name.
     * @param name The display name of the action
     * @param requiresSelection if true, isEnabled() will return false unless
     *        there is a text selection in the editor
     */
    protected JavaRefactoringActionDelegate(String name, boolean requiresSelection) {
        this.requiresSelection = requiresSelection;
        this.name = name;
    }

    /**
     * Create the refactoring UI which will be used to configure and invoke
     * the refactoring.
     * @return A refactoring UI, or null if the refactoring cannot be
     *         performed.  It is preferable not to do deep source analysis
     *         here, but to do that in the analysis phase, unless the
     *         analysis can be done very quickly.
     */
    public abstract RefactoringUI createRefactoringUI(
            TreePathHandle selectedElement,int startOffset,
            int endOffset, CompilationInfo info);

    /**
     * Get an error message to be shown to the user if the creaetRefactoringUI()
     * has just returned null.
     * @return an error message - by default, simply says that this refactoring
     *         cannot be performed
     */
    public String getErrorMessage() {
        return NbBundle.getMessage (JavaRefactoringActionDelegate.class,
                "MSG_CANNOT_PERFORM", name); //NOI18N
    }

    /**
     * Create an Action which should appear in menus to invoke this refactoring.
     * @param delegate An instance of JavaRefactoringActionDelegate which the
     *                 resulting action will call to create the refactoring ui.
     */
    public static Action createAction (JavaRefactoringActionDelegate delegate) {
        return new JavaRefactoringGlobalAction (delegate.name, delegate);
    }

    /**
     * Determine if the action should be enabled.  By default, this method
     * checks for the presence of an open editor;  if <code>requiresSelection</code>
     * was passed to the constructor, it also depends on a text selection in that
     * editor being present.
     * @param context A Lookup containing either an EditorCookie or one or more
     *                Nodes whose lookup contains an EditorCookie
     * @return true if the action should be enabled, false otherwise
     */
    protected boolean isEnabled(Lookup context) {
        EditorCookie ck = JavaRefactoringGlobalAction.getEditorCookie(context);
        boolean result = false;
        if(ck != null) {
            JEditorPane pane = NbDocument.findRecentEditorPane(ck);
            result = pane != null;
            if (requiresSelection) {
                result = result && (pane.getSelectionStart() != pane.getSelectionEnd());
            }
        }
        return result;
    }
}
