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
package org.apache.tools.ant.module.bridge.impl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Input;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Permit secure handlers.
 */
public class InputOverride extends Input {
    
    private boolean secure;

    @Override public Handler createHandler() {
        return new HandlerImpl();
    }

    @Override public void execute() throws BuildException {
        if (secure) {
            NbInputHandler handler = (NbInputHandler) getProject().getInputHandler();
            handler.secure = true;
            try {
                super.execute();
            } finally {
                handler.secure = false;
            }
        } else {
            super.execute();
        }
    }

    public class HandlerImpl extends Handler {

        private Handler delegate;

        private Handler delegate() {
            if (delegate == null) {
                delegate = InputOverride.super.createHandler();
                delegate.setProject(getProject());
            }
            return delegate;
        }

        @Override public void setType(HandlerType type) {
            if (type.getValue().equals("secure")) {
                secure = true;
            } else if (type.getValue().equals("default")) {
                // ignore handler entirely
            } else {
                delegate().setType(type);
            }
        }

        @Override public void setRefid(String refid) {
            delegate().setRefid(refid);
        }

        @Override public void setClassname(String classname) {
            delegate().setClassname(classname);
        }

        @Override public void setClasspath(Path classpath) {
            delegate().setClasspath(classpath);
        }

        @Override public void setClasspathRef(Reference r) {
            delegate().setClasspathRef(r);
        }

        @Override public void setLoaderRef(Reference r) {
            delegate().setLoaderRef(r);
        }

    }

}
