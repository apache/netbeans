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
