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

package org.netbeans.api.debugger.jpda.testapps;

import java.util.Vector;
import java.io.PrintWriter;
import java.io.IOException;

public final class JspLineBreakpointApp {

/**
 * Sample JSP line breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Libor Kotouc
 */
  public static void main(String[] args)
            throws IOException {




    StringBuffer sb = new StringBuffer(1024);


    try {









        
        
      sb.append("\r\n");
      sb.append("\r\n");
      sb.append("\r\n");
      sb.append("<html>\r\n");
      sb.append("    <head>\r\n");
      sb.append("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
      sb.append("        <title>JSP Page</title>\r\n");
      sb.append("    </head>\r\n");
      sb.append("    <body>\r\n");
      sb.append("\r\n");
      sb.append("        <h1>Main Page</h1>\r\n");
      sb.append("        ");
      sb.append("<font color=\"red\">\r\n");
      sb.append("    INCLUDED from d directory\r\n");
      sb.append("</font>");
      sb.append("\r\n");
      sb.append("        <br/>\r\n");
      sb.append("        ");
      sb.append("<font color=\"blue\">\n");
      sb.append("    INCLUDED from &lt;web-root&gt; directory\n");
      sb.append("</font>");
      sb.append("\r\n");
      sb.append("        <br/>\r\n");
      sb.append("        ");
      sb.append("<font color=\"blue\">\n");
      sb.append("    INCLUDED from &lt;web-root&gt; directory\n");
      sb.append("</font>");
      sb.append("\r\n");
      sb.append("    \r\n");
      sb.append("    </body>\r\n");
      sb.append("</html>\r\n");
    } catch (Throwable t) {

        
        
        
        
        
        
        
    }
//    out.flush();
  }
}
