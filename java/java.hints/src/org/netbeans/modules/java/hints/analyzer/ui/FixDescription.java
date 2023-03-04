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

package org.netbeans.modules.java.hints.analyzer.ui;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class FixDescription {

    private final ErrorDescription err;
    private final Fix fix;
    private boolean selected;
    private final AtomicBoolean fixed = new AtomicBoolean();
    private final ChangeSupport cs = new ChangeSupport(this);

    public FixDescription(ErrorDescription err, Fix fix) {
        this.err = err;
        this.fix = fix;
    }

    public boolean isSelected() {
        return selected && !fixed.get();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        cs.fireChange();
    }

//    public Fix getFix() {
//        return fix;
//    }

    public String getText() {
        return fix.getText();
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    public void implement() throws Exception {
        final FileObject file = err.getFile();
        if (!file.canWrite()) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(FixDescription.class, "CTL_File_Not_Writable", file.getNameExt()), //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return ;
        }

        fix.implement();
        fixed.set(true);
        cs.fireChange();
    }
    
    public boolean isFixed() {
        return fixed.get();
    }
    
    public ErrorDescription getErrors() {
        return err;
    }
}
