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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.db.explorer.dlg;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Base class for the connection dialogs.
 *
 * @author Andrei Badea
 */
public abstract class ConnectionDialogMediator {
    
    public static final String PROP_VALID = "valid"; // NOI18N
    
    private final List<ConnectionProgressListener> connProgressListeners = new ArrayList<>();
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    
    private boolean valid = true;
    
    public void addConnectionProgressListener(ConnectionProgressListener listener) {
        synchronized (connProgressListeners) {
            connProgressListeners.add(listener);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void closeConnection()
    {
    }
    
    protected abstract boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema);

    /**
     * An async version of retrieveSchemas.
     * 
     * @param schemaPanel the schema panel
     * @param dbcon the db connection
     * @param defaultSchema the name of the default schema
     * 
     * @return the Task instance passed to the Requestprocessor
     */
    protected Task retrieveSchemasAsync(final SchemaPanel schemaPanel, final DatabaseConnection dbcon, final String defaultSchema)
    {
        fireConnectionStarted();
        
        Task task = RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                retrieveSchemas(schemaPanel, dbcon, defaultSchema);
                fireConnectionFinished();
            }
        });

        return task;
    }

    
    protected void fireConnectionStarted() {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionStarted();
        }
    }
    
    protected void fireConnectionStep(String step) {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionStep(step);
        }
    }
    
    protected void fireConnectionFinished() {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionFinished();
        }
    }

    protected void fireConnectionFailed() {
        for (Iterator<ConnectionProgressListener> i = connProgressListenersCopy(); i.hasNext();) {
            i.next().connectionFailed();
        }
    }
    
    private Iterator<ConnectionProgressListener> connProgressListenersCopy() {
        List<ConnectionProgressListener> listenersCopy = null;
        synchronized (connProgressListeners) {
            listenersCopy = new ArrayList<>(connProgressListeners);
        }
        return listenersCopy.iterator();
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
        propChangeSupport.firePropertyChange(PROP_VALID, null, null);
    }
    
    public boolean getValid() {
        return valid;
    }
}
