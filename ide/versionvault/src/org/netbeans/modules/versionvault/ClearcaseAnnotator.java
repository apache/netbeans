/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault;

import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versionvault.ui.checkin.CheckinAction;
import org.netbeans.modules.versionvault.ui.status.RefreshAction;
import org.netbeans.modules.versionvault.ui.status.ShowPropertiesAction;
import org.netbeans.modules.versionvault.ui.add.AddAction;
import org.netbeans.modules.versionvault.ui.checkout.CheckoutAction;
import org.netbeans.modules.versionvault.ui.update.UpdateAction;
import org.netbeans.modules.versionvault.ui.diff.DiffAction;
import org.netbeans.modules.versionvault.ui.IgnoreAction;
import org.netbeans.modules.versionvault.ui.history.ViewRevisionAction;
import org.netbeans.modules.versionvault.ui.history.BrowseHistoryAction;
import org.netbeans.modules.versionvault.ui.history.BrowseVersionTreeAction;
import org.openide.util.Utilities;
import javax.swing.*;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import org.netbeans.modules.versionvault.client.status.FileEntry;
import org.netbeans.modules.versionvault.client.status.FileVersionSelector;
import org.netbeans.modules.versionvault.ui.AnnotateAction;
import org.netbeans.modules.versionvault.ui.hijack.HijackAction;
import org.netbeans.modules.versionvault.ui.checkin.ExcludeAction;
import org.netbeans.modules.versionvault.ui.checkout.ReserveAction;
import org.netbeans.modules.versionvault.ui.label.LabelAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.diff.PatchAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


/**
 * Responsible for coloring file labels and file icons in the IDE and providing IDE with menu items.
 * 
 * @author Maros Sandor
 */
public class ClearcaseAnnotator extends VCSAnnotator {
      
    /**
     * Fired when textual annotations and badges have changed. The NEW value is Set<File> of files that changed or NULL
     * if all annotaions changed.
     */
    static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged";    
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    private static MessageFormat newLocallyFormat = new MessageFormat("<font color=\"#008000\">{0}</font>{1}");         // NOI18N
    private static MessageFormat checkedoutFormat = new MessageFormat("<font color=\"#0000FF\">{0}</font>{1}");         // NOI18N
    private static MessageFormat hijackedFormat   = new MessageFormat("<font color=\"#FF0000\">{0}</font>{1}");         // NOI18N
    private static MessageFormat ignoredFormat    = new MessageFormat("<font color=\"#999999\">{0}</font>{1}");         // NOI18N
    private static MessageFormat removedFormat    = new MessageFormat("<font color=\"#999999\">{0}</font>{1}");         // NOI18N
    private static MessageFormat missingFormat    = new MessageFormat("<font color=\"#999999\">{0}</font>{1}");         // NOI18N
    private static MessageFormat eclipsedFormat   = new MessageFormat("<s><font color=\"#008000\">{0}</font></s>{1}");  // NOI18N

    private static MessageFormat newLocallyTooltipFormat = new MessageFormat("<font color=\"#008000\">{0}</font>");         // NOI18N
    private static MessageFormat checkedoutTooltipFormat = new MessageFormat("<font color=\"#0000FF\">{0}</font>");         // NOI18N
    private static MessageFormat hijackedTooltipFormat   = new MessageFormat("<font color=\"#FF0000\">{0}</font>");         // NOI18N
    private static MessageFormat ignoredTooltipFormat    = new MessageFormat("<font color=\"#999999\">{0}</font>");         // NOI18N
    private static MessageFormat removedTooltipFormat    = new MessageFormat("<font color=\"#999999\">{0}</font>");         // NOI18N
    private static MessageFormat missingTooltipFormat    = new MessageFormat("<font color=\"#999999\">{0}</font>");         // NOI18N
    private static MessageFormat eclipsedTooltipFormat   = new MessageFormat("<s><font color=\"#008000\">{0}</font></s>");  // NOI18N

    private static String badgeModified = "org/netbeans/modules/versionvault/resources/icons/modified-badge.png";

