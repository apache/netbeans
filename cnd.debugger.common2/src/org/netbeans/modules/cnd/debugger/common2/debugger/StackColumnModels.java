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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.modules.cnd.debugger.common2.values.StringEditor;

/**
 * Convenience container for individual ColumnModels specified as inner classes.
 *
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/CallStackView/
 *	org.netbeans.spi.viewmodel.ColumnModel
 *	NOTE: Use '...debugger.StackColumnModels$Location'
 */

public final class StackColumnModels {

    public static final class Location extends AbstractColumnModel {
	public Location() {
	     super(Constants.PROP_FRAME_LOCATION,
		   Catalog.get("PROP_frame_location"), // NOI18N
		   Catalog.get("HINT_frame_location"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class Number extends AbstractColumnModel {

	public Number() {
	     super(Constants.PROP_FRAME_NUMBER,
		   Catalog.get("PROP_frame_number"), // NOI18N
		  Catalog.get("HINT_frame_number"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class Optimized extends AbstractColumnModel {

	public Optimized() {
	     super(Constants.PROP_FRAME_OPTIMIZED,
		   Catalog.get("PROP_frame_optimized"), // NOI18N
		  Catalog.get("HINT_frame_optimized"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class CurrentPC extends AbstractColumnModel {

	public CurrentPC() {
	     super(Constants.PROP_FRAME_CURRENT_PC,
		   Catalog.get("PROP_frame_current_pc"), // NOI18N
		  Catalog.get("HINT_frame_current_pc"), String.class, false, new StringEditor()); // NOI18N
	}
    }

    public static final class LoadObj extends AbstractColumnModel {

	public LoadObj() {
	     super(Constants.PROP_FRAME_LOADOBJ,
		   Catalog.get("PROP_frame_loadobj"), // NOI18N
		  Catalog.get("HINT_frame_loadobj"), String.class, false, new StringEditor()); // NOI18N
	}
    }
}
