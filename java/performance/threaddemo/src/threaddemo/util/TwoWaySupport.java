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

package threaddemo.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import threaddemo.locking.RWLock;

// XXX possibly need some method which is like getStaleValueNonBlocking but which
// throws InvocationTargetException, to permit views to display an error marker
// on stale data
// XXX might want an optional ability to take a snapshot of data in a read lock
// and then parse it outside the lock to avoid shutting out writers for too long

/**
 * Support for bidirectional construction of a derived model from an underlying model.
 * Based on a lock which is assumed to control both models.
 * Handles all locking and scheduling associated with such a system.
 * It is possible to "nest" supports so that the derived model of one is the
 * underlying model of another - but they must still share a common lock.
 *
 * <p>"Derive" means to take the underlying model (not represented explicitly here,
 * but assumed to be "owned" by the subclass) and produce the derived model;
 * typically this will involve parsing or the like. This operates in a read lock.
 *
 * <p>"Recreate" means to take a new derived model (which may in fact be the same
 * as the old derived model but with different structure) and somehow change the
 * underlying model on that basis.
 *
 * <p>"Initiate" means to start derivation asynchronously, not waiting for the
 * result to be complete; this operation is idempotent, i.e. you can call it
 * whenever you think you might like the value later, but it will not cause
 * gratuitous extra derivations.
 *
 * <p>"Invalidate" means to signal that the underlying model has somehow changed
 * and that if there is any derived model it should be considered stale.
 * Invalidating when there is not yet any derived model is a no-op.
 *
 * <p>There are four different kinds of "values" which are employed by this class
 * and which you should be careful to differentiate:
 *
 * <ol>
 *
 * <li><p>The state of the underlying model. This is <em>not</em> explicitly modeled
 * by this class. Subclasses are expected to use that state as needed in
 * {@link #doDerive} and {@link #doRecreate}.
 *
 * <li><p>The state ("value") of the derived model. This is never null and is the
 * return value of {@link #doRecreate}, {@link #getValueBlocking},
 * {@link #getValueNonBlocking}, and {@link #getStaleValueNonBlocking} (except
 * where those methods are documented to return null), as well as the first
 * parameter to {@link #doRecreate}, {@link #doDerive}, and
 * {@link TwoWaySupport.DerivationResult#TwoWaySupport.DerivationResult}
 * and the parameter to {@link #createReference}.
 *
 * <li><p>Deltas in the underlying model. These may in fact be entire new copies
 * of an underlying model, or some diff-like structure, or an {@link java.util.EventObject},
 * etc. - whatever seems most convenient. These are never null and are the argument
 * type of {@link #invalidate} and the second argument type of {@link #doDerive}
 * as well as both argument types and the return value of {@link #composeUnderlyingDeltas}.
 *
 * <li><p>Deltas in the derived model. Again these may be of the same form as the
 * derived model itself - just replacing the model wholesale - or they may be some
 * kind of diff or event structure. These are again never null and are the argument
 * for {@link #mutate} and the second argument for {@link #doRecreate} and
 * {@link TwoWaySupport.DerivationResult#TwoWaySupport.DerivationResult}.
 *
 * </ol>
 *
 * <p>Setting a new derived value explicitly always sets it immediately.
 * When getting the derived value, you have several choices. You can ask for the
 * exact value, if necessary waiting for it to be derived for the first time, or
 * rederived if it is stale. Or you can ask for the value if it is fresh or accept
 * null if it is missing or stale. Or you can ask for the value if it is fresh or
 * stale and accept null if it is missing. The latter two operations do not block
 * (except to get the read lock) and so are valuable in views.
 *
 * <p>Derivation is started immediately after an initiate operation if there is
 * no derived model yet. If there is a model but it is stale and you ask to
 * initiate derivation, by default this also starts immediately, but you may
 * instead give a delay before the new derivation starts (assuming no one asks
 * for the exact derived value before then); this is useful for cases where
 * derivation is time-consuming (e.g. a complex parse) and for performance
 * reasons you wish to avoid triggering it too frivolously. For example, you may
 * be invalidating the derived model after every keystroke which changes a text
 * document, but would prefer to wait a few seconds before showing new results.
 *
 * <p>In case a recreate operation is attempted during a delay in which the model
 * is stale, or simply while a derivation is in progress with or without a preceding
 * delay, there is a conflict: the recreated model is probably a modification of
 * the old stale underlying model, and it is likely that setting it as the new derived
 * model and recreating the underlying model would clobber intermediate changes in the
 * underlying model, causing data loss. By default this support will signal an exception
 * if this is attempted, though subclasses may choose to suppress that and forcibly
 * set the new derived model and recreate the underlying model. Subclasses are better advised
 * to use the exception, and ensure that views of the derived model either handle
 * it gracefully (e.g. offering the user an opportunity to retry the modification
 * on the new derived model when it is available, or just beeping), or put the
 * derived view into a read-only mode temporarily while there is a stale underlying
 * model so that such a situation cannot arise.
 *
 * <p>There is a kind of "external clobbering" that can occur if the view does not
 * update itself promptly after a recreation (generally, after a change in the
 * derived model leading to a fresh value) but only with some kind of delay. In
 * that case an attempted change to the derived model may be working with obsolete
 * data. The support does <em>not</em> try to handle this case; the view is
 * responsible for detecting it and reacting appropriately.
 *
 * <p>Another kind of "clobbering" can occur in case the underlying model is not
 * completely controlled by the lock. For example, it might be the native filesystem,
 * which can change at any time without acquiring a lock in the JVM. In that case
 * an attempted mutation may be operating against a model derived from an older
 * state of the underlying model. Again, this support does <em>not</em> provide a
 * solution for this problem. Subclasses should attempt to detect such a condition
 * and recover from it gracefully, e.g. by throwing an exception from
 * <code>doRecreate</code> or by merging changes. Using TwoWaySupport may not be
 * appropriate for such cases anyway, since derivation could then cause an existing
 * reader to see state changes within its read lock, which could violate its
 * assumptions about the underlying model.
 *
 * <p>Derivation and recreation may throw checked exceptions. In such cases the
 * underlying and derived models should be left in a consistent state if at all
 * possible. If derivation throws an exception, the derived model will be considered
 * stale, but no attempt to rederive the model will be made unless the underlying
 * model is invalidated; subsequent calls to {@link #getValueBlocking} with the
 * same underlying model will result in the same exception being thrown repeatedly.
 * Views should generally put themselves into a read-only mode in this case.
 * If recreation throws an exception, this is propagated to {@link #mutate} but
 * otherwise nothing is changed.
 *
 * <p>You may not call any methods of this class from within the dynamic scope of
 * {@link #doDerive} or {@link #doRecreate} or a listener callback.
 *
 * <p>You can attach a listener to this class. You will get an event when the
 * status of the support changes. All events are fired as soon as possible in the
 * read lock.
 *
 * @author Jesse Glick
 */
