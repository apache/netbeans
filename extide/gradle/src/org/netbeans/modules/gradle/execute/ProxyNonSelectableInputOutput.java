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

package org.netbeans.modules.gradle.execute;

import java.io.Reader;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * workarounds execution API which at some point in time calls select() on the IO.
 * @author mkleint
 */
public class ProxyNonSelectableInputOutput implements InputOutput {
    private final InputOutput delegate;

    public ProxyNonSelectableInputOutput(InputOutput delegate) {
        this.delegate = delegate;
    }

    @Override
    public OutputWriter getOut() {
        return delegate.getOut();
    }

    @Override
    public Reader getIn() {
        return delegate.getIn();
    }

    @Override
    public OutputWriter getErr() {
        return delegate.getErr();
    }

    @Override
    public void closeInputOutput() {
        delegate.closeInputOutput();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public void setOutputVisible(boolean value) {
        delegate.setOutputVisible(value);
    }

    @Override
    public void setErrVisible(boolean value) {
        delegate.setErrVisible(value);
    }

    @Override
    public void setInputVisible(boolean value) {
        delegate.setInputVisible(value);
    }

    @Override
    public void select() {
        //do not delegate!
    }

    @Override
    public boolean isErrSeparated() {
        return delegate.isErrSeparated();
    }

    @Override
    public void setErrSeparated(boolean value) {
        delegate.setErrSeparated(value);
    }

    @Override
    public boolean isFocusTaken() {
        return delegate.isFocusTaken();
    }

    @Override
    public void setFocusTaken(boolean value) {
        delegate.setFocusTaken(value);
    }

    @Override
    @Deprecated
    public Reader flushReader() {
        return delegate.flushReader();
    }

}
