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








