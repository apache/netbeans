/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.debugger.breakpoints;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.python.debugger.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;

/**
 *
 * @author jean-yves Mengant
 */
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
