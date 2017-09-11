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

import java.io.File;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.ReadOnlyAccess;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel2 extends AbstractDocumentModel<TestComponent2> implements DocumentModel<TestComponent2> {
    TestComponent2 testRoot;
    ReadOnlyAccess access;
    
    /** Creates a new instance of TestModel */
    public TestModel2(Document doc) {
        super(Util.createModelSource(doc));
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public TestModel2(ModelSource source) {
        super(source);
    }
    
    private static Factory factory = null;
    public static Factory factory() {
        if (factory == null) {
            factory = new Factory();
        }
        return factory;
    }
    
    public static class Factory extends AbstractModelFactory<TestModel2> {
        private Factory() {
            super();
        }
        
        protected TestModel2 createModel(ModelSource source) {
            return new TestModel2(source);
        }


//        public TestModel2 getModel(ModelSource source) {
//            return super.getModel(source);
//        }
    }
    
    public TestComponent2 getRootComponent() {
        if (testRoot == null) {
            testRoot = new TestComponent2(this, "test");
        }
        return testRoot;
    }

    public void addChildComponent(Component target, Component child, int index) {
        TestComponent2 parent = (TestComponent2) target;
        TestComponent2 tc = (TestComponent2) child;
        parent.insertAtIndex(tc.getName(), tc, index > -1 ? index : parent.getChildrenCount());
    }

    public void removeChildComponent(Component child) {
        TestComponent2 tc = (TestComponent2) child;
        tc.getParent().removeChild(tc.getName(), tc);
    }

    
    public DocumentModelAccess getAccess() {
        if (access == null) {
            access = new ReadOnlyAccess(this);
        }
        return access;
    }

    public TestComponent2 createRootComponent(org.w3c.dom.Element root) {
        if (TestComponent2.NS_URI.equals(root.getNamespaceURI()) &&
            "test".equals(root.getLocalName())) {
                testRoot = new TestComponent2(this, root);
        } else {
            testRoot = null;
        }
        return testRoot;
    }
    
    public TestComponent2 createComponent(TestComponent2 parent, org.w3c.dom.Element element) {
        return TestComponent2.createComponent(this, parent, element);
    }
    
    protected ComponentUpdater<TestComponent2> getComponentUpdater() {
        return null;
    }
    
}
