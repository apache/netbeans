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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

import java.util.EventObject;

/**
 * Defines an event that encapsulates changes to a Picklist.
 */
public class RecordEvent extends EventObject {
    /** Identifies one or more changes in the lists contents. */
    public static final int CONTENTS_MODIFIED = 0;
    public static final int CONTENTS_UNMODIFIED = 1;
    public static final int CONTENTS_VALID = 2;
    public static final int CONTENTS_INVALID = 3;
    public static final int CONTENTS_ERROR = 4;
    public static final int CONTENTS_NOERROR = 5;
    public static final int CONTENTS_OPTIONS = 6;

    /** * The type of this event; */
    private int type;
    private String text;

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying one of <code>CONTENTS_*</code>.
     */
    public RecordEvent(Object source, int type) {
	super(source);
	this.type = type;
	this.text = null;
    }

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying one of <code>CONTENTS_*</code>.
     */
    public RecordEvent(Object source, int type, String text) {
	super(source);
	this.type = type;
	this.text = text;
    }

    /**
     * Returns the event type. The possible values are one of 'CONTENTS_*'.
     *
     * @return an int representing the type value
     */
    public int getType() {
	return type;
    }

    /**
     * Returns information associated with the event.
     *
     * @return an String representing the informational text
     */
    public String getText() {
	return text;
    }
}
