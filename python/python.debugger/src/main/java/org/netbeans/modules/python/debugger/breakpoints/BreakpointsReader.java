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

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.python.debugger.Utils;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jean-yves Mengant
 */
public class BreakpointsReader
        implements Properties.Reader {

  private final static String _EQUAL_ = "EQUAL";
  private final static String _URL_ = "url";
  private final static String _LINENUMBER_ = "lineNumber";
  private final static String _CONDITION_ = "condition";
  private final static String _COUNTFILTER_ = "countFilter";
  private final static String _FILTERINGSTYLE_ = "filteringStyle";

  /** Creates a new instance of BreakpointsReader */
  public BreakpointsReader() {
  }

  @Override
  public String[] getSupportedClassNames() {
    return new String[]{
              PythonBreakpoint.class.getName(),};
  }

  @Override
  public void write(Object object, Properties properties) {
    PythonBreakpoint b = (PythonBreakpoint) object;
    if ((b != null) &&
            (b.getLine() != null)) {
      FileObject fo = (FileObject) b.getLine().getLookup().lookup(FileObject.class);
      try {
        properties.setString(_URL_, fo.getURL().toString());
        properties.setInt(
                _LINENUMBER_,
                b.getLine().getLineNumber());
        // set complementary optional breakpoint stuff
        properties.setString(_CONDITION_, b.getCondition());
        properties.setInt(_COUNTFILTER_, b.getHitCountFilter());
        if (b.getHitCountFilteringStyle() != null) {
          properties.setString(_FILTERINGSTYLE_, b.getHitCountFilteringStyle().toString());
        }
      } catch (FileStateInvalidException ex) {
        ex.printStackTrace();
      }
    }
  }

  @Override
  public Object read(String typeID, Properties properties) {
    if (!(typeID.equals(PythonBreakpoint.class.getName()))) {
      return null;
    }
    PythonBreakpoint b = new PythonBreakpoint(Utils.getLine(
            properties.getString(_URL_, null),
            properties.getInt(_LINENUMBER_, 1)));
    // get optionals hitcount and conditions
    b.setCondition(properties.getString(_CONDITION_, null));
    HIT_COUNT_FILTERING_STYLE style = HIT_COUNT_FILTERING_STYLE.valueOf(properties.getString(_FILTERINGSTYLE_, _EQUAL_));
    int countFilter = properties.getInt(_COUNTFILTER_, -1);
    b.setHitCountFilter(countFilter, style);

    return b;
  }
}
