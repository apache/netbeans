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

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Location captures the symbolic value of the PC in the form of the tuple
 * <code>
 *	<src, line, func, pc, flags>
 * </code>
 * Not all values are always present.
 * <p>
 * There is usually a "home" Location which represents where the CPU
 * is actually stopped, and a "visited" location which represents PC's
 * on other stack frames, or locations visited when the debugger is 
 * utilised as a browser (E.g. in response to the dbx "func" and "file"
 * commands, or RTC navigation).
 * A home Location is denoted by the green arrow in the src and disassembly
 * views while visited locations are denoted by the "lavender" triangle.
 * <p>
 * This class is _almost_ immutable because existing code for deciding whether
 * a location is visiting needs to set the flag after construction. That
 * SHOULD be improved in the future. That code also why we have an 'equals'
 * which isn't very thorough and should hopefully be discarded at the same
 * time.
 */

public class Location {

    public static final Location EMPTY = 
	new Location(null, 0, null, 0, 0, null);

    public static final int UPDATE	= 1<<0;
    public static final int VISITED	= 1<<1;

    // The following only work for dbx
    public static final int CALLED	= 1<<2;
    public static final int TOPFRAME	= 1<<3;
    public static final int BOTTOMFRAME = 1<<4;
    public static final int SRC_OOD	= 1<<5;

    private final String src;
    private final int line;
    private final String func;
    private final long pc;
    private int flags;
    
    // breakpoint hit if any
    private final NativeBreakpoint breakpoint;

    protected Location(String src, int line, String func, long pc, int flags, NativeBreakpoint breakpoint) {
	this.src = src;
	this.line = line;
	this.func = func;
	this.pc = pc;
	this.flags = flags;
        this.breakpoint = breakpoint;
    }

    /*
     * Immutable-style setter for 'line'.
     */
    public Location line(int line) {
	return new Location(src, line, func, pc, flags, breakpoint);
    }

    public final void setVisited(boolean v) {
	if (v)
	    flags |= VISITED;
	else
	    flags &= ~VISITED;
    }


    public final String src() {
	return src;
    }

    public final int line() {
	return line;
    }

    public final String func() {
	return func;
    }

    public final long pc() {
	return pc;
    }

    public final boolean update() {
	return (flags & UPDATE) == UPDATE;
    }

    public final boolean visited() {
	return (flags & VISITED) == VISITED;
    }

    public final boolean called() {
	return (flags & CALLED) == CALLED;
    }

    public final boolean topframe() {
	return (flags & TOPFRAME) == TOPFRAME;
    }

    public final boolean bottomframe() {
	return (flags & BOTTOMFRAME) == BOTTOMFRAME;
    }

    public final boolean srcOutOfdate() {
	return (flags & SRC_OOD) == SRC_OOD;
    }

    public final boolean hasSource() {
	return src != null && line > 0;
    }

    public NativeBreakpoint getBreakpoint() {
        return breakpoint;
    }

    @Override
    public String toString() {
	String address = Address.toHexString0x(pc, true);
	return "\"" + src + "\":" + line + " " + func + "()" + // NOI18N
		" " + address + // NOI18N
		" " + (update() ? "UPDATE" : "NOUPDATE") + // NOI18N
		" " + (hasSource() ? "SRC" : "NOSRC"); // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location)) {
            return false;
        }

        Location that = (Location) obj;

        if (this.line != that.line) {
            return false;
        }

        if (IpeUtils.sameString(this.src, that.src)) {
            return true;
        }

        return this.pc == that.pc;
    }

    @Override
    /**
     * Implementation of hashCode() 'looks' at fields used in equals only. See
     * comments to the class regarding equals() method and instance
     * immutability...
     */
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.src != null ? this.src.hashCode() : 0);
        hash = 97 * hash + this.line;
        hash = 97 * hash + (int) (this.pc ^ (this.pc >>> 32));
        return hash;
    }
}
