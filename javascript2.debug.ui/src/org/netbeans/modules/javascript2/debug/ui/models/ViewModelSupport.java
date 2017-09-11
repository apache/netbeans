/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.debug.ui.models;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;


/**
 * @author ads
 *
 */
public abstract class ViewModelSupport {

    protected static final Object[] EMPTY_CHILDREN = new Object[]{};
    
    private CopyOnWriteArrayList<ModelListener> myListeners;
    
    protected ViewModelSupport() {
        myListeners = new CopyOnWriteArrayList<ModelListener>();
    }

    public final void addModelListener(ModelListener l) {
        myListeners.add(l);
    }

    public final void removeModelListener(ModelListener l) {
        myListeners.remove(l);
    }

    protected final void refresh() {
        fireChangeEvent(new TreeChanged(this));
    }

    protected final void fireChangeEvent(ModelEvent modelEvent) {
        for ( ModelListener listener : myListeners ) {
            listener.modelChanged(modelEvent);
        }
    }

    protected final void fireChangeEvents(ModelEvent[] events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }

    protected final void fireChangeEvents(Collection<ModelEvent> events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }
   
    
    public static String toHTML(String text) {
        return toHTML(text, false, false, null);
    }

    public static String toHTML(String text, boolean bold, boolean italics,
                                Color color) {
        if (text == null) return null;
        if (text.length() > 6 && text.substring(0, 6).equalsIgnoreCase("<html>")) {
            return text; // Already HTML
        }
        StringBuffer sb = new StringBuffer ();
        sb.append ("<html>");
        if (bold) sb.append ("<b>");
        if (italics) sb.append ("<i>");
        if (color == null) {
            color = UIManager.getColor("Table.foreground");
            if (color == null) {
                color = new JTable().getForeground();
            }
        }
        sb.append ("<font color=");
        sb.append (Integer.toHexString ((color.getRGB () & 0xffffff)));
        sb.append (">");
        text = text.replaceAll ("&", "&amp;");
        text = text.replaceAll ("<", "&lt;");
        text = text.replaceAll (">", "&gt;");
        sb.append (text);
        sb.append ("</font>");
        if (italics) sb.append ("</i>");
        if (bold) sb.append ("</b>");
        sb.append ("</html>");
        return sb.toString ();
    }
    
}
