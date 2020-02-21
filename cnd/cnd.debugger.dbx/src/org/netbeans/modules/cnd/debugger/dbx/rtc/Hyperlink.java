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


package org.netbeans.modules.cnd.debugger.dbx.rtc;

/**
   This class represents a hyperlink to be used by ActiveTerm which has
   <ul>
    <li> An associated URL.
    <li> A Resolver which handles the URL activation.
   </ul>
   When the link is activated, the URL is announced to the various
   URL listeners, who may take actions such as "show the URL in
   the editor", "show the URL in the browser", "show the URL in
   the Data Browser", etc.
   <p>
*/

public final class Hyperlink {

    public static interface Resolver {
	/**
	 * Activate this link
	 */
	public void activate(Object source, Hyperlink hyperlink);
    }


    /**
     * The Resolver which handles this hyperlink.
     */
    private final Resolver resolver;

    /**
     * The URL to open when the hyperlink is activated
     */
    private final String url;

    /** Create a new ThreadsWindow tied to the given debugger. */
    public Hyperlink(Resolver resolver, String url) {
	this.resolver = resolver;
	this.url = url;
    }

    /** Get URL of the link */
    public String getUrl() {
	return url;
    }

    /** Activate this link */
    public void activate(Object source) {
	resolver.activate(source, this);
    }
}
