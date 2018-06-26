/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.monitor.data;

public class Constants  {

    public static class Files {

	public final static String log = "log"; //NOI18N
	public final static String client = "client.data"; //NOI18N
	public final static String request = "request.data"; //NOI18N
	public final static String session = "session.data"; //NOI18N
	public final static String cookies = "cookies.data"; //NOI18N
	public final static String servletProps = "environ.data"; //NOI18N
	public final static String all = "all.data"; //NOI18N
	public final static String replay = "saved.obj"; //NOI18N

	public final static String monitor = "monitor"; //NOI18N
	public final static String save = "save"; //NOI18N
	public final static String current = "current"; //NOI18N
	public final static String blank = "blank"; //NOI18N
	public final static String getall = "getall"; //NOI18N
	public final static String delete = "delete"; //NOI18N
    }

    public static class Comm {
	public final static String ACK =    "ACK"; //NOI18N
	public final static String END =    "END"; //NOI18N
	public final static String ERROR =  "ERROR"; //NOI18N
	public final static String ATT =  "bvhE12xSa"; //NOI18N
    }  

    public static class Labels { 

	public final static String id = "id"; //NOI18N
	public final static String uri = "uri"; //NOI18N
	public final static String invoker = "invoker"; //NOI18N
	public final static String resend = "resend_ffj"; //NOI18N
	public final static String current = "current_ffj"; //NOI18N
	public final static String port = "port"; //NOI18N
    }
  
    public static class Context { 
	public final static String monitorDir = "/monitor/"; //NOI18N
	public final static String monitorContextName = "/monitor_ffj"; //NOI18N
    }

    public static class Http { 

	public final static String POST = "POST"; //NOI18N
	public final static String GET = "GET"; //NOI18N
	public final static String PUT = "PUT"; //NOI18N
	public final static String DELETE = "DELETE"; //NOI18N

	public final static String HEAD = "HEAD"; //NOI18N
	public final static String mimeHTML = "text/html"; //NOI18N
	public final static String mimeTEXT = "text/plain"; //NOI18N

	public final static String userAgent = "user-agent"; //NOI18N
	public final static String accept =  "accept"; //NOI18N
	public final static String acceptCharset =  "accept-charset"; //NOI18N
	public final static String acceptEncoding =  "accept-encoding"; //NOI18N
	public final static String acceptLang =  "accept-language"; //NOI18N
	public final static String referer =  "referer"; //NOI18N
    }	

    public static class Html { 

	// Main stuff
	public final static String startHtml =  "<HTML>\n"; //NOI18N
	public final static String endHtml =  "</HTML>\n"; //NOI18N

	public final static String startBodyNetbeans = 
	    "<body text=\"#000000\" bgcolor=\"#CCCCCC\" link=\"#0000EE\" vlink=\"#551A8B\" alink=\"#FF0000\">\n<table with=\"400\">\n<tr><td>\n"; //NOI18N

	public final static String endBodyNetbeans = 
	    "</td></tr></table></body>"; //NOI18N

	// Misc
	public final static String replayURL = "http://{0}:{1}{2}?resend_ffj={3}&current_ffj={4}"; //NOI18N
	public final static String par = "<p></p>\n"; //NOI18N
	public final static String hr = "<hr>\n"; //NOI18N
	public final static String br = "<br>\n"; //NOI18N
	public final static String bold = "<b>{0}</b><br>\n"; //NOI18N

	// Tables
	public final static String startTable2 = 
	    "<table border=\"1\" cellspacing=\"0\" cellpadding=\"1\" cols=\"2\" width=\"400\" bgcolor=\"#C0C0C0\" >\n"; //NOI18N
      

	public final static String startTable4 = 
	    "<table border=\"1\" cellspacing=\"0\" cellpadding=\"1\" cols=\"4\" width=\"400\" bgcolor=\"#C0C0C0\" >\n"; //NOI18N
      
	public final static String endTable = "</table>\n"; //NOI18N
	public final static String itemString = 
	    "<tr><td width=\"170\">{0}</td><td width=\"210\">{1}</td></tr>\n"; //NOI18N
	public final static String itemStringTwo = 
	    "<tr><td  width=\"60\">{0}</td><td width=\"80\">{1}</td><td width=\"60\">{2}</td><td width=\"180\">{3}</td></tr>\n"; //NOI18N

    }

    public static class Punctuation { 

	public final static String newLine = "\n"; //NOI18N
	public final static String tab = "\t"; //NOI18N
	public final static String itemSep =  "|"; //NOI18N
	public final static String slash =  "/"; //NOI18N
    }
  
} // Constants