public abstract class TwoWaySupport<DM, UMD, DMD> {
    
    /** logging support */
    private static final Logger logger = Logger.getLogger(TwoWaySupport.class.getName());
    
    /** lock used for all static vars */
    private static final Object LOCK = new String("TwoWaySupport");
    
    /** supports which are scheduled to be derived but haven't been yet */
    private static final SortedSet<DeriveTask> toDerive = new TreeSet<DeriveTask>();
    
    /** derivation tasks indexed by support */
    private static final Map<TwoWaySupport,DeriveTask> tasks = new WeakHashMap<TwoWaySupport,DeriveTask>();
    
    /** derivation thread when it has been started */
    private static boolean startedThread = false;
    
    /** queue of derived model references */
    private static ReferenceQueue queue = null;
    
    /** reverse lookup for model field to support queue collector */
    private static final Map<Reference<Object>,Reference<TwoWaySupport>> referencesToSupports = new WeakHashMap<Reference<Object>,Reference<TwoWaySupport>>();
    
    /** associated lock */
    private final RWLock lock;
    
    /** listener list */
    private final List<TwoWayListener<DM, UMD, DMD>> listeners;
    
    /** current derived model, if any */
    private Reference<DM> model = null;
    
    /** current derivation problem, if any */
    private Exception problem = null; // XXX should perhaps be Reference<Exception>?
    
    /** if model is not null, whether it is fresh or stale */
    private boolean fresh = false;
    
