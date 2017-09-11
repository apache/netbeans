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

package org.netbeans.modules.diff;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

import org.netbeans.junit.*;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;

/**
 *
 * @author Martin Entlicher
 */
public abstract class DiffViewAbstract extends NbTestCase {
    /** the DiffView to work on */
    private DiffView dv;

    public DiffViewAbstract(String name) {
        super(name);
    }

    protected abstract DiffView createDiffView(StreamSource ss1, StreamSource ss2) throws IOException;
    
    protected void setUp() throws Exception {
        MockServices.setServices(BuiltInDiffProvider.class);
        dv = createDiffView(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
        final boolean[] finished = new boolean[1];
        dv.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                dv.removePropertyChangeListener(this);
                finished[0] = true;
            }
        });
        for (int i = 0; i < 10 && !finished[0]; ++i) {
            Thread.sleep(1000);
        }
    }
    
    public void testCanDiffConsistent() throws Exception {
        if (dv.canSetCurrentDifference()) {
            dv.setCurrentDifference(0);
        } else {
            try {
                dv.setCurrentDifference(0);
                fail("Should throw UnsupportedOperationException");
            } catch (UnsupportedOperationException uoex) {
                // OK
            }
        }
    }
    
    public void testCurrentDifference() throws Exception {
        if (dv.canSetCurrentDifference()) {
            int dc = dv.getDifferenceCount();
            assertEquals("Just one difference", 2, dc);
            dv.setCurrentDifference(1);
            assertEquals("Test current difference", 1, dv.getCurrentDifference());
//            try {
//                dv.setCurrentDifference(10);
//                fail("Should report an exception.");
//            } catch (IllegalArgumentException ioex) {
//                // OK
//            }
        }
    }
    
    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private String name;
        private String title;
        private String MIMEType;
        private Reader r;
        private String buffer;
        private Writer w;

        Impl(String name, String title, String MIMEType, String str) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.w = null;
            buffer = str;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getMIMEType() {
            return MIMEType;
        }

        public Reader createReader() throws IOException {
            return new StringReader(buffer);
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
    
}
