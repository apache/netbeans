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

package org.netbeans.modules.subversion.ui.status;

import javax.swing.SwingUtilities;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.modules.subversion.util.Context;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.*;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;

/**
 * Top component of the Versioning view.
 * 
 * @author Maros Sandor
 */
public class SvnVersioningTopComponent extends TopComponent implements Externalizable {
   
    private static final long serialVersionUID = 1L;    
    
    private VersioningPanel         syncPanel;
    private Context                 context;
    private String                  contentTitle;
    private String                  branchTitle;
    private long                    lastUpdateTimestamp;
    
    private static SvnVersioningTopComponent instance;

    public SvnVersioningTopComponent() {
        
        putClientProperty("SlidingName", NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); //NOI18N
        
        setName(NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/subversion/resources/icons/versioning-view.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title"));
        syncPanel = new VersioningPanel(this);
        add(syncPanel);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    protected void componentActivated() {
        updateTitle();
        syncPanel.focus(true);
    }

    @Override
    protected void componentDeactivated () {
        syncPanel.focus(false);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(context);
        out.writeObject(contentTitle);
        out.writeLong(lastUpdateTimestamp);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        context = (Context) in.readObject();
        contentTitle = (String) in.readObject();
        lastUpdateTimestamp = in.readLong();
        syncPanel.deserialize();
    }

    protected void componentOpened() {
        super.componentOpened();
        refreshContent();
    }

    protected void componentClosed() {
        super.componentClosed();
    }

    private void refreshContent() {
        if (syncPanel == null) return;  // the component is not showing => nothing to refresh
        updateTitle();
        syncPanel.setContext(context);        
    }

    /**
     * Sets the 'content' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     * 
     * @param contentTitle a new content title, e.g. "2 projects"
     */ 
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        updateTitle();
    }

    /**
     * Sets the 'branch' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     * 
     * @param branchTitle a new content title, e.g. "release40" branch
     */ 
    void setBranchTitle(String branchTitle) {
        this.branchTitle = branchTitle;
        updateTitle();
    }
    
    public void contentRefreshed() {
        lastUpdateTimestamp = System.currentTimeMillis();
        updateTitle();
    }
    
    private void updateTitle() {
        final String age = computeAge(System.currentTimeMillis() - lastUpdateTimestamp);
        SwingUtilities.invokeLater(new Runnable (){
            public void run() {
                if (contentTitle == null) {
                    setName(NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
                } else {
                    if (branchTitle == null) {
                        setName(NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_MultiTitle", contentTitle, age)); // NOI18N
                    } else {
                        setName(NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title_ContentBranch", contentTitle, branchTitle, age)); // NOI18N
                    }
                }                
            }
        });
    }

    String getContentTitle() {
        return contentTitle;
    }

    private String computeAge(long l) {
        if (lastUpdateTimestamp == 0) {
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeUnknown"); // NOI18N
        } else if (l < 1000) { // 1000 equals 1 second
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeCurrent"); // NOI18N
        } else if (l < 2000) { // age between 1 and 2 seconds
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeOneSecond"); // NOI18N
        } else if (l < 60000) { // 60000 equals 1 minute
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeSeconds", Long.toString(l / 1000)); // NOI18N
        } else if (l < 120000) { // age between 1 and 2 minutes
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeOneMinute"); // NOI18N
        } else if (l < 3600000) { // 3600000 equals 1 hour
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeMinutes", Long.toString(l / 60000)); // NOI18N
        } else if (l < 7200000) { // age between 1 and 2 hours
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeOneHour"); // NOI18N
        } else if (l < 86400000) { // 86400000 equals 1 day
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeHours", Long.toString(l / 3600000)); // NOI18N
        } else if (l < 172800000) { // age between 1 and 2 days
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeOneDay"); // NOI18N
        } else {
            return NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_AgeDays", Long.toString(l / 86400000)); // NOI18N
        }
    }

    public static synchronized SvnVersioningTopComponent getInstance() {
        if (instance == null) {
            instance = (SvnVersioningTopComponent) WindowManager.getDefault().findTopComponent("svnversioning"); // NOI18N
            if (instance == null) {
                Subversion.LOG.log(Level.INFO, null, new IllegalStateException("Can not find Versioning component")); // NOI18N
                instance = new SvnVersioningTopComponent();
            }
        }
    
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }

    /**
     * Programmatically invokes the Refresh action.
     */ 
    public void performRefreshAction() {
        syncPanel.performRefreshAction();
    }

    /**
     * Sets files/folders the user wants to synchronize. They are typically activated (selected) nodes.
     * 
     * @param ctx new context of the Versioning view
     */
    public void setContext(Context ctx) {
        syncPanel.cancelRefresh();
        if (ctx == null) {
            setName(NbBundle.getMessage(SvnVersioningTopComponent.class, "MSG_Preparing")); // NOI18N
            setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            context = ctx;
            setBranchTitle(null);
            refreshContent();
        }
        setToolTipText(getContextFilesList(ctx, NbBundle.getMessage(SvnVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title"))); // NOI18N            
    }
    
    private String getContextFilesList(Context ctx, String def) {
        if (ctx == null || ctx.getRootFiles().length == 0) return def;
        StringBuffer sb = new StringBuffer(200);
        sb.append("<html>"); // NOI18N
        for (File file : ctx.getRootFiles()) {
            sb.append(file.getAbsolutePath());
            sb.append("<br>"); // NOI18N
        }
        sb.delete(sb.length() - 4, Integer.MAX_VALUE);
        return sb.toString();
    }

    /** Tests whether it shows some content. */
    public boolean hasContext() {
        return context != null && context.getRootFiles().length > 0;
    }

    protected String preferredID() {
        return "synchronize";    // NOI18N       
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
}