    private static String toolTipModified = "<img src=\"" + ClearcaseAnnotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(ClearcaseAnnotator.class, "MSG_Contains_Modified_Locally");

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N

    private static final int STATUS_BADGEABLE =         
        FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
        FileInformation.STATUS_VERSIONED_CHECKEDOUT | FileInformation.STATUS_VERSIONED_HIJACKED | 
        FileInformation.STATUS_NOTVERSIONED_ECLIPSED;
    
    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_IGNORED | 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_CHECKEDOUT | FileInformation.STATUS_VERSIONED_HIJACKED | 
            FileInformation.STATUS_NOTVERSIONED_ECLIPSED;

    public static String ANNOTATION_STATUS  = "status";
    public static String ANNOTATION_VERSION = "version";
    public static String ANNOTATION_RULE    = "rule";
    
    public static String[] LABELS = new String[] {ANNOTATION_STATUS, ANNOTATION_VERSION, ANNOTATION_RULE};
    
    private int INCLUDE_STATUS =
                FileInformation.STATUS_VERSIONED_UPTODATE |
                FileInformation.STATUS_LOCAL_CHANGE |
                FileInformation.STATUS_NOTVERSIONED_IGNORED |
                FileInformation.STATUS_NOTVERSIONED_ECLIPSED |
                FileInformation.STATUS_VERSIONED_CHECKEDOUT;

    private MessageFormat format;     
    private String emptyFormat;
    
    private FileStatusCache cache;    

    public ClearcaseAnnotator() {
        cache = Clearcase.getInstance().getFileStatusCache();        
        refresh();
    }

    public void refresh() {
        String string = ClearcaseModuleConfig.getLabelsFormat();
        if (string != null && !string.trim().equals("")) {
            string = Utils.skipUnsupportedVariables(string, new String[] {"{" + ANNOTATION_STATUS + "}", "{" + ANNOTATION_VERSION + "}", "{" + ANNOTATION_RULE + "}", "{mime_type}" });     // NOI18N
            string = string.replaceAll("\\{" + ANNOTATION_STATUS  + "\\}", "\\{0\\}");           // NOI18N    
            string = string.replaceAll("\\{" + ANNOTATION_VERSION + "\\}", "\\{1\\}");           // NOI18N
            string = string.replaceAll("\\{" + ANNOTATION_RULE    + "\\}", "\\{2\\}");           // NOI18N
            format = new MessageFormat(string);
            emptyFormat = format.format(new String[] {"", "", ""}, new StringBuffer(), null).toString().trim();            
        } else {
            format = null;
            emptyFormat = null;
        }                     
        refreshAllAnnotations();
    }

