/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.spi;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import static org.netbeans.modules.progress.spi.InternalHandle.STATE_INITIALIZED;
import org.netbeans.progress.module.TrivialProgressUIWorkerProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * UI Counterpart of the {@link InternalHandle}. 
 * 
 * @author sdedic
 */
public final class UIInternalHandle extends InternalHandle {
    private static final Logger LOG = Logger.getLogger(UIInternalHandle.class.getName());
    
    private final Action viewAction;
    private ExtractedProgressUIWorker component;
    private boolean customPlaced1 = false;
    private boolean customPlaced2 = false;
    private boolean customPlaced3 = false;

    public UIInternalHandle(String displayName, 
                   Cancellable cancel,
                   boolean userInitiated,
                   Action view) {
        super(displayName, cancel, userInitiated);
        viewAction = view;
    }

    public boolean isAllowCancel() {
        return super.isAllowCancel() && !isCustomPlaced();
    }
    
    public boolean isAllowView() {
        return viewAction != null && !isCustomPlaced();
    }

    public boolean isCustomPlaced() {
        return component != null;
    }
    
    public void requestView() {
        if (!isAllowView()) {
            return;
        }
        viewAction.actionPerformed(new ActionEvent(viewAction, ActionEvent.ACTION_PERFORMED, "performView"));
    }

    private void createExtractedWorker() {
        if (component == null) {
            ProgressUIWorkerProvider prov = Lookup.getDefault().lookup(ProgressUIWorkerProvider.class);
            if (prov == null) {
                LOG.log(Level.CONFIG, "Using fallback trivial progress implementation");
                prov = new TrivialProgressUIWorkerProvider();
            }
            component = prov.getExtractedComponentWorker();
            setController(new SwingController(component));
        }
    }

    /**
     * have the component in custom location, don't include in the status bar.
     */
    public synchronized JComponent extractComponent() {
        if (customPlaced1) {
            throw new IllegalStateException("Cannot retrieve progress component multiple times");
        }
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced1 = true;
        createExtractedWorker();
        return component.getProgressComponent();
    }
    
    public synchronized JLabel extractDetailLabel() {
        if (customPlaced2) {
            throw new IllegalStateException("Cannot retrieve progress detail label component multiple times");
        }
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced2 = true;
        createExtractedWorker();
        return component.getDetailLabelComponent();
    }

    public synchronized JLabel extractMainLabel() {
        if (customPlaced3) {
            throw new IllegalStateException("Cannot retrieve progress main label component multiple times");
        }
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced3 = true;
        createExtractedWorker();
        return component.getMainLabelComponent();
    }

}
