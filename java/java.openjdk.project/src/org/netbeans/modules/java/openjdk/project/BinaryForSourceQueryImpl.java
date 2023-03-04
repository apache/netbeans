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
package org.netbeans.modules.java.openjdk.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;

/**
 *
 * @author lahvac
 */
public class BinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {

    private static final Logger LOG = Logger.getLogger(BinaryForSourceQueryImpl.class.getName());

    private final Result result;
    private final ClassPath sourceCP;

    public BinaryForSourceQueryImpl(JDKProject project, ClassPath sourceCP) {
        this.result = new ResultImpl("${outputRoot}/jdk/modules/${module}", project.evaluator());
        this.sourceCP = sourceCP;
    }

    @Override
    public Result findBinaryRoots(URL sourceRoot) {
        FileObject r = URLMapper.findFileObject(sourceRoot);
        if (Arrays.asList(sourceCP.getRoots()).contains(r)) //TODO: faster
            return result;
        return null;
    }

    private static final class ResultImpl implements Result, PropertyChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final String template;
        private final PropertyEvaluator evaluator;

        public ResultImpl(String template, PropertyEvaluator evaluator) {
            this.template = template;
            this.evaluator = evaluator;
            this.evaluator.addPropertyChangeListener(this);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public URL[] getRoots() {
            try {
                return new URL[] {
                    new URL(evaluator.evaluate(template))
                };
            } catch (MalformedURLException ex) {
                LOG.log(Level.FINE, null, ex);
                return new URL[0];
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            cs.fireChange();
        }

    }

}
