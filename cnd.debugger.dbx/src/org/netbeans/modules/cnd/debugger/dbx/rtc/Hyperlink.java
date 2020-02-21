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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