    /** if true, derivation has been initiated */
    private boolean active = false;
    
    /** underlying delta, if one is being processed thru initiate + doDerive */
    private UMD underlyingDelta = null;
    
    /** currently in doRecreate() */
    private boolean mutating = false;
    
    /** currently in doDerive() */
    private boolean deriving = false;
    
    /**
     * Create an uninitialized support.
     * No derivation or recreation is scheduled initially.
     * @param lock the associated lock
     */
    protected TwoWaySupport(RWLock lock) {
        this.lock = lock;
        listeners = new ArrayList<TwoWayListener<DM, UMD, DMD>>();
    }
    
    /**
     * Get the associated lock.
     * @return the lock
     */
    public final RWLock getLock() {
        return lock;
    }
    
    /**
     * Compute the derived model from the underlying model.
     *
     * <p>This method is called with a read lock held on the lock.
     * However for derived models with mutable state you may need to acquire an
     * additional simple lock (monitor) on some part of the model to refresh its
     * state - this is not a true write, but other readers should be locked out
     * until it is finished. For purely functional derived models that are
     * replaced wholesale, this is not necessary.
     *
     * <p>Note that derivations never run in parallel, even though they are in a
     * read lock. In this implementation, all derivations in fact run in a dedicated
     * thread if they are invoked asynchronously using {@link #initiate}, but that
     * may change.
     *
     * <p>{@link TwoWayListener#derived} will be triggered after this method
     * completes. An implementation is responsible for notifying relevant listeners
     * of changes to the derived model, but should not do so from within the scope
     * of this method, as the new value of the derived model will not yet be available;
     * instead listen for {@link TwoWayEvent.Derived}. Both the derived delta and
     * final value are made available to that event for this reason.
     *
     * @param oldValue the old value of the derived model, or null if it had
     *                 never been calculated before
     * @param underlyingDelta a change in the underlying model, or null if no
     *                        particular change was signalled
     * @return the new value of the derived model (might be the same object as
     *         the old value) plus the derived delta
     * @throws Exception (checked only!) if derivation of the model failed
     */
    protected abstract DerivationResult<DM, DMD> doDerive(DM oldValue, UMD underlyingDelta) throws Exception;
    
    /**
     * Result of a derivation. Includes both the final resulting value, and the
     * derived delta. The derived delta is not used by {@link TwoWaySupport} except
     * to pass to {@link TwoWayEvent.Derived}, which may be useful for subclasses
     * firing changes.
     */
    protected static final class DerivationResult<DM, DMD> {
        final DM newValue;
        final DMD derivedDelta;
        /**
         * Create a derivation result wrapper object.
         * @param newValue the new value of the derived model
         * @param derivedDelta some representation of the difference from the old
         *                     model; must be null if the old derived value was null,
         *                     and only then
         */
        public DerivationResult(DM newValue, DMD derivedDelta) {
            if (newValue == null) throw new NullPointerException();
            this.newValue = newValue;
            this.derivedDelta = derivedDelta;
        }
    }
    
    /**
     * Compute the effect of two sequential changes to the underlying model.
     * 
     * <p>This method is called with a read lock held on the lock.
     *
     * <p>After this method is called, the first argument is discarded by this support,
     * so a subclass may implement it by mutating the first argument and returning it.
     *
     * @param underlyingDelta1 the older delta
     * @param underlyingDelta2 the newer delta
     * @return a delta representing those two changes applied in sequence
     */
    protected abstract UMD composeUnderlyingDeltas(UMD underlyingDelta1, UMD underlyingDelta2);
    
    /**
     * Recreate the underlying model from the derived model.
     *
     * <p>This method is called with a write lock held on the lock.
     *
     * <p>It is expected that any changes to the underlying model will be notified
     * to the relevant listeners within the dynamic scope of this method. An implementation
     * is responsible for notifying relevant listeners of changes to the derived
     * model, but should not do so from within the scope of this method, as the
     * new value of the derived model will not yet be available; instead listen for
     * {@link TwoWayEvent.Recreated}.
     *
     * @param oldValue the old value of the derived model, or null if it was
     *                 never derived
     * @param derivedDelta a change in the derived model
     * @return the new value of the derived model (might be the same object as
     *         the old value)
     * @throws Exception (checked only!) if recreation of the underlying model failed
     */
    protected abstract DM doRecreate(DM oldValue, DMD derivedDelta) throws Exception;
    
