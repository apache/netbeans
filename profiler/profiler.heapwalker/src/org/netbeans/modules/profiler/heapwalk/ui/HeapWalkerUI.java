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

package org.netbeans.modules.profiler.heapwalk.ui;

import org.netbeans.modules.profiler.heapwalk.HeapWalker;
import org.netbeans.modules.profiler.heapwalk.HeapWalkerManager;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.ProfilerTopComponent;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "HeapWalkerUI_ComponentDescr=Profiler HeapWalker",
    "HeapWalkerUI_LoadingProgress=Loading heap dump..."
})
public class HeapWalkerUI extends ProfilerTopComponent {
    
    private static final byte PERSISTENCE_VERSION_MAJOR = 8;
    private static final byte PERSISTENCE_VERSION_MINOR = 1;
    
    private static final String HELP_CTX_KEY = "HeapWalker.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeapWalker heapWalker;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    
    /**
     * Default constructor, used when restoring persisted heap dumps.
     */
    public HeapWalkerUI() {
        setIcon(Icons.getImage(ProfilerIcons.HEAP_DUMP));
        getAccessibleContext().setAccessibleDescription(Bundle.HeapWalkerUI_ComponentDescr());
        setLayout(new BorderLayout());
    }
    
    public HeapWalkerUI(HeapWalker heapWalker) {
        this();
        initImpl(heapWalker);
    }
    
    private void initImpl(HeapWalker hw) {
        this.heapWalker = hw;

        initDefaults();
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    protected Component defaultFocusOwner() {
        return heapWalker == null ? this : heapWalker.getMainHeapWalker().getPanel();
    }

    // --- TopComponent support --------------------------------------------------
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeByte(PERSISTENCE_VERSION_MAJOR);
        out.writeByte(PERSISTENCE_VERSION_MINOR);
        
        out.writeUTF(Utilities.toURI(heapWalker.getHeapDumpFile()).toString());
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            add(new JLabel(Bundle.HeapWalkerUI_LoadingProgress(), JLabel.CENTER), BorderLayout.CENTER);
            invalidate();
            doLayout();
            repaint();
            
            super.readExternal(in);
            
            in.readByte(); // PERSISTENCE_VERSION_MAJOR
            in.readByte(); // PERSISTENCE_VERSION_MINOR
            
            URI uri = new URI(in.readUTF());
            final File file = Utilities.toFile(uri);
            new RequestProcessor("HPROF loader for " + getName()).post(new Runnable() { // NOI18N
                public void run() {
                    try {
                        final HeapWalker hw = new HeapWalker(file);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                removeAll();
                                initImpl(hw);
                            }
                        });
                    } catch (Throwable t) { handleReadThrowable(t); }
                }
            });
        } catch (Throwable t) { handleReadThrowable(t); }
    }
    
    private void handleReadThrowable(Throwable t) {
        ProfilerLogger.info("Restoring heap dump failed: " + t.getMessage()); // NOI18N
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { close(); }
        });
    }

    public int getPersistenceType() {
        return ProfilerIDESettings.getInstance().getReopenHeapDumps()?
               PERSISTENCE_ONLY_OPENED : PERSISTENCE_NEVER;
    }

    protected void componentClosed() {
        if (heapWalker == null) return; // Window closed after persistence failure
        
        HeapWalkerManager.getDefault().heapWalkerClosed(heapWalker);
    }

    protected String preferredID() {
        return this.getClass().getName();
    }
    
    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    // --- UI definition ---------------------------------------------------------
    private void initComponents() {
        add(heapWalker.getMainHeapWalker().getPanel(), BorderLayout.CENTER);
        invalidate();
        doLayout();
        repaint();
    }

    private void initDefaults() {
        setName(heapWalker.getName());
        if (heapWalker.getHeapDumpFile() != null)
            setToolTipText(heapWalker.getHeapDumpFile().getAbsolutePath());
        
        File file = heapWalker.getHeapDumpFile();
        putClientProperty(RECENT_FILE_KEY, file); // #221709, add heap dump to Open Recent File list
    }
}
