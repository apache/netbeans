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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

/**
 * Signal lists
 *
 * was: ... inlined in RunConfig
 */

public final class Signals extends ProfileCategory {

    public static final class InitialSignalInfo {
	private final int signo;
	private final String name;
	private final String description;
	private final boolean caughtByDefault;

	private boolean caught;

	public InitialSignalInfo(int signo, String name, String description,
		                 boolean caughtByDefault, boolean caught) {
	    this.signo = signo;
	    this.name = name;
	    this.description = description;
	    this.caughtByDefault = caughtByDefault;

	    this.caught = caught;
	}

	public int signo() {
	    return signo;
	}

	public String name() {
	    return name;
	}

	public String description() {
	    return description;
	}

	public boolean isCaughtByDefault() {
	    return caughtByDefault;
	}

	public boolean isCaught() {
	    return caught;
	}

	public void setCaught(boolean caught) {
	    this.caught = caught;
	}

	/**
	 * Reset the valkue of 'caught' to it's default value.
	 */
	public void resetCaught() {
	    caught = caughtByDefault;
	}

    };

    public static final class SignalInfo {
	private final int signo;
	private final boolean caught;

	public SignalInfo(int signo, boolean caught) {
	    this.signo = signo;
	    this.caught = caught;
	}

	public int signo() {
	    return signo;
	}
	public boolean caught() {
	    return caught;
	}
    };

    // These arrays are 0 origin.
    // That is SIGHUP (1) is at entry 0.

    private InitialSignalInfo[] signals = new InitialSignalInfo[0];
    private InitialSignalInfo[] xml_signals;

    public Signals(DbgProfile owner) {
	super(owner, DbgProfile.PROP_SIGNALS);
    } 

    private boolean clone = false;

    boolean isClone() {
	return clone;
    }

    void setClone() {
	clone = true;
    }

    /*
     * This needs to work because firePropertyChange depends on it.
     */

    // Override Object
    @Override
    public boolean equals(Object thatObject) {
	// canonical part
	if (this == thatObject)
	    return true;
	if (! (thatObject instanceof Signals))
	    return false;
	Signals that = (Signals) thatObject;

	if (this.count() != that.count())
	    return false;

	for (int sx = 0; sx < count(); sx++) {
	    if (this.signals[sx].caught != that.signals[sx].caught)
		return false;
	}

	return true;
    }

    @Override
    public Object clone() {
	Signals clone = new Signals(null);
	clone.setClone();

	clone.signals = new InitialSignalInfo[signals.length];
	for (int sx = 0; sx < signals.length; sx++) {
	    final InitialSignalInfo oi = signals[sx];
	    if (oi == null)
		return null;
	    InitialSignalInfo ni = new InitialSignalInfo(oi.signo(),
		                                         (oi.name() != null) ? oi.name() : " ", // NOI18N
							 (oi.description() != null) ? oi.description() : " ", // NOI18N
							 oi.isCaughtByDefault(),
							 oi.isCaught());

	    clone.signals[sx] = ni;
	}

	return clone;
    }

    /**
     * Assign values of thatObject to this.
     */

    @Override
    public void assign(Object thatObject) {

	// This happens in the case of cancellation
	if (this == thatObject)
	    return;

	if (! (thatObject instanceof Signals))
	    return;
	Signals that = (Signals) thatObject;

	// Pathmap and Exceptions don't check this
	// OLD assert that.isClone() : "Signals.assign() not assigned from clone";

	Signals old = (Signals) this.clone();

	this.signals = new InitialSignalInfo[that.signals.length];
	for (int sx = 0; sx < that.signals.length; sx++) {
	    final InitialSignalInfo oi = that.signals[sx];
	    if (oi == null)
		return;
	    InitialSignalInfo ni = new InitialSignalInfo(oi.signo(),
		                                         (oi.name() != null) ? oi.name() : " ", // NOI18N
							 (oi.description() != null) ? oi.description() : " ", // NOI18N
							 oi.isCaughtByDefault(),
							 oi.isCaught());
	    this.signals[sx] = ni;

	}
	checkSignal();

	delta(old, this);
    }

