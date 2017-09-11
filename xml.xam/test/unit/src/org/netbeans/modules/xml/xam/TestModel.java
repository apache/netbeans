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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel extends AbstractModel<TestComponent> implements Model<TestComponent> {
    TestComponent testRoot;
    TestAccess access;
    
    /** Creates a new instance of TestModel */
    public TestModel() {
        super(createModelSource());
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ModelSource createModelSource() {
        Lookup lookup = Lookups.fixed(new Object[] { } );
        return new ModelSource(lookup, true);
    }
    
    public TestComponent getRootComponent() {
        if (testRoot == null) {
            testRoot = new TestComponent(this, 0);
        }
        return testRoot;
    }

    public void addChildComponent(Component target, Component child, int index) {
        TestComponent parent = (TestComponent) target;
        TestComponent tc = (TestComponent) child;
        parent.insertAtIndex(tc.getName(), tc, index > -1 ? index : parent.getChildrenCount());
    }

    public void removeChildComponent(Component child) {
        TestComponent tc = (TestComponent) child;
        tc.getParent().removeChild(tc.getName(), tc);
    }

    
    public TestAccess getAccess() {
        if (access == null) { 
            access = new TestAccess();
        }
        return access;
    }
    
    public static class TestAccess extends ModelAccess {
        PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public void removeUndoableEditListener(UndoableEditListener listener) {
            //TODO
        }

        public void addUndoableEditListener(UndoableEditListener listener) {
            //TODO
        }

        public Model.State sync() throws IOException {
            return Model.State.VALID;
        }

        public void prepareForUndoRedo() {
        }

        Long lastFlushTime = null;
        public void flush() {
            Long currentTime = new Long(System.currentTimeMillis());
            pcs.firePropertyChange("flushed", lastFlushTime, currentTime);
            lastFlushTime = currentTime;
        }

        public void finishUndoRedo() {
        }
        
        public void addFlushListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removeFlushListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
    }
}
