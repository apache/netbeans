/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.loaders;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.Action;
import org.netbeans.api.actions.Savable;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Enumerations;

/**
 */
public class SavableDataObjectTest extends NbTestCase {
    public SavableDataObjectTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(Pool.class);
    }
    
    public void testHowManySavables() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject test = root.createData("test.save");
        
        DataObject obj = DataObject.find(test);
        assertEquals("Right type", SavaObj.class, obj.getClass());
        
        Action a = findSaveAction().createContextAwareInstance(obj.getNodeDelegate().getLookup());
        Action all = findSaveAllAction();
        assertFalse("Disabled at first", a.isEnabled());
        assertFalse("All Disabled at first", all.isEnabled());
        
        obj.setModified(true);
        
        assertTrue("Enabled", a.isEnabled());
        assertTrue("All enabled too", all.isEnabled());
        
        assertEquals("One modified object", 1, Savable.REGISTRY.lookupAll(Savable.class).size());
        assertTrue("Old registry contains it as well", DataObject.getRegistry().getModifiedSet().contains(obj));
        
        all.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("One save", 1, ((SavaObj)obj).handleSave);
        
        assertFalse("Disabled at end", a.isEnabled());
        assertFalse("All Disabled at end", all.isEnabled());
        
    }
    
    
    private static ContextAwareAction findSaveAction() {
        Action a = Actions.forID("System", "org.openide.actions.SaveAction");
        assertTrue(a instanceof ContextAwareAction);
        return (ContextAwareAction) a;
    }
    private static Action findSaveAllAction() {
        Action a = Actions.forID("System", "org.openide.actions.SaveAllAction");
        assertNotNull("action found: " + a, a);
        return a;
    }
    
    public static final class Pool extends DataLoaderPool {
        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(SavaLoader.getLoader(SavaLoader.class));
        }
    } // end of Pool
    
    public static final class SavaLoader extends UniFileLoader {
        public SavaLoader() {
            super(SavaObj.class.getName());
        }
        
        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("save");
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SavaObj(primaryFile, this);
        }
    }
    
    private static final class SavaObj extends MultiDataObject {
        int handleSave;
        
        public SavaObj(FileObject pf, SavaLoader l) throws DataObjectExistsException {
            super(pf, l);
        }

        @Override
        public void setModified(boolean modif) {
            if (modif) {
                getCookieSet().assign(Savable.class, new SaveMe());
            } else {
                SaveMe prev = getCookieSet().getLookup().lookup(SaveMe.class);
                getCookieSet().assign(Savable.class);
                if (prev != null) {
                    prev.discard();
                }
            }
            super.setModified(modif);
        }
        
        private class SaveMe extends AbstractSavable {
            public SaveMe() {
                register();
            }
            
            @Override
            public String findDisplayName() {
                return "SaveMe";
            }

            @Override
            protected void handleSave() throws IOException {
                handleSave++;
                setModified(false);
            }
            
            private DataObject obj() {
                return SavaObj.this;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof SaveMe) {
                    SaveMe other = (SaveMe)obj;
                    return obj().equals(other.obj());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return obj().hashCode();
            }

            final void discard() {
                unregister();
            }
        }
    }
}
