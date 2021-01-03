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

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.python.debugger.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;

public class PythonBreakpoint
        extends Breakpoint {

  /** The style of filtering of hit counts.
   * The breakpoint is reported when the actual hit count is "equal to",
   * "greater than" or "multiple of" the number specified by the hit count filter. */
  public static enum HIT_COUNT_FILTERING_STYLE {

    EQUAL, GREATER, MULTIPLE
  }
  private final static String _UPDATED_ = "updated";
  private String _condition = ""; // NOI18N
  private boolean _enabled = true;
  private Line _line;

  /** Creates a new instance of PythonBreakpoint */
  public PythonBreakpoint(Line line) {
    _line = line;
  }

  /**
   * Test whether the breakpoint is enabled.
   *
   * @return <code>true</code> if so
   */
  @Override
  public boolean isEnabled() {
    return _enabled;

  }

  public String getCondition() {
    return _condition;
  }

  /**
   * Disables the breakpoint.
   */
  @Override
  public void disable() {
    if (!_enabled) {
      return;
    }
    _enabled = false;
    firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
  }

  public Line getLine() {
    return _line;
  }

  protected void fireUpdated() {
    firePropertyChange(_UPDATED_, null, null);
  }


  public void setCondition(String condition) {
    _condition = condition;
  }

  /**
   * Enables the breakpoint.
   */
  @Override
  public void enable() {
    if (_enabled) {
      return;
    }
    _enabled = true;
    firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
  }

  public FileObject getFileObject() {
    return (FileObject) getLine().getLookup().lookup(FileObject.class);
  }

  public String getFilePath() {
    return FileUtil.toFile(getFileObject()).getAbsolutePath();
  }

  public int getLineNumber() {
    // Note that Line.getLineNumber() starts at zero
    return getLine().getLineNumber() + 1;
  }

  public void setLine(String url, int lineNumber) {
    // Note that Line.getLineNumber() starts at zero
    _line = Utils.getLine(url, lineNumber - 1);
  }
}