    private Image annotateFileIcon(VCSContext context, Image icon) {
        FileInformation mostImportantInfo = null;

        for (File file : context.getRootFiles()) {
            FileInformation info = getCachedInfo(file);
            if(info == null) {
                return null;
            }
            int status = info.getStatus();
            if ((status & INCLUDE_STATUS) == 0) continue;

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        if(mostImportantInfo == null) return null;
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
            statusText = newLocallyTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if ((status & FileInformation.STATUS_VERSIONED_CHECKEDOUT) != 0) {
            statusText = checkedoutTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (status == FileInformation.STATUS_VERSIONED_HIJACKED) {
            statusText = hijackedTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (status == FileInformation.STATUS_NOTVERSIONED_IGNORED ) {
            statusText = ignoredTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (status == FileInformation.STATUS_NOTVERSIONED_ECLIPSED ) {
            statusText = eclipsedTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (status == FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED ) {
            statusText = removedTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (status == FileInformation.STATUS_VERSIONED_LOADED_BUT_MISSING ) {
            statusText = missingTooltipFormat.format(new Object [] { mostImportantInfo.getStatusText() });
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null; // NOI18N
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        boolean isVersioned = false;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = getCachedInfo(file);
            if (info == null) {
                return null;
            }
            if ((info.getStatus() & STATUS_BADGEABLE) != 0) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) {
            return null;
        }
        boolean allExcluded = true;
        boolean modified = false;
        for (File root : context.getRootFiles()) {
            Map<File, FileInformation> modifiedFiles = cache.getAllModifiedValues(root); // XXX should go only after files from the context
            if (VersioningSupport.isFlat(root)) {
                for (File file : modifiedFiles.keySet()) {
                    if (file.getParentFile().equals(root)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(file);
                        if (info.isDirectory()) {
                            continue;
                        }
                        modified = true;
                        allExcluded &= ClearcaseModuleConfig.isExcludedFromCommit(file.getAbsolutePath());
                    }
                }
            } else {
                // TODO should go recursive!
                for (File mf : modifiedFiles.keySet()) {
                    if (Utils.isAncestorOrEqual(root, mf)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        if ((info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) && mf.equals(root)) {
                            continue;
                        }
                        modified = true;
                        allExcluded &= ClearcaseModuleConfig.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            }
        }
        if (modified && !allExcluded) {
            Image badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeModified, true), toolTipModified); // NOI18N
            return ImageUtilities.mergeImages(icon, badge, 16, 9);
        } else {
            return null;
        }
    }
    
    /**
     * Refreshes all textual annotations and badges.
     */
    private void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    public String annotateName(String name, VCSContext context) {        
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        
        for (File file : context.getRootFiles()) {
            FileInformation info = getCachedInfo(file);
            if(info == null) {
                return name;
            }
            int status = info.getStatus();
            if ((status & INCLUDE_STATUS) == 0) continue;
            
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }

        if (mostImportantInfo == null) return null;
        return folderAnnotation ? 
                annotateNameHtml(name, mostImportantInfo, mostImportantFile) : 
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    public Image annotateIcon(Image icon, VCSContext context) {
        boolean folderAnnotation = false;       
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }
        
        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon);
        }
        return annotateFolderIcon(context, icon);
    }
    
    public Action[] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        Lookup context = ctx.getElements();
        List<Action> actions = new ArrayList<Action>(20);
        File [] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        boolean noneVersioned = isNothingVersioned(files);
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(new CheckoutAction(ctx));
            actions.add(new ReserveAction(ctx));
            actions.add(new HijackAction(ctx));
            actions.add(new AddAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_AddToSourceControl_Name"), ctx)); //NOI18N
            actions.add(null);
            actions.add(new DiffAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Diff"), ctx)); //NOI18N
            actions.add(new UpdateAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Update"), ctx)); //NOI18N
            actions.add(new CheckinAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Checkin"), ctx)); //NOI18N
            actions.add(null);
            actions.add(SystemAction.get(PatchAction.class));
            actions.add(null);
            actions.add(new LabelAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Label"), ctx)); //NOI18N
            actions.add(null);
            actions.add(new AnnotateAction(ctx, Clearcase.getInstance().getAnnotationsProvider(ctx)));
            actions.add(new ViewRevisionAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_View_Revision"), ctx)); //NOI18N
            actions.add(new BrowseHistoryAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_BrowseHistory"), ctx)); //NOI18N
            actions.add(new BrowseVersionTreeAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_BrowseVersionTree"), ctx)); //NOI18N
            actions.add(null);
            actions.add(new IgnoreAction(ctx));
            actions.add(new ExcludeAction(ctx));
            actions.add(null);            
            actions.add(new ShowPropertiesAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_ShowProperties"), ctx)); //NOI18N
        } else {            
            if (noneVersioned) {
                // TODO: insert import action when we have a functional import wizard
            } else {
                actions.add(new CheckoutAction(ctx));
                actions.add(new ReserveAction(ctx));
                actions.add(new HijackAction(ctx));
                actions.add(new AddAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_AddToSourceControl_Name"), ctx)); //NOI18N                
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(RefreshAction.class), NbBundle.getMessage(ClearcaseAnnotator.class, "Action_ShowChanges"), context)); //NOI18N
                actions.add(new DiffAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Diff"), ctx)); //NOI18N
                actions.add(new UpdateAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Update"), ctx)); //NOI18N
                actions.add(new CheckinAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Checkin"), ctx)); //NOI18N
                actions.add(null);
                actions.add(new LabelAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_Label"), ctx)); //NOI18N
                actions.add(null);
                actions.add(new AnnotateAction(ctx, Clearcase.getInstance().getAnnotationsProvider(ctx)));
                actions.add(new ViewRevisionAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_View_Revision"), ctx)); //NOI18N
                actions.add(new BrowseHistoryAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_BrowseHistory"), ctx)); //NOI18N
                actions.add(new BrowseVersionTreeAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_BrowseVersionTree"), ctx)); //NOI18N
                actions.add(null);
                actions.add(new IgnoreAction(ctx));
                actions.add(new ExcludeAction(ctx));
                actions.add(null);                    
                actions.add(new ShowPropertiesAction(NbBundle.getMessage(ClearcaseAnnotator.class, "Action_ShowProperties"), ctx)); //NOI18N
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }    
    
    private static boolean isNothingVersioned(File[] files) {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        for (File file : files) {
            FileInformation info = cache.getCachedInfo(file);
            if (info != null && (info.getStatus() & FileInformation.STATUS_MANAGED) != 0) return false;
        }
        return true;
    }
    
    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }
    
    public String annotateNameHtml(String name, FileInformation info, File file) {        
        name = htmlEncode(name);

        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            if (format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {                                
                String statusText = info.getShortStatusText();
                if(!statusText.equals("")) { //NOI18N
                    textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                } else {
                    textAnnotation = ""; //NOI18N
                }                
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(ClearcaseAnnotator.class, "textAnnotation", textAnnotation); //NOI18N
        }
        
        if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
            return newLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if ((info.getStatus() & FileInformation.STATUS_VERSIONED_CHECKEDOUT) != 0) {
            return checkedoutFormat.format(new Object [] { name, textAnnotation });
        } else if (info.getStatus() == FileInformation.STATUS_VERSIONED_HIJACKED) {
            return hijackedFormat.format(new Object [] { name, textAnnotation });
        } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_IGNORED ) {
            return ignoredFormat.format(new Object [] { name, textAnnotation });
        } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_ECLIPSED ) {
            return eclipsedFormat.format(new Object [] { name, textAnnotation });
        } else if (info.getStatus() == FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED ) {
            return removedFormat.format(new Object [] { name, textAnnotation });
        } else if (info.getStatus() == FileInformation.STATUS_VERSIONED_LOADED_BUT_MISSING ) {
            return missingFormat.format(new Object [] { name, textAnnotation });
        }         
                
        return name;
    }
    
    /**
     * Applies custom format.
     */
    private String formatAnnotation(FileInformation info, File file) {
        String statusText = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusText = info.getShortStatusText();
        }

        String versionSelector = "";     // NOI18N
        FileEntry fileEntry = info.getFileEntry(Clearcase.getInstance().getClient(), file);
        String rule = "";
        if (fileEntry != null) {
            FileVersionSelector version = fileEntry.getOriginVersion();
            if(version != null) {
                versionSelector = fileEntry.getOriginVersion().getVersionSelector();                
            }            
            rule = fileEntry.getRule();
        }
        
        Object[] arguments = new Object[] {
            statusText,
            versionSelector,
            rule
        };
                
        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();    
        if(annotation.equals(emptyFormat)) {
            return "";                          // NOI18N
        } else {
            annotation = annotation.trim();
            if(annotation.equals("")) {
                return "";                      // NOI18N   
            } else {
                return " " + annotation;        // NOI18N   
            }            
        }
    }
    
    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }    

    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return ClearcaseUtils.getComparableStatus(a.getStatus()) < ClearcaseUtils.getComparableStatus(b.getStatus());
    }        

    private static FileInformation getCachedInfo(File file) {        
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        FileInformation info = cache.getCachedInfo(file);
        if(info == null) {
            cache.refreshLater(file);
        }
        return info;
    }

}
