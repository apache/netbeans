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
package org.netbeans.modules.python.debugger.backend;

/**
 * commuenication event between Debugger Front End and Pluggin editors
 *
 */
public class PluginEvent {

  public final static int UNDEFINED = -1;
  public final static int NEWSOURCE = 0;
  public final static int NEWLINE = 1;
  public final static int STARTING = 2;
  public final static int ENDING = 3;
  public final static int ENTERCALL = 4;
  public final static int LEAVECALL = 5;
  public final static int BUSY = 6;
  public final static int NOTBUSY = 7;
  private int _type = UNDEFINED;
  private String _source = null;
  private int _line = UNDEFINED;

  public PluginEvent(int type, String source, int line) {
    _type = type;
    _source = source;
    _line = line;
  }

  public int get_type() {
    return _type;
  }

  public String get_source() {
    return _source;
  }

  public int get_line() {
    return _line;
  }
}
