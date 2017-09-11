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
package org.openide.awt;

import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;


/** Permits control of a status line.
 * The default instance may correspond to the NetBeans status line in the main window.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class StatusDisplayer {

    /**
     * Default message 'importance' for file annotations.
     */
    public static final int IMPORTANCE_ANNOTATION = 1000;
    /**
     * Default message 'importance' for messages from incremental find.
     */
    public static final int IMPORTANCE_INCREMENTAL_FIND = 900;
    /**
     * Default message 'importance' for messages from find and replace actions.
     */
    public static final int IMPORTANCE_FIND_OR_REPLACE = 800;
    /**
     * Default message 'importance' for error and warning messages on current line.
     */
    public static final int IMPORTANCE_ERROR_HIGHLIGHT = 700;

    private static StatusDisplayer INSTANCE = null;

    /** Subclass constructor. */
    protected StatusDisplayer() {
    }

    /** Get the default status displayer.
     * @return the default instance from lookup
     */
    public static synchronized StatusDisplayer getDefault() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(StatusDisplayer.class);

            if (INSTANCE == null) {
                INSTANCE = new Trivial();
            }
        }

        return INSTANCE;
    }

    /** Get the currently displayed text.
     * <p>Modules should <strong>not</strong> need to call this method.
     * If you think you really do, please explain why on nbdev.
     * The implementation of the GUI component (if any) which displays
     * the text naturally needs to call it.
     * @return some text
     */
    public abstract String getStatusText();

    /** Show text in the status line.
     * Can be called at any time, but remember the text may not be updated
     * until the AWT event queue is ready for it - so if you are hogging
     * the event queue the text will not appear until you release it
     * (finish your work or display a modal dialog, for example).
     *  <p class="nonnormative">Default implementation of status line in NetBeans
     * displays the text in status line and clears it after a while. 
     * Also there is no guarantee how long the text will be displayed as 
     * it can be replaced with new call to this method at any time.</p>
     * <p>Note: The text may not show in the status line at all if some
     * other text with higher importance is currently showing in the status line
     * as status messages displayed this way have zero <code>importance</code>.
     * The message will show when higher-priority message has been cleared (either
     * explicitly or after garbage collect).</p>
     * @param text the text to be shown
     * @see #setStatusText(String,int)
     */
    public abstract void setStatusText(String text);

    /**
     * <p>Show text in the status line. <code>importance</code> argument
     * indicates that the text should stay in the status line until it is replaced
     * with new text by calling <code>setStatusText(String,int)</code> again with
     * the same or higher importance value.</p>
     * <p>The text will be removed from status line when this method's return value is
     * garbage-collected or explicitly by calling <code>Message.clear(int)</code>.
     * @param text The text to be shown until some other text with the same or higher
     * importance is passed into the status line.
     * @param importance Positive integer defining the 'Importance' of the message
     * to be displayed, the higher number the higher importance.
     * @return Handle associated with given status line text.
     * @throws IllegalArgumentException If importance <= 0
     * @since 7.5
     */
    public Message setStatusText(final String text, int importance) {
        if( importance <= 0 )
            throw new IllegalArgumentException("Invalid importance value: " + importance);
        setStatusText(text);
        return new Message() {
            public void clear(int timeInMillis) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if( text == getStatusText() )
                            setStatusText("");
                    }
                }, timeInMillis);
            }

            @Override
            protected void finalize() throws Throwable {
                if( text == getStatusText() )
                    setStatusText("");
            }
        };
    }

    /** Add a listener for when the text changes.
     * @param l a listener
     */
    public abstract void addChangeListener(ChangeListener l);

    /** Remove a listener for the text.
     * @param l a listener
     */
    public abstract void removeChangeListener(ChangeListener l);

    /**
     * Handle for 'important' status line messages. The message will be removed
     * from status line when this object is garbage-collected.
     *
     * @see #setStatusText(String,int)
     * @since 7.5
     */
    public static interface Message {
        /**
         * Removes this message from status line after <code>timeInMillis</code>
         * milliseconds. The StatusDisplayer fires a <code>ChangeEvent</code> when the message
         * is cleared.
         * @param timeInMillis
         */
        void clear(int timeInMillis);
    }

    /**
     * Trivial default impl for standalone usage.
     * @see "#32154"
     */
    private static final class Trivial extends StatusDisplayer {
        private final ChangeSupport cs = new ChangeSupport(this);
        private String text = ""; // NOI18N

        public synchronized String getStatusText() {
            return text;
        }

        public synchronized void setStatusText(String text) {
            if (text.equals(this.text)) {
                return;
            }

            this.text = text;

            if (text.length() > 0) {
                System.err.println("(" + text + ")"); // NOI18N
            }

            cs.fireChange();
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }
}
