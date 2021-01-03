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
package org.netbeans.modules.python.debugger.breakpoints;

import javax.swing.JComponent;

import org.netbeans.spi.debugger.ui.BreakpointType;

import org.openide.util.NbBundle;

/**
 * Implementation of breakpoint for Python.
 *
 */
public class PythonLineBreakpointType extends BreakpointType {

  @Override
  public String getCategoryDisplayName() {
    return NbBundle.getMessage(
            PythonLineBreakpointType.class,
            "CTL_Python_breakpoint_events_cathegory_name");
  }

  @Override
  public JComponent getCustomizer() {
    return new PythonBreakpointPanel();
  }

  @Override
  public String getTypeDisplayName() {
    return NbBundle.getMessage(PythonLineBreakpointType.class,
            "CTL_Python_line_event_type_name");
  }

  @Override
  public boolean isDefault() {
    return true;
  }
}
