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
package org.netbeans.modules.python.debugger.gui;

import java.util.EventObject;
import javax.swing.*;

/**
 *  Python debugging event
 *
 */
public class DebugEvent
        extends EventObject {

  public final static String STOP = "shutdown Python environment";
  public final static String START = "startup local debugging session";
  public final static String REMOTESTART = "startup remote debugging session";
  public final static String STEPOVER = "Debug statement step Over";
  public final static String STEPINTO = "Debug statement step Into";
  public final static String RUN = "Run";
  public final static String RUNTOCURSOR = "Run to cursor";
  public final static String SENDCOMMAND = "execute Python Command";
  public final static String COMMANDFIELD = "Python command field";
  public final static String TOGGLEJYTHON = "Jython / CPython language switch";
  public final static String PGMARGS = "Add python programs arguments to args table";
  private String _moduleName;
  private Action _action;
  private AbstractButton _guiButton;

  public DebugEvent(Object source,
          String moduleName,
          Action action,
          AbstractButton gui) {
    super(source);
    _action = action;
    _guiButton = gui;
  }

  public String get_moduleName() {
    return _moduleName;
  }

  public Action get_action() {
    return _action;
  }

  public AbstractButton get_guiButton() {
    return _guiButton;
  }
}
