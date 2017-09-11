/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.RulesManagerImpl;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lahvac
 */
public class Utilities {
    public static List<PositionBounds> prepareSpansFor(FileObject file, Iterable<? extends int[]> spans) {
        List<PositionBounds> result = new ArrayList<PositionBounds>();

        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
            CloneableEditorSupport ces = (CloneableEditorSupport) ec;

            result = new LinkedList<PositionBounds>();

            for (int[] span : spans) {
                PositionRef start = ces.createPositionRef(span[0], Bias.Forward);
                PositionRef end = ces.createPositionRef(span[1], Bias.Forward);

                result.add(new PositionBounds(start, end));
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    public static Collection<RefactoringElementImplementation> createRefactoringElementImplementation(FileObject file, List<PositionBounds> spans, boolean verified) {
        List<RefactoringElementImplementation> result = new LinkedList<RefactoringElementImplementation>();

        try {
            DataObject d = DataObject.find(file);
            LineCookie lc = d.getLookup().lookup(LineCookie.class);

            for (PositionBounds bound : spans) {
                PositionRef start = bound.getBegin();
                PositionRef end = bound.getEnd();
                Line l = lc.getLineSet().getCurrent(start.getLine());
                String lineText = l.getText();

                int boldStart = start.getColumn();
                int boldEnd   = end.getLine() == start.getLine() ? end.getColumn() : lineText.length();

                StringBuilder displayName = new StringBuilder();

                if (!verified) {
                    displayName.append("(not verified) ");
                }

                displayName.append(escapedSubstring(lineText, 0, boldStart).replaceAll("^[ ]*", ""));
                displayName.append("<b>");
                displayName.append(escapedSubstring(lineText, boldStart, boldEnd));
                displayName.append("</b>");
                displayName.append(escapedSubstring(lineText, boldEnd, lineText.length()));

                result.add(new RefactoringElementImpl(file, bound, displayName.toString()));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    private static String escapedSubstring(String str, int start, int end) {
        String substring = str.substring(start, end);

        try {
            return XMLUtil.toElementContent(substring);
        } catch (CharConversionException ex) {
            Exceptions.printStackTrace(ex);
            return substring;
        }
    }
    
    public static Map<? extends HintMetadata, ? extends Collection<? extends HintDescription>> getBatchSupportedHints(ClassPathBasedHintWrapper cpBased) {
        Map<HintMetadata, Collection<? extends HintDescription>> result = new HashMap<HintMetadata, Collection<? extends HintDescription>>();

        for (Map.Entry<? extends HintMetadata, ? extends Collection<? extends HintDescription>> entry: cpBased.getHints().entrySet()) {
            if (entry.getKey().options.contains(Options.NO_BATCH)) continue;
            if (entry.getKey().kind != Kind.INSPECTION) continue;
            result.put(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
    
    public static final class ClassPathBasedHintWrapper {
        private Map<? extends HintMetadata, ? extends Collection<? extends HintDescription>> hints;
        private List<HintDescription> cpHints;

        public synchronized void compute() {
            if (hints != null) return ;

            Set<ClassPath> binaryClassPath = new HashSet<ClassPath>();
            binaryClassPath.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE));
            binaryClassPath.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT));
            if (cpHints == null) {
                cpHints = org.netbeans.modules.java.hints.spiimpl.Utilities.listClassPathHints(GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE), binaryClassPath);
            }
            HashMap<HintMetadata, Collection<HintDescription>> localHints = new HashMap<HintMetadata, Collection<HintDescription>>();

            RulesManagerImpl.sortByMetadata(cpHints, localHints);
            localHints.putAll((Map<HintMetadata, Collection<HintDescription>>) RulesManager.getInstance().readHints(null, null, null));

            this.hints = localHints;
        }

        public synchronized Map<? extends HintMetadata, ? extends Collection<? extends HintDescription>> getHints() {
            compute();
            return hints;
        }
        
        public synchronized void reset() {
            hints = null;
        }

    }

    private static final class RefactoringElementImpl extends SimpleRefactoringElementImplementation {

        private final FileObject file;
        private final PositionBounds span;
        private final String displayName;

        private final Lookup lookup;

        public RefactoringElementImpl(FileObject file, PositionBounds span, String displayName) {
            this.file = file;
            this.span = span;
            this.lookup = Lookups.fixed(file);
            this.displayName = displayName;
        }

        public String getText() {
            return getDisplayText();
        }

        public String getDisplayText() {
            return displayName;
        }

        public void performChange() {
            //throw new IllegalStateException();
        }

        public Lookup getLookup() {
            return lookup;
        }

        public FileObject getParentFile() {
            return file;
        }

        public PositionBounds getPosition() {
            return span;
        }

    }
    
    //TODO: Copy/Paste from RetoucheUtils
    
    private static final RequestProcessor RP = new RequestProcessor(Utilities.class.getName(), 1, false, false);
    
    /**
     * This is a helper method to provide support for delaying invocations of actions
     * depending on java model. See <a href="http://java.netbeans.org/ui/waitscanfinished.html">UI Specification</a>.
     * <br>Behavior of this method is following:<br>
     * If classpath scanning is not in progress, runnable's run() is called. <br>
     * If classpath scanning is in progress, modal cancellable notification dialog with specified
     * tile is opened.
     * </ul>
     * As soon as classpath scanning finishes, this dialog is closed and runnable's run() is called.
     * This method must be called in AWT EventQueue. Runnable is performed in AWT thread.
     * 
     * @param runnable Runnable instance which will be called.
     * @param actionName Title of wait dialog.
     * @return true action was cancelled <br>
     *         false action was performed
     */
    public static boolean invokeAfterScanFinished(final Runnable runnable , final String actionName) {
        assert SwingUtilities.isEventDispatchThread();
        if (SourceUtils.isScanInProgress()) {
            final ActionPerformer ap = new ActionPerformer(runnable);
            //100ms is workaround for 127536
            final RequestProcessor.Task waitTask = RP.post(ap, 100);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ap.cancel();
                    waitTask.cancel();
                }
            };
            JLabel label = new JLabel(getString("MSG_WaitScan"), javax.swing.UIManager.getIcon("OptionPane.informationIcon"), SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(12,12,11,11));
            DialogDescriptor dd = new DialogDescriptor(label, actionName, true, new Object[]{getString("LBL_CancelAction", new Object[]{actionName})}, null, 0, null, listener);
            waitDialog = DialogDisplayer.getDefault().createDialog(dd);
            waitDialog.pack();
            waitDialog.setVisible(true);
            waitDialog = null;
            return ap.hasBeenCancelled();
        } else {
            runnable.run();
            return false;
        }
    }
    
    private static Dialog waitDialog = null;
    
    private static String getString(String key) {
        return NbBundle.getMessage(Utilities.class, key);
    }
    
    private static String getString(String key, Object values) {
        return new MessageFormat(getString(key)).format(values);
    }


    private static class ActionPerformer implements Runnable {
        private Runnable action;
        private boolean cancel = false;

        ActionPerformer(Runnable a) {
            this.action = a;
        }
        
        public boolean hasBeenCancelled() {
            return cancel;
        }
        
        public void run() {
            try {
                SourceUtils.waitScanFinished();
            } catch (InterruptedException ie) {
                Exceptions.printStackTrace(ie);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!cancel) {
                        if (waitDialog != null) {
                            waitDialog.setVisible(false);
                            waitDialog.dispose();
                        }
                        action.run();
                    }
                }
            });
        }
        
        public void cancel() {
            assert SwingUtilities.isEventDispatchThread();
            // check if the scanning did not finish during cancel
            // invocation - in such case do not set cancel to true
            // and do not try to hide waitDialog window
            if (waitDialog != null) {
                cancel = true;
                waitDialog.setVisible(false);
                waitDialog.dispose();
            }
        }
    }
    
    //Endo of copy/paste

    private  static final String HINTS_FOLDER = "org-netbeans-modules-java-hints/rules/hints/";  // NOI18N
    public static final String CUSTOM_CATEGORY ="custom";
    
    public static String categoryDisplayName(String categoryCodeName) {
            FileObject catFO = FileUtil.getConfigFile(HINTS_FOLDER + categoryCodeName);
            return catFO != null ? getFileObjectLocalizedName(catFO) :
             CUSTOM_CATEGORY.equals(categoryCodeName)?NbBundle.getBundle("org.netbeans.modules.java.hints.resources.Bundle").getString("org-netbeans-modules-java-hints/rules/hints/custom"):categoryCodeName;
    }

    private static String getFileObjectLocalizedName( FileObject fo ) {
        Object o = fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
        if ( o instanceof String ) {
            String bundleName = (String)o;
            try {
                ResourceBundle rb = NbBundle.getBundle(bundleName);            
                String localizedName = rb.getString(fo.getPath());                
                return localizedName;
            }
            catch(MissingResourceException ex ) {
                // Do nothing return file path;
            }
        }
        return fo.getPath();
    }
}
