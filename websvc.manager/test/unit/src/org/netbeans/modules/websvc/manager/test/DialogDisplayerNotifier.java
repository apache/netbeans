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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.manager.test;

import java.awt.Dialog;
import java.util.LinkedList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author quynguyen
 */
public class DialogDisplayerNotifier extends DialogDisplayer {
    private LinkedList<WebServiceManagerListener> listeners = new LinkedList<WebServiceManagerListener>();
    
    public void addListener(WebServiceManagerListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(WebServiceManagerListener listener) {
        listeners.remove(listener);
    }
    
    public void removeAllListeners() {
        listeners.clear();
    }
    
    @Override
    public Object notify(NotifyDescriptor descriptor) {
        for (WebServiceManagerListener listener : listeners) {
            listener.eventFired(new WebServiceManagerEvent((String)descriptor.getMessage()));
        }
        return NotifyDescriptor.OK_OPTION;
    }

    @Override
    public Dialog createDialog(DialogDescriptor descriptor) {
        return null;
    }

}
