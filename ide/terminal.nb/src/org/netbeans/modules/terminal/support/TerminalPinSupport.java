/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.terminal.support;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.terminal.nb.TerminalPinnedTabOptions;

/**
 *
 * @author ilia
 */
public class TerminalPinSupport {

    private final AtomicLong maxId = new AtomicLong(0);
    private static final TerminalPinSupport INSTANCE = new TerminalPinSupport();

    private final TerminalPinnedTabOptions pinOptions = TerminalPinnedTabOptions.getDefault();

    public static TerminalPinSupport getDefault() {
	return INSTANCE;
    }

    private final Map<TerminalCreationDetails, TerminalPinningDetails> cache;
    private final List<DetailsStateListener> listeners;

    private TerminalPinSupport() {
	cache = new ConcurrentHashMap<TerminalCreationDetails, TerminalPinningDetails>();
	listeners = new CopyOnWriteArrayList<DetailsStateListener>();
    }

    public List<TerminalDetails> readStoredDetails() {
	List<TerminalDetails> storedDetails = pinOptions.readStoredDetails();
	for (TerminalDetails details : storedDetails) {
	    setIfGreater(maxId, details.getCreationDetails().getId());
	    cache.put(details.getCreationDetails(), details.getPinningDetails());
	}
	return storedDetails;
    }

    public void clear() {
	cache.clear();
	pinOptions.clear();
    }

    public long createPinDetails(TerminalCreationDetails creationDetails) {
	if (creationDetails.getId() != 0) {
	    /* We have this entry in cache, however there is no term associated with it.*/
	    TerminalCreationDetails oldCreationDetails = findCreationDetails(creationDetails.getId());
	    TerminalPinningDetails pinningDetails = cache.get(oldCreationDetails);
	    cache.remove(oldCreationDetails);
	    cache.put(creationDetails, pinningDetails);

	    detailsAdded(creationDetails.getTerm());
	    
	    return creationDetails.getId();
	} else {
	    long id = maxId.incrementAndGet();

	    cache.put(
		    TerminalCreationDetails.create(
			    creationDetails.getTerm(),
			    id,
			    creationDetails.getExecEnv(),
			    creationDetails.isPwdFlag()
		    ), TerminalPinningDetails.DUMMY
	    );
	    
	    return id;
	}
    }

    public void addDetailsStateListener(DetailsStateListener listener) {
	listeners.add(listener);
    }

    public void removeDetailsStateListener(DetailsStateListener listener) {
	listeners.remove(listener);
    }

    private void detailsAdded(Term term) {
	for (DetailsStateListener listener : listeners) {
	    listener.detailsAdded(term);
	}
    }

    public void tabWasPinned(Term term, TerminalPinningDetails pinningDetails) {
	TerminalCreationDetails creationDetails = findCreationDetails(term);
	if (cache.containsKey(creationDetails)) {
	    cache.put(creationDetails, pinningDetails);
	    pinOptions.persist(new TerminalDetails(creationDetails, pinningDetails));
	}
    }

    public void tabWasUnpinned(Term term) {
	pinOptions.forget(findCreationDetails(term).getId());
    }

    public void close(Term term) {
	cache.remove(findCreationDetails(term));
    }

    private boolean setIfGreater(AtomicLong al, long setTo) {
	while (true) {
	    long current = al.get();
	    if (setTo > current) {
		if (al.compareAndSet(current, setTo)) {
		    return true;
		}
	    } else {
		return false;
	    }
	}
    }

    public static interface DetailsStateListener extends EventListener{

	void detailsAdded(Term term);
    }

    public TerminalPinningDetails findPinningDetails(Term term) {
	for (TerminalCreationDetails creationDetails : cache.keySet()) {
	    final Term key = creationDetails.getTerm();
	    if (key != null && key.equals(term)) {
		TerminalPinningDetails get = cache.get(creationDetails);
		if (get == TerminalPinningDetails.DUMMY) {
		    return null;
		}
		return get;
	    }
	}
	return null;
    }

    public TerminalCreationDetails findCreationDetails(Term term) {
	for (TerminalCreationDetails creationDetails : cache.keySet()) {
	    final Term key = creationDetails.getTerm();
	    if (key != null && key.equals(term)) {
		return creationDetails;
	    }
	}
	return null;
    }

    public TerminalCreationDetails findCreationDetails(long id) {
	for (TerminalCreationDetails creationDetails : cache.keySet()) {
	    final long key = creationDetails.getId();
	    if (key == id) {
		return creationDetails;
	    }
	}
	return null;
    }

    // Descriptor
    public static final class TerminalPinningDetails {

	// ConcurrentHashMap don't allow nulls, here is dummy
	static final TerminalPinningDetails DUMMY = new TerminalPinningDetails(false, null, null, false);

	private final boolean customTitle;
	private final String title;
	private final String cwd;
	private boolean pinned;

	public static TerminalPinningDetails create(boolean customTitle, String title, String cwd, boolean pinned) {
	    return new TerminalPinningDetails(customTitle, title, cwd, pinned);
	}

	private TerminalPinningDetails(boolean customTitle, String title, String cwd, boolean pinned) {
	    this.customTitle = customTitle;
	    this.title = title;
	    this.cwd = cwd;
	    this.pinned = pinned;
	}

	public boolean isCustomTitle() {
	    return customTitle;
	}

	public String getTitle() {
	    return title;
	}

	public String getCwd() {
	    return cwd;
	}

	public boolean isPinned() {
	    return pinned;
	}

	public void setPinned(boolean pinned) {
	    this.pinned = pinned;
	}
    }

    public static final class TerminalCreationDetails {

	private final long id;
	private final Term term;
	private final String execEnv;
	private final boolean pwdFlag;

	public static TerminalCreationDetails create(Term term, String execEnv, boolean pwdFlag) {
	    return new TerminalCreationDetails(term, 0, execEnv, pwdFlag);
	}

	public static TerminalCreationDetails create(Term term, long id, String execEnv, boolean pwdFlag) {
	    return new TerminalCreationDetails(term, id, execEnv, pwdFlag);
	}

	public static TerminalCreationDetails create(long id, String execEnv, boolean pwdFlag) {
	    return new TerminalCreationDetails(null, id, execEnv, pwdFlag);
	}

	private TerminalCreationDetails(Term term, long id, String execEnv, boolean pwdFlag) {
	    this.id = id;
	    this.term = term;
	    this.execEnv = execEnv;
	    this.pwdFlag = pwdFlag;
	}

	public long getId() {
	    return id;
	}

	public Term getTerm() {
	    return term;
	}

	public String getExecEnv() {
	    return execEnv;
	}

	public boolean isPwdFlag() {
	    return pwdFlag;
	}
    }

    public static final class TerminalDetails {

	private final TerminalCreationDetails creationDetails;
	private final TerminalPinningDetails pinningDetails;

	public TerminalDetails(TerminalCreationDetails creationDetails, TerminalPinningDetails pinningDetails) {
	    this.creationDetails = creationDetails;
	    this.pinningDetails = pinningDetails;
	}

	public TerminalCreationDetails getCreationDetails() {
	    return creationDetails;
	}

	public TerminalPinningDetails getPinningDetails() {
	    return pinningDetails;
	}
    }
}
