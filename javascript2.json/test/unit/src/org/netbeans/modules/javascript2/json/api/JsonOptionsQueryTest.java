/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.json.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public final class JsonOptionsQueryTest extends NbTestCase {

    FileObject wd;

    public JsonOptionsQueryTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        assertNotNull(wd);
    }

    public void testNoImplementation() {
        MockLookup.setInstances();
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertFalse(res.isCommentSupported());
    }

    public void testSingleImplementation() {
        MockLookup.setInstances(new JsonOptsImpl(Boolean.TRUE));
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertTrue(res.isCommentSupported());
    }

    public void testMultipleImplementations1() {
        MockLookup.setInstances(
                new JsonOptsImpl(null),
                new JsonOptsImpl(Boolean.TRUE));
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertTrue(res.isCommentSupported());
    }

    public void testMultipleImplementations2() {
        MockLookup.setInstances(
                new JsonOptsImpl(Boolean.FALSE),
                new JsonOptsImpl(Boolean.TRUE));
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertFalse(res.isCommentSupported());
    }

    public void testChanges() {
        final JsonOptsImpl impl1 = new JsonOptsImpl(Boolean.FALSE);
        final JsonOptsImpl impl2 = new JsonOptsImpl(Boolean.TRUE);
        MockLookup.setInstances(
                impl1,
                impl2);
        final JsonOptionsQuery.Result res = JsonOptionsQuery.getOptions(wd);
        assertNotNull(res);
        assertFalse(res.isCommentSupported());
        final MockPropertyChangeListener l = new MockPropertyChangeListener(JsonOptionsQuery.Result.PROP_COMMENT_SUPPORTED);
        res.addPropertyChangeListener(l);
        impl1.setComments(null);
        l.assertEventCount(1);
        assertTrue(res.isCommentSupported());
    }

    private static final class JsonOptsImpl implements JsonOptionsQueryImplementation {
        private final PropertyChangeSupport listeners;
        private final ResImpl res;
        private Boolean comments;

        JsonOptsImpl(final Boolean comments) {
            this.comments = comments;
            this.listeners = new PropertyChangeSupport(this);
            this.res = new ResImpl();
        }

        void setComments(Boolean comments) {
            this.comments = comments;
            this.listeners.firePropertyChange(Result.PROP_COMMENT_SUPPORTED, null, null);
        }

        @Override
        public Result getOptions(FileObject file) {
            return res;
        }

        private class ResImpl implements Result {

            @Override
            public Boolean isCommentSupported() {
                return comments;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                listeners.addPropertyChangeListener(listener);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                listeners.removePropertyChangeListener(listener);
            }
        }
    }

}
