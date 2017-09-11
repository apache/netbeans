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

package org.netbeans.spi.editor.highlighting.support;

import java.util.List;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * The default implementation of the <code>HighlightsContainer</code> interface.
 * It provides standard implementation of the methods for adding and removing
 * <code>HighlightsChangeListener</code>s and allows subclasses to notify listeners
 * by calling the <code>fireHighlightsChange</code> method.
 * 
 * @author Vita Stejskal
 */
public abstract class AbstractHighlightsContainer implements HighlightsContainer {
    
    private ListenerList<HighlightsChangeListener> listeners = new ListenerList<HighlightsChangeListener>();
    
    /** Creates a new instance of AbstractHighlightsContainer */
    protected AbstractHighlightsContainer() {
    }

    public abstract HighlightsSequence getHighlights(int startOffset, int endOffset);

    /**
     * Adds <code>HighlightsChangeListener</code> to this container.
     * 
     * @param listener The listener to add.
     */
    public final void addHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes <code>HighlightsChangeListener</code> to this container.
     * 
     * @param listener The listener to remove.
     */
    public final void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Notifies all registered listeners about a change in this container. The
     * area of a document where highlights changed is specified by the
     * <code>changeStartOffset</code> and <code>changeEndOffset</code> parameters.
     * 
     * @param changeStartOffset The starting offset of the changed area.
     * @param changeEndOffset The ending offset of the changed area.
     */
    protected final void fireHighlightsChange(int changeStartOffset, int changeEndOffset) {
        List<HighlightsChangeListener> targets;
        
        synchronized (listeners) {
            targets = listeners.getListeners();
        }
        
        if (targets.size() > 0) {
            HighlightsChangeEvent evt = new HighlightsChangeEvent(this, changeStartOffset, changeEndOffset);
            
            for(HighlightsChangeListener l : targets) {
                l.highlightChanged(evt);
            }
        }
    }
}