    private void assertStateConsistent() {
        assert Thread.holdsLock(LOCK);
        assert !fresh || model != null;
        assert !fresh || problem == null;
        assert !fresh || !active;
        if (active) {
            assert tasks.containsKey(this);
            // XXX check that toDerive and tasks are consistent
        } else {
            assert !tasks.containsKey(this);
        }
        // XXX what else?
    }
    
    /**
     * Get the value of the derived model, blocking as needed until it is ready.
     * This method requires the read lock and may block further for
     * {@link #doDerive}.
     * @return the value of the derived model (never null)
     * @throws InvocationTargetException if <code>doDerive</code> was called
     *                                   and threw an exception (possibly from an
     *                                   earlier derivation run that is still broken)
     */
    public final DM getValueBlocking() throws InvocationTargetException {
        assert lock.canRead();
        DM old;
        synchronized (LOCK) {
            assertStateConsistent();
            assert !mutating;
            while (deriving) {
                // Another reader is getting the value at the moment, wait for it.
                logger.finer("waiting for another reader to finish deriving");
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {/* OK */}
            }
            if (fresh) {
                DM o = model.get();
                if (o != null) {
                    logger.log(Level.FINER, "fresh value: {0}", o);
                    return o;
                }
            } else if (problem != null) {
                logger.log(Level.FINER, "problem: {0}", problem);
                deactivate();
                throw new InvocationTargetExceptionNoStackTrace(problem);
            }
            // Else we need to block for a value.
            old = (model != null) ? model.get() : null;
            deriving = true;
            fresh = false;
        }
        // Getting the value:
        DerivationResult<DM, DMD> result;
        DM newValue = null;
        try {
            result = doDerive(old, null);
            if (result == null) {
                throw new NullPointerException();
            }
            assert result.newValue != null;
            if (old == null && result.derivedDelta != null) {
                throw new IllegalStateException("Cannot have a non-null derivedDelta for a null oldValue");
            }
            if (old != null && result.derivedDelta == null) {
                throw new IllegalStateException("Cannot have a null derivedDelta for a non-null oldValue");
            }
            logger.log(Level.FINER, "derived value: {0}", result.newValue);
            fresh = true;
            newValue = result.newValue;
        } catch (RuntimeException e) {
            // We don't treat these as model-visible exceptions.
            throw e;
        } catch (Exception e) {
            problem = e;
            fresh = false;
            fireChange(new TwoWayEvent.Broken<DM, UMD, DMD>(this, old, underlyingDelta, e));
            throw new InvocationTargetException(e);
        } finally {
            synchronized (LOCK) {
                deriving = false;
                LOCK.notifyAll();
                if (newValue != null) {
                    setModel(newValue);
                }
                deactivate();
            }
        }
        fireChange(new TwoWayEvent.Derived<DM, UMD, DMD>(this, old, result.newValue, result.derivedDelta, underlyingDelta));
        return result.newValue;
    }
    
    private void deactivate() {
        assert Thread.holdsLock(LOCK);
        if (active) {
            // No longer need to run this.
            active = false;
            DeriveTask t = tasks.remove(this);
            assert t != null;
            toDerive.remove(t);
        }
    }
    
    private static final class InvocationTargetExceptionNoStackTrace extends InvocationTargetException {
        public InvocationTargetExceptionNoStackTrace(Throwable problem) {
            super(problem);
        }
        public Throwable fillInStackTrace() {
            return this;
        }
    }
    
    private void setModel(DM result) {
        assert Thread.holdsLock(LOCK);
        assert result != null;
        if (model != null) {
            referencesToSupports.remove(model);
        }
        model = createEnqueuedReference(result);
        @SuppressWarnings("unchecked")
        Reference<Object> _model = (Reference<Object>) model;
        referencesToSupports.put(_model, new WeakReference<TwoWaySupport>(this));
    }
    
    /**
     * Get the value of the derived model, if it is ready and fresh.
     * This method requires the read lock but otherwise does not block.
     * @return the value of the derived model, or null if it is stale or has never
     *         been computed at all
     */
    public final DM getValueNonBlocking() {
        assert lock.canRead();
        synchronized (LOCK) {
            assertStateConsistent();
            assert !mutating;
            return fresh ? model.get() : null;
        }
    }
    
