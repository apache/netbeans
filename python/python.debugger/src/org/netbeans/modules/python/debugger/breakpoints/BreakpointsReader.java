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

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.python.debugger.Utils;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;

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
