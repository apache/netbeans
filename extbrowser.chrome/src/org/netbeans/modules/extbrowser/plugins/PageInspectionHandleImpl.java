/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.extbrowser.plugins;

import org.json.simple.JSONObject;
import org.netbeans.modules.extbrowser.chrome.ChromeBrowserImpl;
import org.netbeans.modules.web.browser.spi.PageInspectionHandle;

/**
 * {@code PageInspectionHandle} for external browser.
 *
 * @author Jan Stola
 */
public class PageInspectionHandleImpl implements PageInspectionHandle {
    /** Name of the attribute holding the message type. */
    private static final String MESSAGE_TYPE = "message"; // NOI18N
    /** Message type used by the page inspection handle. */
    private static final String PAGE_INSPECTION_PROPERTY_CHANGE = "pageInspectionPropertyChange"; // NOI18N
    /** Name of the attribute holding the name of the modified property. */
    private static final String PROPERTY_NAME = "propertyName"; // NOI18N
    /** Name of the attribute holding the value of the modified property. */
    private static final String PROPERTY_VALUE = "propertyValue"; // NOI18N
    /** Name of the selection mode property. */
    private static final String SELECTION_MODE = "selectionMode"; // NOI18N
    /** Name of the synchronize selection property. */
    private static final String SYNCHRONIZE_SELECTION = "synchronizeSelection"; // NOI18N
    /** Web-browser pane this handle belongs to. */
    private ChromeBrowserImpl browserImpl;

    /**
     * Creates a new {@code PageInspectionHandleImpl}.
     *
     * @param browserImpl web-browser pane this handle belongs to.
     */
    public PageInspectionHandleImpl(ChromeBrowserImpl browserImpl) {
        this.browserImpl = browserImpl;
    }

    @Override
    public void setSelectionMode(boolean selectionMode) {
        JSONObject message = new JSONObject();
        message.put(MESSAGE_TYPE, PAGE_INSPECTION_PROPERTY_CHANGE);
        message.put(PROPERTY_NAME, SELECTION_MODE);
        message.put(PROPERTY_VALUE, selectionMode);
        sendMessage(message.toJSONString());
    }

    @Override
    public void setSynchronizeSelection(boolean synchronizeSelection) {
        JSONObject message = new JSONObject();
        message.put(MESSAGE_TYPE, PAGE_INSPECTION_PROPERTY_CHANGE);
        message.put(PROPERTY_NAME, SYNCHRONIZE_SELECTION);
        message.put(PROPERTY_VALUE, synchronizeSelection);
        sendMessage(message.toJSONString());
    }

    /**
     * Sends a message to the inspected web-browser pane.
     *
     * @param message message to send.
     */
    private void sendMessage(String message) {
        ExternalBrowserPlugin.getInstance().sendMessage(message, browserImpl, ExternalBrowserPlugin.FEATURE_ROS);
    }

}