    /**
     * Get the value of the derived model, if it is ready (fresh or stale).
     * This method requires the read lock but otherwise does not block.
     * @return the value of the derived model, or null if it has never been
     *         computed at all
     */
    public final DM getStaleValueNonBlocking() {
        assert lock.canRead();
        synchronized (LOCK) {
            assertStateConsistent();
            assert !mutating;
            return (model != null) ? model.get() : null;
        }
    }
    
    /**
     * Change the value of the derived model and correspondingly update the
     * underlying model.
     * <p>This method requires the write lock and calls {@link #doRecreate}
     * if it does not throw <code>ClobberException</code>.
     * @param derivedDelta a change to the derived model
     * @return the new value of the derived model
     * @throws ClobberException in case {@link #permitsClobbering} is false and
     *                          the old value of the derived model was stale or
     *                          missing
     * @throws InvocationTargetException if <code>doRecreate</code> throws an
     *                                   exception
     */
    public final DM mutate(DMD derivedDelta) throws ClobberException, InvocationTargetException {
        if (derivedDelta == null) throw new NullPointerException();
        assert lock.canWrite();
        DM oldValue;
        synchronized (LOCK) {
            assertStateConsistent();
            assert !mutating;
            assert !deriving;
            oldValue = (model != null) ? model.get() : null;
            if (!fresh && !permitsClobbering()) {
                throw new ClobberException(this, oldValue, derivedDelta);
            }
            mutating = true;
        }
        DM result = null;
        try {
            // XXX should also dequeue if necessary to avoid sequence:
            // invalidate -> initiate -> [pause] -> mutate -> [pause] -> invalidate -> [pause] -> derive
            // where the final derivation was not really appropriate (or was it?)
            result = doRecreate(oldValue, derivedDelta);
            assert result != null;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            synchronized (LOCK) {
                mutating = false;
                if (result != null) {
                    setModel(result);
                }
            }
        }
        if (fresh) {
            fireChange(new TwoWayEvent.Recreated<DM, UMD, DMD>(this, oldValue, result, derivedDelta));
        } else {
            fireChange(new TwoWayEvent.Clobbered<DM, UMD, DMD>(this, oldValue, result, derivedDelta));
        }
        return result;
    }
    
    /**
     * Indicate that any current value of the derived model is invalid and
     * should no longer be used if exact results are desired.
     * <p>This method requires the read lock but does not block otherwise,
     * except to call {@link #composeUnderlyingDeltas}.
     * @param underlyingDelta a change to the underlying model
     */
    public final void invalidate(UMD underlyingDelta) {
        if (underlyingDelta == null) throw new NullPointerException();
        assert lock.canRead();
        boolean wasInited;
        DM oldValue;
        synchronized (LOCK) {
            assertStateConsistent();
            assert !mutating;
            if (this.underlyingDelta != null) {
                // XXX don't call this with LOCK held
                // may then need to have an 'invalidating' flag (?)
                this.underlyingDelta = composeUnderlyingDeltas(this.underlyingDelta, underlyingDelta);
            } else {
                this.underlyingDelta = underlyingDelta;
            }
            wasInited = fresh || problem != null;
            if (fresh) {
                fresh = false;
            }
            oldValue = (model != null) ? model.get() : null;
            problem = null;
        }
        if (wasInited && oldValue != null) {
            fireChange(new TwoWayEvent.Invalidated<DM, UMD, DMD>(this, oldValue, underlyingDelta));
        }
    }

    /**
     * Initiate creation of the derived model from the underlying model.
     * This is a no-op unless that process has not yet been started or if the
     * value of the derived model is already fresh and needs no rederivation.
     * <p>This method does not require the lock nor does it block, except
     * insofar as {@link #initiating} might.
     */
    public final void initiate() {
        boolean isInitiating = false;
        synchronized (LOCK) {
            assertStateConsistent();
            if (!active && !fresh) {
                DM oldValue = (model != null) ? model.get() : null;
                DeriveTask<DM, UMD, DMD> t = new DeriveTask<DM, UMD, DMD>(this, oldValue != null || problem != null);
                toDerive.add(t);
                tasks.put(this, t);
                active = true;
                startDerivationThread();
                isInitiating = true;
                LOCK.notifyAll();
            }
        }
        if (isInitiating) {
            initiating();
        }
    }
    
