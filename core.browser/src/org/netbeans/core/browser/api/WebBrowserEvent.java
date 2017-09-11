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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.browser.api;

import java.awt.AWTEvent;
import org.w3c.dom.Node;

/**
 *
 * @author S. Aubrecht
 */
public abstract class WebBrowserEvent {

    /**
     * Browser is about to load a new URL.
     */
    public static final int WBE_LOADING_STARTING = 1;

    /**
     * Browser started loading a new URL.
     */
    public static final int WBE_LOADING_STARTED = 2;

    /**
     * Browser finished loading
     */
    public static final int WBE_LOADING_ENDED = 3;

    /**
     * Mouse event in browser component.
     */
    public static final int WBE_MOUSE_EVENT = 4;

    /**
     * Key event in browser component.
     */
    public static final int WBE_KEY_EVENT = 5;



    /**
     * @return Event type.
     */
    public abstract int getType();

    /**
     * @return Browser component the event originated from.
     */
    public abstract WebBrowser getWebBrowser();

    /**
     * @return URL associated with the event or null.
     */
    public abstract String getURL();

    /**
     * @return AWT event (MouseEvent or KeyEvent) or null.
     */
    public abstract AWTEvent getAWTEvent();

    /**
     * @return Document associated with the event (WBE_MOUSE_EVENT and WBE_KEY_EVENT only) or null.
     */
    public abstract Node getNode();

    /**
     * Invoke this method to abort loading of URL when event type is WBE_LOADING_STARTING.
     * Has no effect for other event types.
     */
    public abstract void cancel();
}
