/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.browser.ui.picker;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author S. Aubrecht
 */
final class SelectionModel {

    private final ListSelectionListener selectionListener;
    private final ArrayList<SelectionList> lists = new ArrayList<>( 10 );
    private final ChangeSupport changeSupport = new ChangeSupport( this );

    SelectionModel() {
        selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                onSelectionChange( e );
            }
        };
    }

    void add( SelectionList sl ) {
        synchronized( lists ) {
            if( lists.contains( sl ) )
                return;
            boolean hasSelection = null != getSelectedItem();
            lists.add( sl );
            if( hasSelection )
                sl.clearSelection();
            sl.getSelectionModel().addListSelectionListener( selectionListener );
        }
    }

    public ListItem getSelectedItem() {
        synchronized( lists ) {
            for( SelectionList sl : lists ) {
                ListItem sel = sl.getSelectedItem();
                if( null != sel )
                    return sel;
            }
            return null;
        }
    }

    public void setSelectedItem( ListItem item ) {
        assert SwingUtilities.isEventDispatchThread();
        synchronized( lists ) {
            if( null == item ) {
                for( SelectionList sl : lists ) {
                    sl.clearSelection();
                }
            } else {
                for( SelectionList sl : lists ) {
                    if( sl.setSelectedItem( item ) ) {
                        return;
                    }
                }
            }
        }
        //or just ignore silently?
        throw new IllegalArgumentException();
    }

    public void addChangeListener( ChangeListener cl ) {
        changeSupport.addChangeListener( cl );
    }

    public void removeChangeListener( ChangeListener cl ) {
        changeSupport.removeChangeListener( cl );
    }

    private boolean ignoreSelectionEvents = false;
    private void onSelectionChange( ListSelectionEvent e ) {
        if( e.getValueIsAdjusting() )
            return;
        if( ignoreSelectionEvents ) {
            return;
        }
        ignoreSelectionEvents = true;
        synchronized( lists ) {
            for( SelectionList sl : lists ) {
                if( sl.getSelectionModel() == e.getSource() )
                    continue;
                sl.clearSelection();
            }
        }
        ignoreSelectionEvents = false;
        changeSupport.fireChange();
    }
}
