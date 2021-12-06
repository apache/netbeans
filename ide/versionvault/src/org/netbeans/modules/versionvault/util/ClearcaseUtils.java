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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.versionvault.util;

import org.netbeans.modules.versionvault.client.ClearcaseClient;
import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.client.CheckoutCommand;
import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.ui.hijack.HijackAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.awt.Dialog;

import org.netbeans.modules.versionvault.client.status.FileEntry;
import org.netbeans.modules.versionvault.client.status.ListStatus;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


/**
 * Clearase specific utility methods.
 * 
 * @author Maros Sandor
 */
public class ClearcaseUtils {
    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public static VCSContext getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        return VCSContext.forNodes(nodes);
    }

    /**
     * Assynchronously refreshes the status for files and their filesystems. 
     * Note, that a refresh on the NB filesystem may result in intercepting 
     * new file events. 
     * All necessary filesystems will be refreshed to.
     * 
     * @param files files to be refreshed          
     * @param includeChildren if true all children for the given files will be explicitly refreshed too
     */
    public static void afterCommandRefresh(final File[] files, final boolean includeChildren) {          
        afterCommandRefresh(files, includeChildren, true);
    }
    
    /**
     * Assynchronously refreshes the status for files and their filesystems. 
     * Note, that a refresh on the NB filesystem may result in intercepting 
     * new file events. 
     * 
     * @param files files to be refreshed          
     * @param includeChildren if true all children for the given files will be explicitly refreshed too
     * @param refreshFS if true refreshes also all necessary filesystems.
     */
    public static void afterCommandRefresh(final File[] files, final boolean includeChildren, final boolean refreshFS) {          
        Clearcase.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                if(refreshFS) {
                    // refreshing the NB filessytem before the cache refresh starts firing change events -> 
                    // otherwise they might cause externally deleted/created warnings
                    Set<File> parents = new HashSet<File>();
                    for (File file : files) {
                        File parent = file.getParentFile();
                        if (parent != null) {
                            parents.add(parent);
                        }
                    }
                    FileUtil.refreshFor(parents.toArray(new File[parents.size()])); 
                }    
                // refresh the cache ...
                Set<File> refreshSet = new HashSet<File>();
                for (File file : files) {
                    if(includeChildren) {
                        refreshSet.addAll(getFilesTree(file));    
                    } else {
                        refreshSet.add(file);
                    }
                }                        
                File[] refreshFiles = refreshSet.toArray(new File[refreshSet.size()]);
                Clearcase.getInstance().getFileStatusCache().refreshLater(refreshFiles);                            
            }
        });                
    }

    public static List<File> getFilesTree(File file) {
        List<File> ret = new  ArrayList<File>();
        ret.add(file);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for (File f : files) {
                    ret.addAll(getFilesTree(f));
                }
            }
        }
        return ret;
    }

    public static enum ViewType { None, Snapshot, Dynamic, Remote };
    
    private ClearcaseUtils() {
    }

    private static ViewType getViewType(File file) {
        // TODO: incomplete implementation
        if (Clearcase.getInstance().getTopmostSnapshotViewAncestor(file) != null) return ViewType.Snapshot;
        return ViewType.None;
    }

    /**
     * Query for files in snapshot views.
     * 
     * @param ctx a context to scan
     * @return true if the context contains at least one file from a snapshot view, false otherwise
     */
    public static boolean containsSnapshot(VCSContext ctx) {
        for (File file : ctx.getRootFiles()) {
            if (getViewType(file) == ViewType.Snapshot) return true;
        }
        return false;
    }
    
    /**
     * Computes previous revision number to the given one.
     * 
     * @param rev a revision number, eg. "/main/3"
     * @return String predecesor revision number, eg. "/main/2"
     */
    public static String previousRevision(String rev) {
        int idx = rev.lastIndexOf(File.separator);
        if (idx == -1) return null;
        int revno = 0;
        try {
            revno = Integer.parseInt(rev.substring(idx + 1));
        } catch (NumberFormatException e) {
            return null;
        }
        return revno == 0 ? null : rev.substring(0, idx + 1) + (revno - 1);
    }
    
    /**
     * Determines whether the supplied context contains something managed by Clearcase.s
     * 
     * @param context context to examine
     * @return true if the context contains some files that are managed by Clearcase, false otherwise
     */
    public static boolean containsVersionedFiles(VCSContext context) {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        Set<File> roots = context.getRootFiles();
        for (File file : roots) {
            if ((cache.getInfo(file).getStatus() & FileInformation.STATUS_VERSIONED) != 0 ) {
                return true;
            }                
        }
        return false;
    }
    
    /**
     * Returns path of the file in VOB.
     * 
     * @param file a versioned file 
     * @return path of the file in VOB or null if the file is not under clearcase control
     */
    public static String getLocation(File file) {
        File parent = Clearcase.getInstance().getTopmostManagedParent(file);
        if (parent != null) {
            // TODO what is vob root?
            return file.getAbsolutePath().substring(parent.getAbsolutePath().length());
        } else {
            return null;
        }
    }
    
    public static String getExtendedName(File file, String revision) {
        return file.getAbsolutePath() + Clearcase.getInstance().getExtendedNamingSymbol() + revision;
    }
    
    /**
     * Scans given file set recursively and puts all files and directories found in the result array. 
     * 
     * @return File[] all files and folders found in the 
     */
    public static File[] expandRecursively(VCSContext ctx, FileFilter filter) {
        Set<File> fileSet = ctx.computeFiles(filter);
        Set<File> expandedFileSet = new HashSet<File>(fileSet.size() * 2);
        for (File file : fileSet) {
            addFilesRecursively(file, expandedFileSet);
        }
        return (File[]) expandedFileSet.toArray(new File[expandedFileSet.size()]);
    }

    private static void addFilesRecursively(File file, Set<File> fileSet) {
        fileSet.add(file);
        File [] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                addFilesRecursively(child, fileSet);
            }
        }
    }

    public static String getMimeType(File file) {
        // TODO: implement
        return "text/plain";
    }

    /**
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public static boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    public static class ByImportanceComparator implements Comparator<FileInformation> {
        public int compare(FileInformation i1, FileInformation i2) {
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }
       
    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_LOADED_BUT_MISSING)) {
            return 20;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_ECLIPSED)) {
            return 40;    
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_HIJACKED)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_CHECKEDOUT)) {
            return 60;
        } else if (0 != (status & FileInformation.STATUS_UNRESERVED)) {
            return 70;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 80;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_IGNORED)) {
            return 90;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 100;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 150;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }
    
    /**
     * Returns the {@link FileEntry} for the given file or 
     * null if the file does not exist.
     * 
     * This method synchronously accesses disk and may block for a longer period of time.
     * 
     * @param file the file to get the {@link FileEntry} for
     * @return the {@link FileEntry}
     */
    public static FileEntry readEntry(ClearcaseClient client, File file) {
        List<FileEntry> entries = readEntries(client, file, true);
        if(entries == null || entries.size() == 0) {
            return null;
        }
        return entries.get(0);
    }
    
    /**
     * Returns FileEntries for the given file.
     * 
     * This method synchronously accesses disk and may block for a longer period of time.
     * 
     * @param file the file
     * @return {@link FileEntry}-s describing the files actuall status
     * @see {@link FileEntry}
     */   
    public static List<FileEntry> readEntries(ClearcaseClient client, File file, boolean directory) {
        if(file == null) {
            return null;
        }       
        // 1. list files ...
        ListStatus ls = new ListStatus(file, directory);    
        client.exec(ls, false);

        return new ArrayList<FileEntry>(ls.getOutput());
    }

    /**
     * The file given in {@link #ensureMutable(org.netbeans.modules.clearcase.client.ClearcaseClient, java.io.File)} or
     * {@link #ensureMutable(org.netbeans.modules.clearcase.client.ClearcaseClient, java.io.File, org.netbeans.modules.clearcase.client.status.FileEntry)}     
     * is mutable.
     */
    public static int IS_MUTABLE        = 1;
    
    /**
     * The file given in {@link #ensureMutable(org.netbeans.modules.clearcase.client.ClearcaseClient, java.io.File)} or
     * {@link #ensureMutable(org.netbeans.modules.clearcase.client.ClearcaseClient, java.io.File, org.netbeans.modules.clearcase.client.status.FileEntry)}
     * is mutable and was checkedout by the method.
     */
    public static int WAS_CHECKEDOUT    = 2;
    
    /**
     * Checks out the file or directory depending on the user-selected strategy in Options.
     * In case the file is already writable or the directory is checked out, the method does nothing.
     * Interceptor entry point.
     * 
     * @param client ClearcaseClient
     * @param file file to checkout
     * @return <ul> 
     *            <li>0 isn't mutable
     *            <li>{@link #IS_MUTABLE} is mutable  
     *            <li>{@link #WAS_CHECKEDOUT} is mutable and was checkout by the method
     *          </ul> 
     * @see org.netbeans.modules.clearcase.ClearcaseModuleConfig#getOnDemandCheckout()
     */
    public static int ensureMutable(ClearcaseClient client, File file) {
        return ensureMutable(client, file, null);
    }   
        
    /**
     * Checks out the file or directory depending on the user-selected strategy in Options.
     * In case the file is already writable or the directory is checked out, the method does nothing.
     * Interceptor entry point.
     * 
     * @param client ClearcaseClient
     * @param file file to checkout
     * @param entry the given files {@link FileEntry}
     * @return <ul> 
     *            <li>0 isn't mutable
     *            <li>{@link #IS_MUTABLE} is mutable  
     *            <li>{@link #WAS_CHECKEDOUT} is mutable and was checkout by the method
     *          </ul> 
     * @see org.netbeans.modules.clearcase.ClearcaseModuleConfig#getOnDemandCheckout()
     */
    public static int ensureMutable(ClearcaseClient client, File file, FileEntry entry) {
        if (file.isDirectory()) {
            if(entry == null) {
                entry = ClearcaseUtils.readEntry(client, file);                
            }
            if (entry == null || entry.isCheckedout() || entry.isViewPrivate()) {
                return IS_MUTABLE;
            }
        } else {
            if (file.canWrite()) return IS_MUTABLE;
        }

        ClearcaseModuleConfig.OnDemandCheckout odc = ClearcaseModuleConfig.getOnDemandCheckout();
        boolean canHijack = getViewType(file) == ViewType.Snapshot;
        if (!canHijack && odc == ClearcaseModuleConfig.OnDemandCheckout.Hijack) {
            odc = ClearcaseModuleConfig.OnDemandCheckout.Unreserved;
        }
        if (odc == ClearcaseModuleConfig.OnDemandCheckout.Prompt) {
            odc = promptForAction(file);
        }

        CheckoutCommand command;
        switch (odc) {
        case Disabled:
            return 0;
        case Hijack:
            return HijackAction.hijack(file) ? IS_MUTABLE : 0; 
        case Reserved:
        case ReservedWithHijackFallback:
        case ReservedWithUnreservedFallback:
        case ReservedWithBothFallbacks:
            command = new CheckoutCommand(new File [] { file }, null, CheckoutCommand.Reserved.Reserved, true, new AfterCommandRefreshListener(file));
            break;
        case Unreserved:
        case UnreservedWithFallback:
            command = new CheckoutCommand(new File [] { file }, null, CheckoutCommand.Reserved.Unreserved, true, new AfterCommandRefreshListener(file));
            break;
        default:
            throw new IllegalStateException("Illegal Checkout type: " + odc);
        }
        
        Clearcase.getInstance().getClient().exec(command, odc == ClearcaseModuleConfig.OnDemandCheckout.Reserved || odc == ClearcaseModuleConfig.OnDemandCheckout.Unreserved);
        if(!command.hasFailed()) {
            return WAS_CHECKEDOUT;
        } else if (canHijack && (odc == ClearcaseModuleConfig.OnDemandCheckout.UnreservedWithFallback || odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithHijackFallback)) {
            return HijackAction.hijack(file) ? IS_MUTABLE : 0; 
        } else if(odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithUnreservedFallback || odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithBothFallbacks) {
            command = new CheckoutCommand(new File [] { file }, null, CheckoutCommand.Reserved.Unreserved, true, 
                                          new OutputWindowNotificationListener(), new AfterCommandRefreshListener(file));
            Clearcase.getInstance().getClient().exec(command, odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithUnreservedFallback);
            if (command.hasFailed() && canHijack) {    
                if (odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithBothFallbacks) {
                    return HijackAction.hijack(file) ? IS_MUTABLE : 0; 
                }
                return 0;
            } else {
                return WAS_CHECKEDOUT;
            }   
        } else {
            return 0;
        }
    }

    private static ClearcaseModuleConfig.OnDemandCheckout promptForAction(File file) {
        ClearcaseModuleConfig.OnDemandCheckout odc = ClearcaseModuleConfig.OnDemandCheckout.valueOf(ClearcaseModuleConfig.getPreferences().get("ondemandcheckout.action", ClearcaseModuleConfig.OnDemandCheckout.Reserved.toString()));
        CheckoutActionPanel panel = new CheckoutActionPanel(file, odc);
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ClearcaseUtils.class, "OnDemandCheckouts_Title")); // NOI18N
        dd.setModal(true);
        dd.setMessageType(DialogDescriptor.QUESTION_MESSAGE);
        
        dd.setOptions(new Object[] {DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION});
        dd.setHelpCtx(new HelpCtx(ClearcaseUtils.class));
                
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "ondemandcheckout.dialog")); // NOI18N       
        dialog.pack();        
        dialog.setVisible(true);
        
        Object value = dd.getValue();
        if (value != DialogDescriptor.OK_OPTION) return ClearcaseModuleConfig.OnDemandCheckout.Disabled;
        
        if (panel.rbHijack.isSelected()) odc = ClearcaseModuleConfig.OnDemandCheckout.Hijack; 
        if (panel.rbReserved.isSelected()) odc = ClearcaseModuleConfig.OnDemandCheckout.Reserved; 
        if (panel.rbUnreserved.isSelected()) odc = ClearcaseModuleConfig.OnDemandCheckout.Unreserved; 
        
        ClearcaseModuleConfig.getPreferences().put("ondemandcheckout.action", odc.toString());
        return odc;
    }
}
