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

package org.netbeans.core.windows.view.ui;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.*;
import org.netbeans.core.windows.view.ui.*;

import java.awt.Component;
import java.awt.Dimension;

/**
 * A dummy ViewElement implementation for MultiSplitPane testing.
 *
 * @author Stanislav Aubrecht
 */
class TestViewElement extends ViewElement {

    SplitTestComponent myComponent;
    int orientation;

    /** Creates a new instance of TestViewElement */
    public TestViewElement( int orientation, double resizeWeight ) {
        super( null, resizeWeight );
        this.myComponent = new SplitTestComponent();
        this.orientation = orientation;
    }
    
    public Component getComponent() {
        return myComponent;
    }
    
    /**
     * lets the visual components adjust to the current state.
     * @returns true if a change was performed.
     */
    public boolean updateAWTHierarchy(Dimension availableSpace) {
        return true;
    }
    
    int getSizeInSplit() {
        return orientation == JSplitPane.HORIZONTAL_SPLIT ? getComponent().getWidth() : getComponent().getHeight();
    }
    
    int getNonSplitSize() {
        return orientation != JSplitPane.HORIZONTAL_SPLIT ? getComponent().getWidth() : getComponent().getHeight();
    }

    void setMinSize( int minSize ) {
        myComponent.setMinimumSize( new Dimension( minSize, minSize ) );
    }
    
    private static class SplitTestComponent extends JComponent {
        public SplitTestComponent() {
            setMinimumSize( new Dimension( 0, 0 ) );
        }
    }
}
