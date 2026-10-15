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

package org.netbeans.modules.gototest;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.gototest.TestOppositesLocator;
import org.netbeans.api.gototest.TestOppositesLocator.Location;
import org.netbeans.api.gototest.TestOppositesLocator.LocatorResult;
import org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer;
import org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * Action which jumps to the opposite test file given a current file.
 * This action delegates to specific framework implementations (JUnit, Ruby etc.)
 * which perform logic appropriate for the file type being opened.
 * <p>
 * Much of this is based on the original JUnit action by Marian Petras.
 * 
 * @author  Marian Petras
 * @author Tor Norbye
 */
public class GotoOppositeAction extends CallableSystemAction {

    public GotoOppositeAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        // Not sure what the following is used for - a grep for trimmed-text
        // doesn't reveal any clients. Obsolete code perhaps?
        String trimmedName = NbBundle.getMessage(
                GotoOppositeAction.class,
                "LBL_Action_GoToTest_trimmed"); //NOI18N
        putValue("trimmed-text", trimmedName); //NOI18N
    }
    
    @Override
    @NbBundle.Messages("LBL_Action_GoToTestOrTestedClass=&Go to Test/Tested class")
    public String getName() {
        return Bundle.LBL_Action_GoToTestOrTestedClass();
    }
    
    @Override
    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        EditorCookie ec =  Utilities.actionsGlobalContext().lookup(EditorCookie.class);
        if (ec == null || ec.getDocument() == null) {
            return false;
        }
        return true;
    }

    public HelpCtx getHelpCtx() {
        // TODO - delegate to file locators!
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void initialize () {
	super.initialize ();
        putProperty(Action.SHORT_DESCRIPTION,
                    NbBundle.getMessage(getClass(),
                                        "HINT_Action_GoToTest"));       //NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public void performAction() {
        int caretOffsetHolder[] = { -1 };
        final FileObject fo = getApplicableFileObject(caretOffsetHolder);
        final int caretOffset = caretOffsetHolder[0];

        if (fo != null) {
            RequestProcessor RP = new RequestProcessor(GotoOppositeAction.class.getName());

            RP.post(new Runnable() {

                @Override
                public void run() {
                    LocatorResult opposites;

                    try {
                        opposites = TestOppositesLocator.getDefault().findOpposites(fo, caretOffset).get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                        return ;
                    }

                    if (opposites.getErrorMessage() != null) {
                        StatusDisplayer.getDefault().setStatusText(opposites.getErrorMessage());
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                TestCreatorPanelDisplayer.getDefault().displayPanel(UICommonUtils.getFileObjectsFromNodes(TopComponent.getRegistry().getActivatedNodes()), null, null);
                            }
                        });
                    } else {
                        opposites.getProviderErrors()
                                 .stream()
                                 .forEach(msg -> {
                                     DialogDisplayer.getDefault().notify(
                                             new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                                     });
                        Collection<? extends Location> locations = opposites.getLocations();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (locations.size() == 1) {
                                    handleResult(locations.iterator().next());
                                } else if (locations.size() > 1) {
                                    showPopup(fo, locations);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @NbBundle.Messages("LBL_PickExpression=Go to Test")
    private void showPopup(FileObject fo, Collection<? extends Location> locations) {
        JTextComponent pane;
        Point l = new Point(-1, -1);

        try {
            EditorCookie ec = fo.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                pane = NbDocument.findRecentEditorPane(ec);
                Rectangle pos = pane.modelToView(pane.getCaretPosition());
                l = new Point(pos.x + pos.width, pos.y + pos.height);
                SwingUtilities.convertPointToScreen(l, pane);

                String label = Bundle.LBL_PickExpression();
                PopupUtil.showPopup(new OppositeCandidateChooser(this, label, locations), label, l.x, l.y, true, -1);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(GotoOppositeAction.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    public void handleResult(Location opposite) {
        FileObject fileObject = opposite.getFileObject();
        if (fileObject != null) {
            NbDocument.openDocument(fileObject, opposite.getOffset(), Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        }
    }
    
    private FileObject getApplicableFileObject(int[] caretPosHolder) {
        if (!EventQueue.isDispatchThread()) {
            // Unsafe to ask for an editor pane from a random thread.
            // E.g. org.netbeans.lib.uihandler.LogRecords.write asking for getName().
            Collection<? extends FileObject> dobs = Utilities.actionsGlobalContext().lookupAll(FileObject.class);
            return dobs.size() == 1 ? dobs.iterator().next() : null;
        }

        // TODO: Use the new editor library to compute this:
        // JTextComponent pane = EditorRegistry.lastFocusedComponent();

        TopComponent comp = TopComponent.getRegistry().getActivated();
        if(comp == null) {
            return null;
        }
        Node[] nodes = comp.getActivatedNodes();
        if (nodes != null && nodes.length == 1) {
            if (comp instanceof CloneableEditorSupport.Pane) { //OK. We have an editor
                EditorCookie ec = nodes[0].getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    JEditorPane editorPane = NbDocument.findRecentEditorPane(ec);
                    if (editorPane != null) {
                        if (editorPane.getCaret() != null) {
                                caretPosHolder[0] = editorPane.getCaret().getDot();
                        }
                        Document document = editorPane.getDocument();
                        return Source.create(document).getFileObject();
                    }
                }
            } else {
                return UICommonUtils.getFileObjectFromNode(nodes[0]);
            }
        }
        
        return null;
    }
}
