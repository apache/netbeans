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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Panel that changes its hight automatically if the components inside
 * it cannot fit in the current size. The panel checks and changes only its
 * height, not width. The panel changes not only height of its own but also
 * height of the toplevel <code>Window</code> it is embedded into. The size
 * change occurs only after this panel's children are <em>painted</em>.
 * <p>
 * This panel is supposed to be used as a replacement for a normal
 * <code>JPanel</code> if this panel contains a wrappable text and the panel
 * needs to be high enough so that all lines of the possibly wrapped text
 * can fit.
 * <p>
 * This class overrides method <code>paintChildren(Graphics)</code>.
 * If overriding this method in this subclasses of this class,
 * call <code>super.paintChildren(...)</code> so that the routine which performs
 * the size change is not skipped.
 *
 * @author Marian Petras
 */
public class SelfResizingPanel extends JPanel {
    
    /**
     * <code>false</code> until this panel's children are painted
     * for the first time
     */
    private boolean painted = false;
    
    /** Creates a new instance of SelfResizingPanel */
    public SelfResizingPanel() {
        super();
    }
    
    /**
     * Paints this panel's children and then displays the initial message
     * (in the message area) if any.
     * This method is overridden so that this panel receives a notification
     * immediately after the children components are painted - it is necessary
     * for computation of space needed by the message area for displaying
     * the initial message.
     * <p>
     * The first time this method is called, method
     * {@link #paintedFirstTime is called immediately after
     * <code>super.paintChildren(..>)</code> finishes.
     *
     * @param  g  the <code>Graphics</code> context in which to paint
     */
    protected void paintChildren(java.awt.Graphics g) {
        
        /*
         * This is a hack to make sure that window size adjustment
         * is not done sooner than the text area is painted.
         *
         * The reason is that the window size adjustment routine
         * needs the text area to compute height necessary for displaying
         * the given message. But the text area does not return correct
         * data (Dimension getPreferredSize()) until it is painted.
         */
        
        super.paintChildren(g);
        if (!painted) {
            paintedFirstTime(g);
            painted = true;
        }
    }
    
    /**
     * This method is called the first time this panel's children are painted.
     * By default, this method just calls {@link #adjustWindowSize()}.
     *
     * @param  g  <code>Graphics</code> used to paint this panel's children
     */
    protected void paintedFirstTime(java.awt.Graphics g) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                adjustWindowSize();
            }
        });
    }
    
    /**
     * Checks whether the dialog is large enough for the message (if any)
     * to be displayed and adjusts the dialogs size if it is too small.
     * <p>
     * Note: Resizing the dialog works only once this panel and its children
     * are {@linkplain #paintChildren(java.awt.Graphics) painted}.
     */
    protected void adjustWindowSize() {
        Dimension currSize = getSize();
        int currHeight = currSize.height;
        int prefHeight = getPreferredSize().height;
        if (currHeight < prefHeight) {
            int delta = prefHeight - currHeight;
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            Dimension winSize = win.getSize();
            win.setSize(winSize.width, winSize.height + delta);
        }
    }
    
    /**
     * Has this panel's children been already painted?
     *
     * @return  <code>true</code> if
     *          {@link #paintChildren #paintChildren(Graphics) has already been
     *          called; <code>false</code> otherwise
     * @see  #paintedFirstTime
     */
    protected boolean isPainted() {
        return painted;
    }
    
}