    /**
     * Called during {@link #initiate}.
     * The default implementation does nothing. Subclasses may choose to initiate
     * a request for some information from the underlying model, if it is not
     * immediately accessible.
     * <p>This method is not called with any lock, so if a read lock is desired,
     * it must be requested explicitly.
     */
    protected void initiating() {
        // do nothing
    }
    
    /**
     * Add a listener to lifecycle changes in the support.
     * <p>A listener may be added multiple times and must be removed once
     * for each add.
     * <p>This method may be called from any thread and will not block.
     * @param l a listener to add
     */
    public final void addTwoWayListener(TwoWayListener<DM, UMD, DMD> l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    /**
     * Add a listener to lifecycle changes in the support.
     * <p>This method may be called from any thread and will not block.
     * @param l a listener to remove
     */
    public final void removeTwoWayListener(TwoWayListener<DM, UMD, DMD> l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Fire an event to all listeners in the read lock.
     */
    private void fireChange(final TwoWayEvent<DM, UMD, DMD> e) {
        final List<TwoWayListener<DM, UMD, DMD>> ls;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            ls = new ArrayList<TwoWayListener<DM, UMD, DMD>>(listeners);
        }
        lock.read(new Runnable() {
            public void run() {
                for (TwoWayListener<DM, UMD, DMD> l : ls) {
                    if (e instanceof TwoWayEvent.Derived) {
                        l.derived((TwoWayEvent.Derived<DM, UMD, DMD>) e);
                    } else if (e instanceof TwoWayEvent.Invalidated) {
                        l.invalidated((TwoWayEvent.Invalidated<DM, UMD, DMD>) e);
                    } else if (e instanceof TwoWayEvent.Recreated) {
                        l.recreated((TwoWayEvent.Recreated<DM, UMD, DMD>) e);
                    } else if (e instanceof TwoWayEvent.Clobbered) {
                        l.clobbered((TwoWayEvent.Clobbered<DM, UMD, DMD>) e);
                    } else if (e instanceof TwoWayEvent.Forgotten) {
                        l.forgotten((TwoWayEvent.Forgotten<DM, UMD, DMD>) e);
                    } else {
                        assert e instanceof TwoWayEvent.Broken;
                        l.broken((TwoWayEvent.Broken<DM, UMD, DMD>) e);
                    }
                }
            }
        });
    }
    
    /**
     * Supply an optional delay before rederivation of a model after an invalidation.
     * If zero (the default), there is no intentional delay. The delay is irrelevant
     * in the case of {@link #getValueBlocking}.
     * @return a delay in milliseconds (>= 0)
     */
    protected long delay() {
        return 0L;
    }
    
    /**
     * Indicate whether this support permits changes to the derived model via
     * {@link #mutate} to "clobber" underived changes to the underlying model.
     * If false (the default), such attempts will throw {@link ClobberException}.
     * If true, they will be permitted, though a clobber event will be notified
     * rather than a recreate event.
     * <p>A subclass must always return the same value from this method.
     * @return true to permit clobbering, false to forbid it
     */
    protected boolean permitsClobbering() {
        return false;
    }
    
    private Reference<DM> createEnqueuedReference(DM value) {
        Reference<DM> r = createReference(value, queue);
        if (!(r instanceof StrongReference) && queue == null) {
            // Well discard that one; optimistically assumed that
            // createReference is not overridden, in which case we
            // never actually have to make a queue.
            queue = new ReferenceQueue();
            r = createReference(value, queue);
            Thread t = new Thread(new QueuePollingThread(), "TwoWaySupport.QueuePollingThread");
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            t.start();
        }
        return r;
    }
    
    private static final class QueuePollingThread implements Runnable {
        
        public void run() {
            while (true) {
                try {
                    Reference r = queue.remove();
                    TwoWaySupport s;
                    synchronized (LOCK) {
                        Reference<TwoWaySupport> r2 = referencesToSupports.remove(r);
                        s = (r2 != null) ? r2.get() : null;
                    }
                    if (s != null) {
                        notify(s);
                    }
                } catch (InterruptedException e) {
                    assert false : e;
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        private void notify(TwoWaySupport s) {
            s.fireChange(new TwoWayEvent.Forgotten(s));
        }
        
    }
    
    /**
     * Create a reference to the derived model.
     * The support will only retain this reference (though event objects will
     * strongly refer to the derived model when appropriate).
     * If the referent is collected, the support returns to an underived state.
     *
     * <p>This implementation always creates a strong reference that will never
     * be collected so long as the support itself is not collected.
     * @param value a derived model object
     * @param q a reference queue supplied by the support
     * @return a reference to the model enqueued on that reference queue
     */
    protected Reference<DM> createReference(DM value, ReferenceQueue q) {
        // Does not matter what the queue is.
        return new StrongReference<DM>(value);
    }

    /**
     * A strong reference whose referent will not be collected unless the
     * reference is too.
     */
    private static final class StrongReference<DM> extends WeakReference<DM> {
        private DM value;
        public StrongReference(DM value) {
            super(value);
            assert value != null;
            this.value = value;
        }
        public DM get() {
            return value;
        }
        public void clear() {
            super.clear();
            value = null;
        }
    }
    
    static final class DeriveTask<DM, UMD, DMD> implements Comparable<DeriveTask<DM, UMD, DMD>> {
        
        public final Reference<TwoWaySupport<DM, UMD, DMD>> support;
        
        public final long schedule;
        
        public DeriveTask(TwoWaySupport<DM, UMD, DMD> support, boolean delay) {
            this.support = new WeakReference<TwoWaySupport<DM, UMD, DMD>>(support);
            schedule = System.currentTimeMillis() + (delay ? support.delay() : 0L);
        }
        
        public int compareTo(DeriveTask<DM, UMD, DMD> t) {
            if (t == this) return 0;
            if (schedule > t.schedule) return 1;
            if (schedule < t.schedule) return -1;
            return hashCode() - t.hashCode();
        }
        
    }
    
    private static void startDerivationThread() {
        synchronized (LOCK) {
            if (!startedThread) {
                Thread t = new Thread(new DerivationThread(), "TwoWaySupport.DerivationThread");
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true);
                t.start();
                startedThread = true;
            }
        }
    }
    
    private static final class DerivationThread implements Runnable {
        
        public void run() {
            while (true) {
                // Javac thinks it "might already have been assigned" by the time it is
                // actually assigned below.
                final TwoWaySupport[] s = new TwoWaySupport[1];
                synchronized (LOCK) {
                    while (toDerive.isEmpty()) {
                        logger.finer("derivation thread waiting...");
                        try {
                            LOCK.wait();
                        } catch (InterruptedException e) {
                            assert false : e;
                        }
                    }
                    Iterator<DeriveTask> it = toDerive.iterator();
                    DeriveTask t = it.next();
                    s[0] = (TwoWaySupport) t.support.get();
                    logger.log(Level.FINER, "derivation thread found: {0}", s[0]);
                    if (s[0] == null) {
                        // Dead - support was collected before we got to it.
                        it.remove();
                        continue;
                    }
                    long now = System.currentTimeMillis();
                    if (t.schedule > now) {
                        logger.log(Level.FINER, "derivation thread deferring: {0} for {1}msec", new Object[] {s[0], t.schedule - now});
                        try {
                            LOCK.wait(t.schedule - now);
                        } catch (InterruptedException e) {
                            assert false : e;
                        }
                        // Try again in next round.
                        continue;
                    }
                }
                logger.log(Level.FINER, "derivation thread processing: {0}", s[0]);
                // Out of synch block; we have a support to run.
                s[0].getLock().read(new Runnable() {
                    public void run() {
                        try {
                            s[0].getValueBlocking();
                            // Ignore value and exceptions - gVB is
                            // enough to cache that info and fire changes.
                        } catch (InvocationTargetException e) {
                            // OK, handled separately.
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        } catch (Error e) {
                            e.printStackTrace();
                        }
                    }
                });
                // Don't explicitly remove it from the queue - if it was
                // active, then gVB should have done that itself.
            }
        }
        
    }
    
}
