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

package org.netbeans.spi.diff;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Reader;

//import org.openide.util.Lookup;

import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;

/**
 * This class represents a diff visualizer. It's used as a presenter of a visual
 * representation of the source differences to the user.
 * <p>The registered Diff Visualizers can be obtained via {@link org.openide.util.Lookup}
 * (e.g. you can get the default diff provider by
 *  <code>Lookup.getDefault().lookup(DiffVisualizer.class)</code>)
 *
 * @author  Martin Entlicher
 */
public abstract class DiffVisualizer extends Object {

    /*
    public static DiffVisualizer getDefault() {
        return (DiffVisualizer) Lookup.getDefault().lookup(DiffVisualizer.class);
    }
     */
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param diffs The list of differences.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The Component representing the diff visual representation
     *         or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Component createView(Difference[] diffs, String name1, String title1,
                                         Reader r1, String name2, String title2,
                                         Reader r2, String MIMEType) throws IOException ;

    /**
     * Creates single-window diff component that does not include any navigation controls and
     * is controlled programatically via the returned DiffView interface.
     * <p>
     * The StreamSource can be used to save the source content if it's modified
     * in the view. The view should not allow source modification if StreamSource.createWriter()
     * returns <code>null</code>.
     * 
     * @param diffs The list of differences.
     * @param s1 the first source
     * @param s2 the second source
     * @return DiffView controller interface
     *
     */ 
    public DiffView createDiff(Difference[] diffs, StreamSource s1, StreamSource s2) throws IOException {
        final Component c = createView(diffs, s1.getName(), s1.getTitle(), s1.createReader(),
                                       s2.getName(), s2.getTitle(), s2.createReader(),
                                       s1.getMIMEType());
        final int n = diffs.length;
        return new DiffView() {
            
            public Component getComponent() {
                return c;
            }
    
            public int getDifferenceCount() {
                return n;
            }
    
            public boolean canSetCurrentDifference() {
                return false;
            }

            public void setCurrentDifference(int diffNo) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
    
            public int getCurrentDifference() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
            
            public javax.swing.JToolBar getToolBar() {
                return null;
            }
   
            public void addPropertyChangeListener(PropertyChangeListener l) {}
    
            public void removePropertyChangeListener(PropertyChangeListener l) {}
    
        };
    }
}
