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


package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

/**
 * Generation of a bpt directive.
 *
 * Breakpoint control directives flow from a user action to a verifying
 * engine (primary generation) then back to the UI and may get "spread"
 * to other engines (secondary generation). Replies from these engines
 * need not be spread any further.
 *
 * Gen also controls whether overloaded bpts pop up a user dialog (first gen)
 * or silently accept allpossibilities (second gen).
 * 
 * Creation doesn't use Gen to control spreading since it spreads via
 * the RESTORE pathway which is implicitly like a second() generation.
 *
 * For changes the generation is checked in NB.spreadChange() and advanced in
 * NB.changeAllButTo().
 * In the case of change directives the change may need to spread to children
 * of overloaded bpts as well (tertiary generation).
 *
 * For deletion the generation is checked in NB.primDelete() and advanced in 
 * NB.primDelete().
 * When finishing a session bpt deletion directives start out with second
 * generation Gen.
 *
 *
 * The 'origin' property records the NB to which the action was originally
 * applied. It is used for a special case in NB.primDelete().
 *
 *	In contrast, the change pathway, which also needs such information,
 *	relies on the original() property of edited NB's. It may be possible
 *	to forego NB.original() in favor of Gen.origin() but we'll leave
 *	that for anotehr day.
 *
 * 
 * Typical usage is:
 *	gen = Gen.primary()
 * or
 *	gen = Gen.secondary()
 *
 * and once the directive has been validated ...
 *	gen = gen.second()
 *
 * This class is immutable.
 */

public final class Gen {
    private final int gen;
    private final boolean spread;
    private final NativeBreakpoint origin;

    private Gen(int gen, boolean spread, NativeBreakpoint origin) {
	this.gen = gen;
	this.spread = spread;
	this.origin = origin;
    }

    @Override
    public String toString() {
	String sprd = spread? "s": "";		// NOI18N
	return "<"+gen+spread+">";		// NOI18N
    }

    /**
     * Create a primary generation.
     * By default the primary generation always propagates.
     */
    public static Gen primary(NativeBreakpoint origin) {
	return new Gen(1, true, origin);
    }

    /**
     * Create a secondary generation from scratch.
     */
    public static Gen secondary(NativeBreakpoint origin) {
	return new Gen(2, false, origin);
    }

    /**
     * Derive a second generation from a primary gen.
     */
    public final Gen second() {
	assert this.isPrimary() : "can only get a second Gen from first Gen";
	return new Gen(2, false, origin);
    }

    /**
     * Derive a third generation from a primary or secondary gen.
     */
    final Gen third() {
	assert !this.isTertiary() : "cannot get a third gen from third gen";
	return new Gen(3, false, origin);
    }

    public final boolean isPrimary() {
	return gen == 1;
    }

    public final boolean isSecondary() {
	return gen == 2;
    }

    public final boolean isTertiary() {
	return gen == 3;
    }

    public final NativeBreakpoint origin() {
	return origin;
    }
}
