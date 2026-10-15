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
package org.netbeans.test.syntax;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.Operator;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Riha
 */
public class GeneralJSP extends J2eeTestCase {

    protected EventTool evt;
    public static String current_project = "";
    public static String original_content;

    public GeneralJSP(String arg0) {
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
        Logger.getLogger(GeneralJSP.class.getName()).log(Level.INFO, "Opening file {0}", pathAndFileName);
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

    public void pressKey(EditorOperator file, int key, int numberOfTimes) {
        for (int i = 0; i < numberOfTimes; i++) {
            file.pressKey(key);
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

    protected void checkCompletionItemsJsp(CompletionJListOperator jlist, String[] asIdeal) throws Exception {
        checkCompletionItemsJsp(jlist, asIdeal, 20);
    }

    protected void checkCompletionItemsJsp(CompletionJListOperator jlist, String[] asIdeal, int maxItems) throws Exception {
        Set<String> actual = new HashSet<String>();
        List list = jlist.getCompletionItems();
        StringBuilder suggestions = new StringBuilder();
        String _t;
        for (int i = 0; i < list.size() && i < maxItems; i++) {
            if (list.get(i) instanceof org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem) {
                _t = ((org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem) list.get(i)).getItemText();
                actual.add(_t);
                suggestions.append(_t).append(",");
            } else if (list.get(i) instanceof org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem) {
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

    protected void checkCompletionDoesntContainItems(CompletionJListOperator jlist, String[] invalidList) {
        for (String sCode : invalidList) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 != iIndex) {
                fail("Completion list contains invalid item:" + sCode);
            }
        }
    }

    protected void checkCompletionMatchesPrefix(List list, String prefix) {
        StringBuilder sb = new StringBuilder();
        String item;
        for (int i = 0; i < list.size(); i++) {
            item = list.get(i).toString();
            if(!item.toLowerCase().startsWith(prefix) && !item.equalsIgnoreCase("$color_chooser")){
                sb.append(item).append("\n");
            }
        }

        if (sb.toString().length() > 1) {
            fail("Completion contains nonmatching items for prefix " + prefix + ". Completion list is " + sb.toString());
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
