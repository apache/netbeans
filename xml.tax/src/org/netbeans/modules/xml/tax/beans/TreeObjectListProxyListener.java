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
package org.netbeans.modules.xml.tax.beans;

import java.beans.*;
import java.util.*;

import org.netbeans.tax.*;

/**
 * This class listens on all members of object list and
 * joins all member events into this one source.
 * <p>
 * <pre>
 *   TreeObjectListProxyListener proxy =
 *       new TreeObjectListProxyListener(list);
 *   proxy.addPropertyChangeListener(WeakListener.propertyChange(this, proxy));
 * </pre>
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class TreeObjectListProxyListener implements PropertyChangeListener {

    private final TreeObjectList list;
    private PropertyChangeSupport changeSupport;
        
    /** Creates new TreeObjectListListener */
    public TreeObjectListProxyListener(TreeObjectList list) {
        this.list = list;
        list.addPropertyChangeListener(this);
        for (Iterator it = list.iterator(); it.hasNext();) {
            TreeObject next = (TreeObject) it.next();
            if (next != null) next.addPropertyChangeListener(this);
        }
    }

    /*
     * Update listening on list members, forward member events.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        String name = e.getPropertyName();
        Object source = e.getSource();
        
        if (source == list) {
            if (TreeObjectList.PROP_CONTENT_INSERT.equals(name)) {
                TreeObject newObject = (TreeObject)e.getNewValue();
                if (newObject != null) newObject.addPropertyChangeListener(this);
            } else if (TreeObjectList.PROP_CONTENT_REMOVE.equals(name)) {
                TreeObject oldObject = (TreeObject)e.getOldValue();
                if (oldObject != null) oldObject.removePropertyChangeListener(this);
            }
        }
        
        forward(e);
    }    
    
    /**
     */
    private void forward(final PropertyChangeEvent e) {
        if (changeSupport != null)
            changeSupport.firePropertyChange(e);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }
}
