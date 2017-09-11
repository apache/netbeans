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
package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public class ClassPathProviderImplTest extends NbTestCase {
    
    private File root1, root2;
    
    public ClassPathProviderImplTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        root1 = FileUtil.normalizeFile(new File(getWorkDir(),"r1"));  //NOI18N
        root2 = FileUtil.normalizeFile(new File(getWorkDir(),"r2"));  //NOI18N
    }      
    
    public void testMockEvaluator() {
        final MockEvaluator eval = new MockEvaluator();
        eval.setProperty("javac.source", "1.8");    //NOI81N
        eval.setProperty("javac.target", "1.9");    //NOI18N
        assertEquals("1.8", eval.getProperty("javac.source"));  //NOI18N
        assertEquals("1.9", eval.getProperty("javac.target"));  //NOI18N
        assertEquals("1.8/1.9", eval.evaluate("${javac.source}/${javac.target}")); //NOI18N
        MockPropertyChangeListener l = new MockPropertyChangeListener("javac.source");  //NOI18N
        eval.addPropertyChangeListener(l);
        eval.setProperty("javac.source", "1.7");    //NOI18N        
        l.assertEvents("javac.source");             //NOI18N
        assertEquals("1.7", eval.getProperty("javac.source"));  //NOI18N
    }
    
    public void testSourceLevelSelector() {
        final MockEvaluator eval = new MockEvaluator();
        eval.setProperty("javac.source", "1.8");    //NOI81N
        final ClassPathProviderImpl.SourceLevelSelector s = new ClassPathProviderImpl.SourceLevelSelector(
                eval,
                "javac.source", //NOI18N
                new ArrayList<Supplier<? extends ClassPath>>() {
                    {
                        add(()->org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(root1)));
                        add(()->org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(root2)));
                    }
                }
        );
        final ClassPath cp18i1 = s.getActiveClassPath();
        assertEquals(
                Collections.singletonList(FileUtil.urlForArchiveOrDir(root1)),
                cp18i1.entries().stream()
                        .map((e)->e.getURL())
                        .collect(Collectors.toList()));
        final ClassPath cp18i2 = s.getActiveClassPath();
        assertTrue(cp18i1 == cp18i2);
        eval.setProperty("javac.source", "9");  //NOI18N
        final ClassPath cp9i1 = s.getActiveClassPath();
        assertEquals(
                Collections.singletonList(FileUtil.urlForArchiveOrDir(root2)),
                cp9i1.entries().stream()
                        .map((e)->e.getURL())
                        .collect(Collectors.toList()));
        final ClassPath cp9i2 = s.getActiveClassPath();
        assertTrue(cp9i1 == cp9i2);
        eval.setProperty("javac.source", "1.8");  //NOI18N
        final ClassPath cp18i3 = s.getActiveClassPath();
        assertTrue(cp18i1 == cp18i3);
    }
    
    
    private static final class MockEvaluator implements PropertyEvaluator {
        
        private final Map<String,String> props;
        private final PropertyChangeSupport listeners;
        
        MockEvaluator() {
            props = new HashMap<>();
            listeners = new PropertyChangeSupport(this);
        }

        @Override
        public String getProperty(String prop) {
            return props.get(prop);
        }

        @Override
        public String evaluate(String text) {
            final StringBuilder builder = new StringBuilder();
            StringBuilder inProp = null;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (inProp != null) {
                    if (c == '}') { //NOI18N
                        final String e = props.get(inProp.toString());
                        if (e == null) {
                            builder.append("${"+inProp+"}");    //NOI18N
                        } else {
                            builder.append(e);
                        }
                        inProp = null;
                    } else {
                        inProp.append(c);
                    }
                } else {
                    if (c == '$' && text.charAt(i+1) == '{') {  //NOI18N
                        inProp = new StringBuilder();
                        i++;
                    } else {
                        builder.append(c);
                    }
                }
            }
            return builder.toString();
        }

        @Override
        public Map<String, String> getProperties() {
            return new HashMap<>(props);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.removePropertyChangeListener(listener);
        }
        
        void setProperty(String key, String value) {
            final String old = props.get(key);
            props.put(key, value);
            listeners.firePropertyChange(key, old, value);
        }
        
        void remove(String key) {            
            final String old = props.remove(key);
            listeners.firePropertyChange(key, old, null);
        }        
    }
}