    public void restoreDefaultValue() {
	Signals old = (Signals) this.clone();
	for (InitialSignalInfo signal : signals)
	    signal.resetCaught();
	delta(old, this);
    }

    /**
     * Utility for tracking bugs like XXX and YYY.
     */
    public void checkSignal() {
	/* DEBUG
	int sx = 1;
	if (signals != null) {
	    System.out.printf("signal %d set to %s at:\n", sx, signals[sx].caught);
	    Thread.dumpStack();
	}
	*/
    }


    /**
     * Remember signal dispositions as recorded in configuration XML.
     *
     * Called when a project is opened.
     * Then when setDefaultSignals is called we transfer information to the 
     * real 'signals'.
     */

    public void setXMLSignal(InitialSignalInfo[] s) {
	xml_signals = s;
    }


    /**
     * Set the default disposition of signals as told us by the engine.
     * Only set it the first time round.
     * Then we apply changes recorded in XML.
     * was: setSignals()
     */

    public void setDefaultSignals(InitialSignalInfo[] s) {

	if (count() == 0) {

	    signals = s;

	    // Now commit to previously recorded information we got
	    // from stored XML.
	    //
	    // Why not do this as soon as setXMLSignal() is called?
	    // setXMLSignal() is called when a project is opened. At that time
	    // we don't know what the list of signals is. We need to at least
	    // run dbx once and get a signal_list() message from it before
	    // signals.length is > 0. That happens when we are called.
	    //
	    // This has some weird user-level results:
	    // (1) Do one IDE/debug session and modify some of the signal
	    // dispositions.
	    // (2) Re-start IDE, go to a projects properties and bring up
	    // signals.
	    // It will say "No signals until Debugger runs".
	    // Then run the debugger, but don't catch or ignore any new signals!
	    // Finish the session.
	    // Now if you bring up the signals editor it will show some stuff,
	    // specifically your dispositins modified at step 1.
	    // That's because adjustSignals() ran when you first started dbx.
	    //
	    // One remedy for this is to start a dbx behind users backs
	    // just when the IDE starts _just_ to get all this information.

	    if (xml_signals != null) {
                for (InitialSignalInfo xml_signal : xml_signals) {
                    for (InitialSignalInfo signal : signals) {
                        if (signal.name.equals(xml_signal.name)) {
                            signal.setCaught(xml_signal.isCaught());
                        }
                    }
		}
	    }

	    checkSignal();
	}
    }

    /**
     * Called on individual catch/ignore notifications from dbx
     * including those in .dbxrc.
     */

    public void setSignalState(SignalInfo s) {
	// -1 is for adjusting to 0-origin
	signals[s.signo-1].setCaught(s.caught());
	checkSignal();
    }


    public InitialSignalInfo getSignal(int sx) {
	if (count() > 0)
	    return signals[sx];
	else
	    return null;
    }
    
    public InitialSignalInfo getSignalByName(String name) {
	if (count() > 0) {
            for (InitialSignalInfo signal : signals) {
                if (signal.name().equals(name)) {
                    return signal;
                }
            }
        }
        
        return null;
    }

    public int count() {
	if (signals == null)
	    return 0;
	else
	    return signals.length;
    }

    public boolean isDefaultValue(int sx) {
	InitialSignalInfo s = signals[sx];
	if (s == null)
	    return true;
	if (s.caught) {
	    return s.caughtByDefault;
	} else {
	    return !s.caughtByDefault;
	}
    }

    public boolean isDefaultValue() {
	// If at least one signal deviates we're not default
	for (int i = 0; i < signals.length; i++)
	    if (! isDefaultValue(i))
		return false;
	return true;
    }

    @Override
    public String toString() {
	if (count() == 0)
	    return Catalog.get("NoSigsUntilDebug"); // NOI18N

	boolean addSep = false;
	String ret = " "; // NOI18N
	if (signals != null) {
	    for (int i = 0; i < signals.length; i++) {
		if (signals[i] != null) {
		    if (addSep)
			ret += ","; // NOI18N

			ret += signals[i].name;
		    addSep = true;
		}
	    }
	}
	return ret;
    }
}
