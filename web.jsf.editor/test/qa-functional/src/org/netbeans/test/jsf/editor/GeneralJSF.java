/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.jsf.editor;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Riha
 */
public class GeneralJSF extends J2eeTestCase {

    protected EventTool evt;
    public static String current_project = "";

    public GeneralJSF(String arg0) {
        super(arg0);
        this.evt = new EventTool();
    }

    public void openProject(String projectName) throws IOException {
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(projectName);
        waitScanFinished();
    }

    /**
     * Opens file in editor
     *
     * @param pathAndFileName relative path to Web Pages in Projects window,
     * e.g. for Web Pages/web/index.html it would be "web|index.html" (| is path
     * separator)
     * @param projectName project name
     */
    public void openFile(String pathAndFileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralJSF.class.getName()).log(Level.INFO, "Opening file {0}", pathAndFileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Web Pages|" + pathAndFileName);
        evt.waitNoEvent(1000);

        if (node.isLeaf()) {
            node.select();
            node.performPopupAction("Open");
        }
    }

    public void type(EditorOperator edit, String code) {
        int iLimit = code.length();
        for (int i = 0; i < iLimit; i++) {
            edit.typeKey(code.charAt(i));
        }
        evt.waitNoEvent(100);
    }

    protected CompletionInfo getCompletion() {
        CompletionInfo result = new CompletionInfo();
        result.listItself = null;
        int iRedo = 10;
        while (true) {
            try {
                result.listItself = new CompletionJListOperator();
                try {
                    result.listItems = result.listItself.getCompletionItems();
                    Object o = result.listItems.get(0);
                    if (!o.toString().contains("Scanning in progress...")) {
                        return result;
                    }
                    evt.waitNoEvent(1000);
                } catch (java.lang.Exception ex) {
                    return null;
                }
            } catch (JemmyException ex) {
                System.out.println("Wait completion timeout.");
                ex.printStackTrace();
                if (0 == --iRedo) {
                    return null;
                }
            }
        }
    }

    protected Object[] getAnnotations(EditorOperator eOp, int limit) {
        eOp.makeComponentVisible();
        evt.waitNoEvent(1000);
        try {
            final EditorOperator eo = new EditorOperator(eOp.getName());
            final int _limit = limit;
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object oper) {
                    return eo.getAnnotations().length > _limit ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait parser annotations."); // NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        Object[] anns = eOp.getAnnotations();
        return anns;
    }

    protected void checkCompletionItems(CompletionJListOperator jlist, String[] asIdeal) {
        String completionList = "";
        StringBuilder sb = new StringBuilder(":");
        for (String sCode : asIdeal) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 == iIndex) {
                sb.append(sCode).append(",");
                if (completionList.length() < 1) {
                    try {
                        List list = jlist.getCompletionItems();
                        for (int i = 0; i < list.size(); i++) {

                            completionList += list.get(i) + "\n";
                        }
                    } catch (java.lang.Exception ex) {
                        System.out.println("#" + ex.getMessage());
                    }
                }
            }
        }
        if (sb.toString().length() > 1) {
            fail("Unable to find items " + sb.toString() + ". Completion list is " + completionList);
        }
    }
    
    protected void checkCompletionItemsJSF(CompletionJListOperator jlist, String[] asIdeal, int maxItems) throws Exception {
        Set<String> actual = new HashSet<String>();
        List list = jlist.getCompletionItems();
        StringBuilder suggestions = new StringBuilder();
        String _t;
        for (int i = 0; i < list.size() && i < maxItems; i++) {
            if (list.get(i) instanceof org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem) {
                _t = ((org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem) list.get(i)).getItemText();
                actual.add(_t);
                suggestions.append(_t).append(",");
            } else {
                actual.add(list.get(i).toString());
                suggestions.append(list.get(i).toString()).append(",");
            }
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < asIdeal.length; i++) {
            if (!actual.contains(asIdeal[i])) {
                sb.append(asIdeal[i]).append(",");
            }

        }
        String result = sb.toString();
        assertTrue("Completion does not contain items: " + result + " in list " + suggestions, result.length() == 0);
    }

    protected void checkCompletionDoesntContainItems(CompletionJListOperator jlist, String[] invalidList) {
        for (String sCode : invalidList) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 != iIndex) {
                fail("Completion list contains invalid item:" + sCode);
            }
        }
    }

    protected void checkCompletionItems(
            CompletionInfo jlist,
            String[] asIdeal) {
        checkCompletionItems(jlist.listItself, asIdeal);
    }

    public class CFulltextStringComparator implements Operator.StringComparator {

        public boolean equals(java.lang.String caption, java.lang.String match) {
            return caption.equals(match);
        }
    }

    public class CStartsStringComparator implements Operator.StringComparator {

        public boolean equals(java.lang.String caption, java.lang.String match) {
            return caption.startsWith(match);
        }
    }

    protected class CompletionInfo {

        public CompletionJListOperator listItself;
        public List listItems;

        public int size() {
            return listItems.size();
        }

        public void hideAll() {
            CompletionJListOperator.hideAll();
        }
    }
    
     protected void clearLine(EditorOperator eo) {
        eo.deleteLine(eo.getLineNumber());
        eo.pressKey(KeyEvent.VK_ENTER);
    }
}
