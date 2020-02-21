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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import org.netbeans.modules.terminal.api.ui.IOTopComponent;

/**
 *
 */
public final class TermComponentFactory {
    public static final int ACTIVE = 1 << 0;
    public static final int PACKET_MODE = 1 << 1;
    public static final int RAW_PTY = 1 << 2;
    public static final int PTY = 1 << 3;

    /* package */ static TermComponent createNewTermComponent(IOTopComponent owner, int flags) {
	return new NewTermComponent(owner, flags);
    }

    public static boolean isPty(int flags) {
	return (flags & TermComponentFactory.PTY) == TermComponentFactory.PTY;
    }

    public static boolean isRaw(int flags) {
	return (flags & TermComponentFactory.RAW_PTY) == TermComponentFactory.RAW_PTY;
    }

    public static boolean isActive(int flags) {
	return (flags & TermComponentFactory.ACTIVE) == TermComponentFactory.ACTIVE;
    }

    public static boolean isPacketMode(int flags) {
	return (flags & TermComponentFactory.PACKET_MODE) == TermComponentFactory.PACKET_MODE;
    }

    public static void ckFlags(int flags) {
	assert isPty(flags) || !isRaw(flags);		// raw only if pty
	assert isPty(flags) || ! isPacketMode(flags);	// packet only if pty
	assert ! isPty(flags) || ! isActive(flags);	// active only if !pty
    }
}
