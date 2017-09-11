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

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.Color;
import java.awt.Component;
//import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.DiffModuleConfig;
import org.netbeans.spi.diff.MergeVisualizer;

/**
 * The default graphical visualizer of merge conflicts.
 *
 * @author  Martin Entlicher
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.diff.MergeVisualizer.class)
public class GraphicalMergeVisualizer extends MergeVisualizer implements Serializable {
    
    private Color colorUnresolvedConflict = DiffModuleConfig.getDefault().getUnresolvedColor();
    private Color colorResolvedConflict = DiffModuleConfig.getDefault().getAppliedColor();
    private Color colorOtherConflict = DiffModuleConfig.getDefault().getNotAppliedColor();
    
    /** The currently opened merge dialog. */
    private MergeDialogComponent merge;
    
    static final long serialVersionUID =-2175410667258166512L;
    /** Creates a new instance of GraphicalMergeVisualizer */
    public GraphicalMergeVisualizer() {
        merge = null;
        //System.out.println("Created a new GraphicalMergeVisualizer() = "+this);
    }
    
    /**
     * Get the display name of this merge visualizer.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(GraphicalMergeVisualizer.class, "GraphicalMergeVisualizer.displayName");
    }
    
    /**
     * Get a short description of this merge visualizer.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(GraphicalMergeVisualizer.class, "GraphicalMergeVisualizer.shortDescription");
    }
    
    /**
     * Show the visual representation of the merging process of two sources.
     * The result of the merging process can be saved into a Writer even
     * before all conflicts are actually resolved.
     *
     * @param diffs The list of conflicts.
     * @param source1 the source of the first file
     * @param source2 the source of the second file
     * @param result the information about the result source
     * @return The Component representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public Component createView(Difference[] diffs, StreamSource source1,
                                StreamSource source2, StreamSource result) throws IOException {
        synchronized (this) {
            //System.out.println("createView(): merge = "+merge);
            if (merge == null) {
                Set opened = TopComponent.getRegistry().getOpened();
                for (Iterator it = opened.iterator(); it.hasNext(); ) {
                    Object component = it.next();
                    if (component instanceof MergeDialogComponent) {
                        merge = (MergeDialogComponent) component;
                        break;
                    }
                }
                if (merge == null) {
                    merge = new MergeDialogComponent();
                }
            }
        }
        if (!merge.isOpened()) merge.open();
        
        MergePanel panel = new MergePanel();
        MergeControl control = new MergeControl(panel);
        control.initialize(diffs, source1, source2, result,
                           colorUnresolvedConflict, colorResolvedConflict,
                           colorOtherConflict);
        merge.addVetoableChangeListener(WeakListeners.vetoableChange(control, merge));
        merge.addMergePanel(panel);
        return merge;
    }
    
    /** Getter for property colorUnresolvedConflict.
     * @return Value of property colorUnresolvedConflict.
     */
    public java.awt.Color getColorUnresolvedConflict() {
        return colorUnresolvedConflict;
    }
    
    /** Setter for property colorUnresolvedConflict.
     * @param colorUnresolvedConflict New value of property colorUnresolvedConflict.
     */
    public void setColorUnresolvedConflict(java.awt.Color colorUnresolvedConflict) {
        this.colorUnresolvedConflict = colorUnresolvedConflict;
    }
    
    /** Getter for property colorResolvedConflict.
     * @return Value of property colorResolvedConflict.
     */
    public java.awt.Color getColorResolvedConflict() {
        return colorResolvedConflict;
    }
    
    /** Setter for property colorResolvedConflict.
     * @param colorResolvedConflict New value of property colorResolvedConflict.
     */
    public void setColorResolvedConflict(java.awt.Color colorResolvedConflict) {
        this.colorResolvedConflict = colorResolvedConflict;
    }
    
    /** Getter for property colorOtherConflict.
     * @return Value of property colorOtherConflict.
     */
    public java.awt.Color getColorOtherConflict() {
        return colorOtherConflict;
    }
    
    /** Setter for property colorOtherConflict.
     * @param colorOtherConflict New value of property colorOtherConflict.
     */
    public void setColorOtherConflict(java.awt.Color colorOtherConflict) {
        this.colorOtherConflict = colorOtherConflict;
    }
    
}
