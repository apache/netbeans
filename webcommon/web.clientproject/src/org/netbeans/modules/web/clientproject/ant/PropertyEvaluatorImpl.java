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
package org.netbeans.modules.web.clientproject.ant;

import java.beans.PropertyChangeListener;
import java.util.Map;
import org.netbeans.modules.web.clientproject.env.Values;

final class PropertyEvaluatorImpl implements Values {
    final org.netbeans.spi.project.support.ant.PropertyEvaluator delegate;

    public PropertyEvaluatorImpl(org.netbeans.spi.project.support.ant.PropertyEvaluator d) {
        this.delegate = d;
    }

    @Override
    public String getProperty(String prop) {
        return delegate.getProperty(prop);
    }

    @Override
    public String evaluate(String text) {
        return delegate.evaluate(text);
    }

    @Override
    public Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }
}
