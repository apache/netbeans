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

package org.netbeans.modules.gototest;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer;
import org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.spi.gototest.TestLocator.FileType;
import org.netbeans.spi.gototest.TestLocator.LocationListener;
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
    private HashMap<LocationResult, String> locationResults = new HashMap<LocationResult, String>();
    private Semaphore lock;

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
    @NbBundle.Messages("No_Test_Or_Tested_Class_Found=No Test or Tested class found")
    public void performAction() {
        int caretOffsetHolder[] = { -1 };
        final FileObject fo = getApplicableFileObject(caretOffsetHolder);
        final int caretOffset = caretOffsetHolder[0];

        if (fo != null) {
            RequestProcessor RP = new RequestProcessor(GotoOppositeAction.class.getName());

            RP.post(new Runnable() {

                @Override
                public void run() {
                    FileType currentFileType = getCurrentFileType();
                    if(currentFileType == FileType.NEITHER) {
                        StatusDisplayer.getDefault().setStatusText(Bundle.No_Test_Or_Tested_Class_Found());
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                TestCreatorPanelDisplayer.getDefault().displayPanel(UICommonUtils.getFileObjectsFromNodes(TopComponent.getRegistry().getActivatedNodes()), null, null);
                            }
                        });
                    }
                    else {
                        populateLocationResults(fo, caretOffset);
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                if (locationResults.size() == 1) {
                                    handleResult(locationResults.keySet().iterator().next());
                                } else if (locationResults.size() > 1) {
                                    showPopup(fo);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void populateLocationResults(FileObject fo, int caretOffset) {
        locationResults.clear();

        Collection<? extends TestLocator> locators = Lookup.getDefault().lookupAll(TestLocator.class);

        int permits = 0;
        for (TestLocator locator : locators) {
            if (locator.appliesTo(fo)) {
                permits++;
            }
        }

        lock = new Semaphore(permits);
        try {
            lock.acquire(permits);
        } catch (InterruptedException e) {
        }

        for (TestLocator locator : locators) {
            if (locator.appliesTo(fo)) {
                doPopulateLocationResults(fo, caretOffset, locator);
            }
        }
        try {
            lock.acquire(permits);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void doPopulateLocationResults(FileObject fo, int caretOffset, TestLocator locator) {
        if (locator != null) {
            if (locator.appliesTo(fo)) {
                if (locator.asynchronous()) {
                    locator.findOpposite(fo, caretOffset, new LocationListener() {

                        @Override
                        public void foundLocation(FileObject fo, LocationResult location) {
                            if (location != null) {
                                FileObject fileObject = location.getFileObject();
                                if(fileObject == null) {
                                    String msg = location.getErrorMessage();
                                    if (msg != null) {
                                        DialogDisplayer.getDefault().notify(
                                                new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                                    }
                                } else {
                                    locationResults.put(location, fileObject.getName());
                                }
                            }
                            lock.release();
                        }
                    });
                } else {
                    LocationResult opposite = locator.findOpposite(fo, caretOffset);

                    if (opposite != null) {
                        FileObject fileObject = opposite.getFileObject();
                        if (fileObject == null) {
                            String msg = opposite.getErrorMessage();
                            if (msg != null) {
                                DialogDisplayer.getDefault().notify(
                                        new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                            }
                        } else {
                            locationResults.put(opposite, fileObject.getName());
                        }
                    }
                    lock.release();
                }
            }
        }
    }

    @NbBundle.Messages("LBL_PickExpression=Go to Test")
    private void showPopup(FileObject fo) {
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
                PopupUtil.showPopup(new OppositeCandidateChooser(this, label, locationResults), label, l.x, l.y, true, -1);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(GotoOppositeAction.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    public void handleResult(LocationResult opposite) {
        FileObject fileObject = opposite.getFileObject();
        if (fileObject != null) {
            NbDocument.openDocument(fileObject, opposite.getOffset(), Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        } else if (opposite.getErrorMessage() != null) {
            String msg = opposite.getErrorMessage();
            NotifyDescriptor descr = new NotifyDescriptor.Message(msg, 
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(descr);
        }
    }
    
    private TestLocator getLocatorFor(FileObject fo) {
        Collection<? extends TestLocator> locators = Lookup.getDefault().lookupAll(TestLocator.class);
        for (TestLocator locator : locators) {
            if (locator.appliesTo(fo)) {
                return locator;
            }
        }
        
        return null;
    }
    
    private FileType getFileType(FileObject fo) {
        TestLocator locator = getLocatorFor(fo);
        if (locator != null) {
            return locator.getFileType(fo);
        }
        
        return FileType.NEITHER;
    }
    
    private FileType getCurrentFileType() {
        FileObject fo = getApplicableFileObject(new int[1]);
        
        return (fo != null) ? getFileType(fo) : FileType.NEITHER;
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
