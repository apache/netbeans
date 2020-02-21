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
