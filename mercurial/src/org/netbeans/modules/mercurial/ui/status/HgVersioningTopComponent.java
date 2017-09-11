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

package org.netbeans.modules.mercurial.ui.status;

import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Top component of the Versioning view.
 * 
 * @author Maros Sandor
 */
@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_ALWAYS, preferredID=HgVersioningTopComponent.PREFERRED_ID)
@TopComponent.Registration(mode="output", openAtStartup=false, position=3109)
public class HgVersioningTopComponent extends TopComponent implements Externalizable, PropertyChangeListener {
   
    private static final long serialVersionUID = 1L;    
    
    private VersioningPanel         syncPanel;
    private VCSContext              context;
    private String                  contentTitle;
    private String                  branchTitle;
    private long                    lastUpdateTimestamp;
    public static final String PREFERRED_ID = "hgversioningTC"; // NOI18N
    
    private static HgVersioningTopComponent instance;
    private WorkingCopyInfo info;
    private File[] files = new File[0];

    public HgVersioningTopComponent() {
        putClientProperty("SlidingName", NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); //NOI18N
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); //NOI18N

        setName(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/mercurial/resources/icons/versioning-view.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        syncPanel = new VersioningPanel(this);
        add(syncPanel);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    protected void componentActivated() {
        updateTitle();
        syncPanel.focus(true);
    }

    @Override
    protected void componentDeactivated () {
        syncPanel.focus(false);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        refreshContent();
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(this.contentTitle);
        File[] files = context == null
                ? this.files
                : context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        out.writeObject(files);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        setContentTitle((String) in.readObject());
        files = (File[]) in.readObject();
        final List<Node> nodes = new ArrayList<>(files.length);
        for (File file : files) {
            nodes.add(new AbstractNode(Children.LEAF, Lookups.singleton(file)) {
                @Override
                public String getDisplayName() {
                    return getLookup().lookup(File.class).getName();
                }
            });
        }
        setContext(null);
        Utils.post(new Runnable() {

            @Override
            public void run () {
                try {
                    OpenProjects.getDefault().openProjects().get();
                } catch (InterruptedException | ExecutionException ex) {
                }
                final VCSContext ctx = VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        if (context == null) {
                            setContext(ctx);
                            performRefreshAction();
                        }
                    }
                    
                });
            }
        });
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
     * @param contentTitle a new content title, e.g. "2 projects" // NOI18N
     */ 
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        updateTitle();
    }

    /**
     * Sets the 'branch' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     * 
     * @param branchTitle a new content title, e.g. "release40" branch // NOI18N
     */ 
    void setBranchTitle(String branchTitle) {
        if (branchTitle == null) {
            branchTitle = NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_VersioningView_UnnamedBranchTitle"); //NOI18N
        }
        this.branchTitle = branchTitle;
        updateTitle();
    }
    
    public void contentRefreshed() {
        lastUpdateTimestamp = System.currentTimeMillis();
        updateTitle();
    }
    
    private void updateTitle() {
        SwingUtilities.invokeLater(new Runnable (){
            @Override
            public void run() {
                if (contentTitle == null) {
                    setName(NbBundle.getMessage(HgVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
                } else {
                    File baseFile = HgUtils.getRootFile(context);
                    String name = "";
                    if(baseFile != null){
                        name = baseFile.getName();
                    }
                    
                    if (branchTitle == null) {
                        setName(NbBundle.getMessage(HgVersioningTopComponent.class, 
                                "CTL_Versioning_TopComponent_MultiTitle", 
                                contentTitle, name.equals(contentTitle)? "": "[" + name + "]"));  // NOI18N
                    } else {
                        setName(NbBundle.getMessage(HgVersioningTopComponent.class, 
                                "CTL_Versioning_TopComponent_Title_ContentBranch", 
                                contentTitle, name.equals(contentTitle)? "": "[" + name + "] ", branchTitle)); // NOI18N
                    }
                }                
                setToolTipText(getName());
            }
        });
    }

    String getContentTitle() {
        return contentTitle;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized HgVersioningTopComponent getDefault() {
        if (instance == null) {
            instance = new HgVersioningTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the HgVersioningTopComponent  instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HgVersioningTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Mercurial.LOG.log(Level.FINE, "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof HgVersioningTopComponent) {
            return (HgVersioningTopComponent)win;
        }
        Mercurial.LOG.log(Level.FINE, 
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
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
    public void setContext(VCSContext ctx) {
        syncPanel.cancelRefresh();

        if (ctx == null) {
            setName(NbBundle.getMessage(HgVersioningTopComponent.class, "MSG_Preparing")); // NOI18N
            setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            context = ctx;
            syncPanel.setContext(ctx);
            refreshBranchName();
            refreshContent();
        }
        setToolTipText(getName());
    }

    /** Tests whether it shows some content. */
    public boolean hasContext() {
        return context != null && context.getRootFiles().size() > 0;
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (WorkingCopyInfo.PROPERTY_CURRENT_BRANCH.equals(evt.getPropertyName())) {
            setBranchTitle(((String) evt.getNewValue()));
            updateTitle();
        }
    }
    
    void refreshBranchName () {
        Runnable runnable = new Runnable () {
            @Override
            public void run() {
                if (info != null) {
                    info.removePropertyChangeListener(HgVersioningTopComponent.this);
                    info = null;
                }
                Set<File> repositoryRoots = HgUtils.getRepositoryRoots(context);
                String label = null;
                if (repositoryRoots.size() == 1) {
                    info = WorkingCopyInfo.getInstance(repositoryRoots.iterator().next());
                    info.addPropertyChangeListener(HgVersioningTopComponent.this);
                    label = info.getCurrentBranch();
                }
                setBranchTitle(label);
            }
        };
        if (EventQueue.isDispatchThread()) {
            Utils.post(runnable);
        } else {
            runnable.run();
        }
    }
}
