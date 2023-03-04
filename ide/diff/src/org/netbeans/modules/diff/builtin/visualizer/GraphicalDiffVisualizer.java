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

package org.netbeans.modules.diff.builtin.visualizer;

import java.awt.Color;
import java.awt.Component;
import java.io.Reader;
import java.io.Serializable;

import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffVisualizer;
import org.openide.DialogDisplayer;

/**
 * The default graphical visualizer of diffs.
 *
 * @author  Martin Entlicher
 */
public class GraphicalDiffVisualizer extends DiffVisualizer implements Serializable {

    private Color colorAdded = DiffComponent.COLOR_ADDED;
    private Color colorMissing = DiffComponent.COLOR_MISSING;
    private Color colorChanged = DiffComponent.COLOR_CHANGED;
    
    static final long serialVersionUID =-1135210647457196211L;
    /** Creates a new instance of BuiltInDiffVisualizer */
    public GraphicalDiffVisualizer() {
    }
    
    /**
     * Get the display name of this diff visualizer.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(GraphicalDiffVisualizer.class, "GraphicalDiffVisualizer.displayName");
    }
    
    /**
     * Get a short description of this diff visualizer.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(GraphicalDiffVisualizer.class, "GraphicalDiffVisualizer.shortDescription");
    }
    
    
    /**
     * Some diff visualizers may have built-in the diff calculation. In such a case
     * the visualizer does not need any diff provider.
     * @return true when it relies on differences supplied, false if not.
     *
    public boolean needsProvider() {
        return true;
    }
     */
    
    /**
     * Show the visual representation of the diff between two files.
     * @param diffs The list of differences (instances of {@link Difference}).
     *       may be <code>null</code> in case that it does not need diff provider.
     * @param fo1 the first FileObject
     * @param fo2 the second FileObject compared with the first one.
     * @return The TopComponent representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     *
    public Component showDiff(List diffs, FileObject fo1, FileObject fo2) {
        DiffComponent diff;
        try {
            diff = new DiffComponent(diffs, null, fo1.getMIMEType(),
                fo1.getName(), fo2.getName(),
                fo1.getPackageNameExt('/', '.'), fo2.getPackageNameExt('/', '.'),
                new InputStreamReader(fo1.getInputStream()),
                new InputStreamReader(fo2.getInputStream()));
        } catch (FileNotFoundException fnfex) {
            org.openide.TopManager.getDefault().notifyException(fnfex);
            return null;
        }
        return diff;
    }
     */
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param diffs The list of differences (instances of {@link Difference}).
     *       may be <code>null</code> in case that it does not need diff provider.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The TopComponent representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     */
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1,
                                String name2, String title2, Reader r2, String MIMEType) {
        if (diffs.length == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GraphicalDiffVisualizer.class, "MSG_NoDifference", name1, name2)));
        }
        DiffComponent diff;
        String componentName = name1;
        if (name2 != null && name2.length() > 0) componentName = NbBundle.getMessage(
                GraphicalDiffVisualizer.class, "MSG_TwoFilesDiffTitle", componentName, name2);            
        diff = new DiffComponent(diffs, componentName, MIMEType,
            name1, name2, title1, title2, r1, r2,
            new Color[] { colorMissing, colorAdded, colorChanged });
        return diff;
    }
    
    /** Getter for property colorAdded.
     * @return Value of property colorAdded.
     */
    public java.awt.Color getColorAdded() {
        return colorAdded;
    }
    
    /** Setter for property colorAdded.
     * @param colorAdded New value of property colorAdded.
     */
    public void setColorAdded(java.awt.Color colorAdded) {
        this.colorAdded = colorAdded;
    }
    
    /** Getter for property colorMissing.
     * @return Value of property colorMissing.
     */
    public java.awt.Color getColorMissing() {
        return colorMissing;
    }
    
    /** Setter for property colorMissing.
     * @param colorMissing New value of property colorMissing.
     */
    public void setColorMissing(java.awt.Color colorMissing) {
        this.colorMissing = colorMissing;
    }
    
    /** Getter for property colorChanged.
     * @return Value of property colorChanged.
     */
    public java.awt.Color getColorChanged() {
        return colorChanged;
    }
    
    /** Setter for property colorChanged.
     * @param colorChanged New value of property colorChanged.
     */
    public void setColorChanged(java.awt.Color colorChanged) {
        this.colorChanged = colorChanged;
    }
    
}
