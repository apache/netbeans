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

package org.netbeans.modules.versionvault.ui.status;

import javax.swing.SwingUtilities;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * The Clearcase Versioning view.
 * 
 * @author Maros Sandor
 */
public class ClearcaseTopComponent extends TopComponent implements Externalizable {
   
    private static final long serialVersionUID = 1L;    
    
    private VersioningPanel         syncPanel;
    private Context                 context;
    private String                  contentTitle;
    private String                  branchTitle;
    private long                    lastUpdateTimestamp;
    
    private static ClearcaseTopComponent instance;
    private static final String PREFERRED_ID = "ClearcaseTopComponent";
    
    public ClearcaseTopComponent() {
        
        putClientProperty("SlidingName", NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_Title")); //NOI18N
        
        setName(NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_Title")); // NOI18N
        setToolTipText(NbBundle.getMessage(ClearcaseTopComponent.class, "HINT_ClearcaseTopComponent"));
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/versionvault/resources/icons/versioning-view.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_Title"));
        syncPanel = new VersioningPanel(this);
        add(syncPanel);
    }

    /**
     * Obtain the ClearcaseTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ClearcaseTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ClearcaseTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ClearcaseTopComponent) {
            return (ClearcaseTopComponent) win;
        }
        Logger.getLogger(ClearcaseTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
    @Override    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override    
    protected void componentActivated() {
        updateTitle();
        syncPanel.focus();
    }

    @Override    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(context);
        out.writeObject(contentTitle);
        out.writeLong(lastUpdateTimestamp);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object obj = in.readObject();
        context = (Context) obj;        
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
                    setName(NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_Title")); // NOI18N
                } else {
                    if (branchTitle == null) {
                        setName(NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_MultiTitle", contentTitle, age)); // NOI18N
                    } else {
                        setName(NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_Title_ContentBranch", contentTitle, branchTitle, age)); // NOI18N
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
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeUnknown"); // NOI18N
        } else if (l < 1000) { // 1000 equals 1 second
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeCurrent"); // NOI18N
        } else if (l < 2000) { // age between 1 and 2 seconds
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeOneSecond"); // NOI18N
        } else if (l < 60000) { // 60000 equals 1 minute
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeSeconds", Long.toString(l / 1000)); // NOI18N
        } else if (l < 120000) { // age between 1 and 2 minutes
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeOneMinute"); // NOI18N
        } else if (l < 3600000) { // 3600000 equals 1 hour
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeMinutes", Long.toString(l / 60000)); // NOI18N
        } else if (l < 7200000) { // age between 1 and 2 hours
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeOneHour"); // NOI18N
        } else if (l < 86400000) { // 86400000 equals 1 day
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeHours", Long.toString(l / 3600000)); // NOI18N
        } else if (l < 172800000) { // age between 1 and 2 days
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeOneDay"); // NOI18N
        } else {
            return NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_AgeDays", Long.toString(l / 86400000)); // NOI18N
        }
    }

    public static synchronized ClearcaseTopComponent getInstance() {
        if (instance == null) {
            instance = (ClearcaseTopComponent) WindowManager.getDefault().findTopComponent(PREFERRED_ID); // NOI18N
            if (instance == null) {
                Clearcase.LOG.log(Level.INFO, null, new IllegalStateException("Can not find Versioning component")); // NOI18N
                instance = new ClearcaseTopComponent();
            }
        }
    
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }

    /**
     * Sets files/folders the user wants to synchronize. They are typically activated (selected) nodes.
     * 
     * @param ctx new context of the Versioning view
     */
    public void setContext(VCSContext vcsContext) {
        syncPanel.cancelRefresh();
        if (vcsContext == null) {
            setName(NbBundle.getMessage(ClearcaseTopComponent.class, "MSG_Preparing")); // NOI18N
            setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            context = new Context(vcsContext);
            setBranchTitle(null);
            refreshContent();
        }
        setToolTipText(getContextFilesList(vcsContext, NbBundle.getMessage(ClearcaseTopComponent.class, "CTL_Clearcase_TopComponent_Title"))); // NOI18N            
    }
    
    private String getContextFilesList(VCSContext ctx, String def) {
        if (ctx == null || ctx.getRootFiles().size() == 0) return def;
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
        return context != null && context.getRootFiles().size() > 0;
    }

    @Override    
    protected String preferredID() {
        return PREFERRED_ID;    // NOI18N       
    }

    @Override    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
            
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized ClearcaseTopComponent getDefault() {
        if (instance == null) {
            instance = new ClearcaseTopComponent();
        }
        return instance;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return ClearcaseTopComponent.getDefault();
        }
    }


}
