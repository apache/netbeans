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

package org.netbeans.modules.profiler.heapwalk.model;


import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import org.openide.util.NbBundle;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.heap.HeapProgress;
import org.netbeans.lib.profiler.heap.Instance;


/**
 * Implements common methods of all Fields Browser nodes holding reference to org.netbeans.lib.profiler.heap.Instance
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "InstanceNode_LoopToString=(loop to {0})",
    "InstanceNode_References=Computing references..."
})
public abstract class InstanceNode extends AbstractHeapWalkerNode implements HeapWalkerInstanceNode {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final NumberFormat numberFormat = NumberFormat.getInstance();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    HeapWalkerNode loopTo;
    private Instance instance;
    private String name;
    private String details;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InstanceNode(Instance instance, String name, HeapWalkerNode parent) {
        this(instance, name, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
    }

    public InstanceNode(Instance instance, String name, HeapWalkerNode parent, int mode) {
        super(parent, mode);

        this.instance = instance;

        this.name = name;

        this.loopTo = computeLoopTo();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public abstract boolean isArray();

    public Instance getInstance() {
        return instance;
    }

    public boolean isLeaf() {
        return !hasInstance() || isLoop();
    }

    public boolean isLoop() {
        return getLoopTo() != null;
    }

    public HeapWalkerNode getLoopTo() {
        return loopTo;
    }

    public boolean hasInstance() {
        return instance != null;
    }
    
    public String getDetails() {
        if (!hasInstance()) return null;
        if (details == null) {
            details = "";
            computeDetails();
        }
        return details;
    }

    protected List getReferences() {
        if (hasInstance()) {
            ProgressHandle pHandle = null;
            ChangeListener cl = null;
            
            try {
                pHandle = ProgressHandle.createHandle(Bundle.InstanceNode_References());
                pHandle.setInitialDelay(200);
                pHandle.start(HeapProgress.PROGRESS_MAX);

                cl = setProgress(pHandle);
                return getInstance().getReferences();
            } finally {
                if (pHandle != null) {
                    pHandle.finish();
                }
                if (cl != null) {
                    HeapProgress.getProgress().removeChangeListener(cl);
                }
            }
        }
        return Collections.EMPTY_LIST;
    }

    protected HeapWalkerNode computeLoopTo() {
        if (hasInstance()) {
            HeapWalkerNode parent = getParent();

            while (parent instanceof HeapWalkerInstanceNode) {
                if (((HeapWalkerInstanceNode) parent).getInstance().equals(instance)) {
                    return parent;
                }

                parent = parent.getParent();
            }
        }

        return null;
    }

    protected String computeName() {
        if (isLoop()) {
            return name + " " + Bundle.InstanceNode_LoopToString(BrowserUtils.getFullNodeName(getLoopTo()));
        }

        return name;
    }

    protected String computeType() {
        if (!hasInstance()) {
            return "<object>"; // NOI18N
        }

        return instance.getJavaClass().getName();
    }

    protected String computeValue() {
        if (!hasInstance()) {
            return "null"; // NOI18N
        }
        
        return "#" + instance.getInstanceNumber(); // NOI18N
    }
    
    protected void computeDetails() {
        HeapWalkerNode _root = BrowserUtils.getRoot(this);
        if (_root instanceof RootNode) {
            final RootNode root = (RootNode)_root;
            BrowserUtils.performTask(new Runnable() {
                public void run() {
                    final String d = root.getDetails(instance);
                    if (d != null) SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            details = d;
                            root.repaintView();
                        }
                    });
                }
            });
        }
    }

    protected String computeSize() {
        if (hasInstance()) return numberFormat.format(instance.getSize());
        else return "-"; // NOI18N
    }

    protected String computeRetainedSize() {
        if (hasInstance()) return numberFormat.format(instance.getRetainedSize());
        else return "-"; // NOI18N
    }

    protected ImageIcon processLoopIcon(ImageIcon icon) {
        if (!isLoop()) {
            return icon;
        }

        return BrowserUtils.createLoopIcon(icon);
    }
    
    private static ChangeListener setProgress(final ProgressHandle pHandle) {
        final BoundedRangeModel progress = HeapProgress.getProgress();
        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pHandle.progress(progress.getValue());
            }
        };
        progress.addChangeListener(cl);
        return cl;
    }
    
    public Object getNodeID() {
        return instance;
    }
}
