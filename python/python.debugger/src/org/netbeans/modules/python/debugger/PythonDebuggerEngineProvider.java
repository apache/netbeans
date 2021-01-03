/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.debugger;

import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.api.debugger.DebuggerEngine;

/**
 * Netbeans Python Debugger EngineProvider
 */
public class PythonDebuggerEngineProvider
        extends DebuggerEngineProvider {

  private DebuggerEngine.Destructor _destructor;

  public PythonDebuggerEngineProvider() {
    System.out.println("entering PythonDebuggerEngineProvider");
  }

  @Override
  public String[] getLanguages() {
    return new String[]{"python", "jython"};
  }

  @Override
  public String getEngineTypeID() {
    // System.out.println("returning back PythonDebuggerEngine") ;
    return "PythonDebuggerEngine";
  }

  @Override
  public Object[] getServices() {
    return new Object[]{};
  }

  @Override
  public void setDestructor(DebuggerEngine.Destructor destructor) {
    _destructor = destructor;
  }

  public DebuggerEngine.Destructor getDestructor() {
    return _destructor;
  }
}
