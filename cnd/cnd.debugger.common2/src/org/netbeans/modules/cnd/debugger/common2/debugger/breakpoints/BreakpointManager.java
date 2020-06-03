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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error;
import org.netbeans.modules.cnd.debugger.common2.debugger.Log;
import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FallbackBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.ItemSelectorResult;

/**
 * This class manages the NativeBreakpoint "database".
 * An instance of this class is associated with each NativeDebugger.
 * <p>
 * Control flow can be outgoing, i.e. from GUI actions to the engine. Methods
 * that implement that sometimes are prefixed wih 'post' (from HTTP POST) and
 * form a "public API".
 * The actual work of sending requests to the engine is performed through the
 * BreakpointProvider interface.
 * <p>
 * Control flow can be incoming, i.e. responses to notifiations from the engine.
 * These methods are sometimes prefixed with 'note' and for a "protected API".
 */
public final class BreakpointManager {

    // Implementatin notes
    //
    // Outward and inward flows are connected. Each outgoing request might adjust
    // the NativeBreakpoint hierarchy and then will record some state in a BreakpointJob.
    // The request is then delegated via the BreakpointProvider interface.
    // A "routing token" is used as a way of connecting any responses to the
    // BreakpointJobs.
    // In incoming notification from an engine uses the routing token to get a
    // BreakpointPlan which summarizes what engine-specific action needs to take
    // place. BreakpointPlan is a simplifying decorator for BreakpointJob.
    // Once and engine has created a Handler/NativeBreakpoint pair it passes
    // control back to BreakpointManager via the note* class of functions.
    // BreakpointManager then finishes any breakpoint hierarchy bookkeeping
    // and we're done.

    private final NativeDebugger debugger;
    private final BreakpointProvider provider;

    private final Handlers handlers = new Handlers();
    private final BreakpointJobs breakpointJobs = new BreakpointJobs();

    public BreakpointManager(NativeDebugger nd, BreakpointProvider provider) {
	this.debugger = nd;
	this.provider = provider;
    }

    public BreakpointProvider provider() {
	return provider;
    }

    private NativeDebuggerManager manager() {
        return NativeDebuggerManager.get();
    }

    private NativeDebugger debugger() {
	return debugger;
    }


    private static class Handlers implements Iterable<Handler> {
	private final ArrayList<Handler> list = new ArrayList<Handler>();

	private Handler[] toArray() {
	    return list.toArray(new Handler[list.size()]);
	}

	// interface Iterable
        @Override
	public Iterator<Handler> iterator() {
	    return list.iterator();
	}

	public Handler byKey(int id) {
	    for (Handler h : list) {
		if (h.getId() == id)
		    return h;
	    }
	    return null;
	}

	public void remove(Handler h) {
	    list.remove(h);
	}

	public void add(Handler h) {
	    list.add(h);
	}
    }

    /**
     * A BreakpointJob (BJ) is a form of continuation for various bpt
     * manipulation pathways.
     * One gets created when we send a command to the engine.
     * When we get responses from the engine we find the corresponding BJ
     * based on a routing token. The BJ contains information that allows us
     * to continue with the pathway.
     */
    private static class BreakpointJob {

        private Kind kind;
        private int routingToken;
        private final NativeBreakpoint target;
        private /* LATER final */ volatile NativeBreakpoint template;
        private final NativeBreakpoint midlevel;
        private Gen gen;

        // 'overload' is set when we're dealing with new bpts through the
        // overload menu.
        //
        private boolean overload;	// is set when we set expectedOverloads
        private int expectedOverloads = 0;
        private int seenOverloads = 0;
        private boolean fallback;
        private boolean requiredIntervention;

        public enum Kind {

            NEW,
            RESTORE1,
            RESTORE2,
            RESTORE3,
            CHANGE,
            REPAIR,
            SPONTANEOUS,
            DELETE,
            DEFUNCT
        };

        public BreakpointJob(Kind kind,
                NativeBreakpoint target,
                NativeBreakpoint template,
                NativeBreakpoint midlevel) {
            switch (kind) {
                case NEW:
                case RESTORE1:
                case RESTORE2:
                case RESTORE3:
                    assert midlevel != null : "BreakpointJob(): null midlevel";
                    break;
            }

            this.kind = kind;
            this.target = target;
            this.template = template;
            this.midlevel = midlevel;
        }

        public void mutate(Kind kind) {
            this.kind = kind;
        }

        public void print() {
            if (Log.Bpt.pathway) {
                System.out.println("BreakpointJob " + routingToken + " " + kind.name() + ":"); // NOI18N
                System.out.println("\ttarget " + target.toString()); // NOI18N
                System.out.println("\ttemplt " + template.toString()); // NOI18N
                System.out.println("\tspread " + gen.toString() + " overload " // NOI18N
                        + overload + " " + expectedOverloads + " " + fallback); // NOI18N
            }
        }

        public Kind kind() {
            return kind;
        }

        public NativeBreakpoint target() {
            return target;
        }

        public NativeBreakpoint template() {
            return template;
        }

        public NativeBreakpoint midlevel() {
            return midlevel;
        }

        public void setTemplate(NativeBreakpoint template) {
            assert this.template == null;
            assert kind == Kind.SPONTANEOUS;
            this.template = template;
        }

        public void setGen(Gen gen) {
            this.gen = gen;
        }

        public Gen gen() {
            return gen;
        }

        public void setRoutingToken(int routingToken) {
	    assert RoutingToken.BREAKPOINTS.isSameSubsystem(routingToken);
            this.routingToken = routingToken;
        }

        public int getRoutingToken() {
            return routingToken;
        }

        public void setExpectedOverloads(int expectedOverloads) {
            assert expectedOverloads > 0 :
                    "BreakpointJob.setExpecetdOverloads(<= 0)"; // NOI18N
            assert this.expectedOverloads == 0 :
                    "BreakpointJob.setExpecetdOverloads() called more than oncee"; // NOI18N
            this.expectedOverloads = expectedOverloads;
            this.overload = true;
        }

        public boolean isOverload() {
            return overload;
        }

        public int getOutstandingOverloads() {
            assert isOverload();
            return expectedOverloads - seenOverloads;
        }

        public boolean isFirstOverload() {
            assert isOverload();
            return seenOverloads == 1;
        }

        public boolean isLastOverload() {
            assert isOverload();
            return seenOverloads == expectedOverloads;
        }

        public void seeOverload() {
            assert isOverload();
            seenOverloads++;
            assert seenOverloads <= expectedOverloads :
                    "BreakpointJob.seeOverload(): seeing more than expected"; // NOI18N
        }

        public void setFallback(boolean fallback) {
            this.fallback = fallback;
        }

        public boolean isFallback() {
            return fallback;
        }

        public void setRequiredIntervention(boolean requiredIntervention) {
            this.requiredIntervention = requiredIntervention;
        }

        public boolean isRequiredIntervention() {
            return requiredIntervention;
        }
    }

    private static final class BreakpointJobs {

	private final Map<Integer, BreakpointJob> map =
	    new ConcurrentHashMap<Integer, BreakpointJob>();

        public void put(int rt, BreakpointJob bj) {
            bj.print();
            map.put(rt, bj);
        }

        public BreakpointJob get(int rt) {
            BreakpointJob bj;
            if (rt == 0) {
                if (Log.Bpt.pathway) {
                    System.out.printf("BreakpointJobs.get(): rt == 0\n"); // NOI18N
                }
                bj = new BreakpointJob(BreakpointJob.Kind.SPONTANEOUS, null, null, null);
                bj.setGen(Gen.primary(null));
                return bj;

            } else {
		assert RoutingToken.BREAKPOINTS.isSameSubsystem(rt);
                bj = map.get(rt);
//                assert bj != null :
//                        "BreakpointJobs.get(): no bpt for rt " + rt; // NOI18N
                if (bj == null) {
                    if (Log.Bpt.pathway) {
                        System.out.println("BreakpointJobs.get(): no bpt for rt " + rt);//NOI18N
                    }
                    bj = new BreakpointJob(BreakpointJob.Kind.SPONTANEOUS, null, null, null);
                    bj.setGen(Gen.primary(null));
                    return bj;
                }
                if (bj.isOverload()) {
                    bj.seeOverload();
                    if (bj.isLastOverload()) {
                        map.remove(rt);
                    }
                } else {
                    map.remove(rt);
                }
                return bj;
            }
        }
    }
    
    public boolean hasBreakpointJobAt(String fileName, int lineNo) {
        final Map<Integer, BreakpointJob> map = breakpointJobs.map;
        for (BreakpointJob breakpointJob : map.values()) {
            NativeBreakpoint bp = breakpointJob.template();
            if (bp != null && bp.matchesLine(fileName, lineNo)) {
                return true;
            }
        }
        return false;
    }
    
    private BreakpointJob getBreakpointJob(int rt) {
        return breakpointJobs.get(rt);
    }

    private void reinstateBreakpointJob(BreakpointJob originalJob) {
        int rt = originalJob.getRoutingToken();
        assert rt != 0;
        breakpointJobs.put(rt, originalJob);
    }

    /**
     * Description of the kind of primitive breakpoint related message
     * arriving from the candidate engine.
     */
    public static enum BreakpointMsg {
	NEW,
	REPLACE
    }

    /**
     * Description of the kind of primitive operation that needs to be performed
     * by the engine provider.
     */
    public static enum BreakpointOp {
	NEW,
	RESTORE,
	MODIFY
    }

    /**
     * Describes the (private) plan of NativeDebuggerImpl .
     * <ul>
     * <li>
     * Acquire using getBreakpointPlan(int routingToken).
     * <li>
     * Use 'op' to create a Handler/NativeBreakpoint pair with the
     * help of HandlerExpert.
     * <li>
     * Pass on the Handler and plan to one of newHandlerFinish() or noteReplacedHandler().
     */
    public static final class BreakpointPlan {
	private final BreakpointOp op;
	private final BreakpointJob job;
	private final Handler originalHandler;

	BreakpointPlan(BreakpointOp action, BreakpointJob job) {
	    this.op = action;
	    this.job = job;
	    if (job.target() != null) {
		assert job.target().isSubBreakpoint();
		originalHandler = job.target().getHandler();
	    } else {
		originalHandler = null;
	    }
	}

	BreakpointJob job() {
	    return job;
	}

	public BreakpointOp op() {
	    return op;
	}

	public boolean isFallback() {
	    return job.isFallback();
	}

	public NativeBreakpoint restored() {
	    assert job.kind() == BreakpointJob.Kind.RESTORE2;
	    return job.template();
	}

	public NativeBreakpoint template() {
	    return job.template();
	}

	public NativeBreakpoint target() {
	    return job.target();
	}

	public Handler originalHandler() {
	    return originalHandler;
	}
    }

    public void postEnableAllHandlers(boolean enable) {
	provider().postEnableAllHandlersImpl(enable);
    }

    public void postDeleteAllHandlers() {
	provider().postDeleteAllHandlersImpl();
    }


    /**
     * Deal with breakpoint errors.
     * We either create a "broken" breakpoint and silence the error by
     * returning true, or return false and let the user see the error.
     *
     * We get errors in the following situations:
     * - During restoration.
     *   'restoredBreakpoints' has a list of bpts being restored.
     *   we convert them to broken breakpoints.
     * - During bpt creation.
     *   'newBreakpoint' is set.
     *   We make this one be broken as well because the bpt might have been
     *   created in anticipation of a new session which has nothing to do
     *   with us.
     * - During breakpoint editing.
     *   If the bpt is broken 'repairedBreakpoint' is set.
     *   For now we present an error to the user for these.
     */
    public boolean noteBreakpointError(int rt, Error error) {

        boolean beSilent;
        boolean convert;
        boolean replace;

        BreakpointJob bj = getBreakpointJob(rt);
        NativeBreakpoint template = bj.template();

        switch (bj.kind()) {
            default:
            case SPONTANEOUS:
                // should never happen
		assert false : "spontaneous breakpointError";
                beSilent = false;
                convert = false;
                replace = false;
		break;

            case CHANGE:
            case REPAIR:
                // fix for 6567352
                if (bj.gen().isPrimary()) {
                    // validation of initial change failed; complain
                    if (error.isCancelled()) {
                        beSilent = true;
                    } else {
                        beSilent = false;
                    }
                    convert = false;
                    replace = false;
                } else {
                    // validation of spread of change failed;
                    // silently convert to broken
                    beSilent = true;
                    convert = false;
                    replace = true;
                }
                break;

            case RESTORE1:
            case RESTORE2:
            case RESTORE3:
                // restoration failed
                beSilent = true;
                convert = true;
                replace = false;
                break;

            case NEW:
                // new breakpoint
                // We don't complain because this may be a bpt intended for
                // a session about to be started, not the current session.
                beSilent = true;
                convert = true;
                replace = false;
                break;

            case DEFUNCT:
                // bpt went defunct shortly after being created.
                // Most common reason is error.isOodSrc()
                // OLD restored = false;	// we actually don't know
                // we want to be silent on restores and not otherwise but
                // we don't know if we're restoring.
                beSilent = true;
                convert = false;
                replace = false;
                bj.target().getHandler().setError(error.first());
                break;
        }


        if (NativeDebuggerManager.isPerTargetBpts()) {
            // per-target bpts
            if (bj.kind() != BreakpointJob.Kind.RESTORE1 &&
                bj.kind() != BreakpointJob.Kind.RESTORE2 &&
                bj.kind() != BreakpointJob.Kind.RESTORE3 &&
		bj.kind() != BreakpointJob.Kind.DEFUNCT) {

                convert = false;
                beSilent = false;

                if (bj.kind() != BreakpointJob.Kind.REPAIR &&
                        bj.kind() != BreakpointJob.Kind.CHANGE) {
                    // Undo the effct of template.makeMidlevelCopy():
                    template.removeOnlyChild();
                }
            }
        }

        if (replace) {
            replaceBrokenHandler(template, bj.target(), error.first(), bj);
        } else if (convert) {
            newBrokenHandler(template, bj.midlevel(), error.first(), bj);
        }

        return beSilent;
    }

    public void noteDefunctBreakpoint(NativeBreakpoint target, int rt) {
        BreakpointJob bj =
                new BreakpointJob(BreakpointJob.Kind.DEFUNCT, target, null, null);
        bj.setRoutingToken(rt);
        bj.setFallback(target instanceof FallbackBreakpoint);
        assert rt != 0;
        breakpointJobs.put(rt, bj);
    }

    public ItemSelectorResult noteMultipleBreakpoints(int rt, String title, int nitems, String items[]) {
	// called from DebuggerManager.popup()
	// If popup() is pulled into us then noteMultipleBreakpoints
	// can become private.

        BreakpointJob bj = getBreakpointJob(rt);

        assert !bj.isOverload() :
                "multipleBreakpoints(): " + // NOI18N
                "Got OVERLOAD while processing OVERLOAD"; // NOI18N

        switch (bj.kind()) {
            case RESTORE1:
            case RESTORE2:
            case RESTORE3:
                return chooseAllItems(bj, nitems);
            case CHANGE:
            case REPAIR:
            case NEW:
            case SPONTANEOUS:
                if (bj.gen().isSecondary()) {
                    return chooseAllItems(bj, nitems);
                }

                // pass the popup on to the user
                ItemSelectorResult result;
                boolean cancelable = true;
                boolean multiple_selection = true;
                result = manager().popupHelp(title,
                        nitems, items,
                        cancelable, multiple_selection);
                if (result.isCancelled()) {
                    if (bj.kind() != BreakpointJob.Kind.SPONTANEOUS) {
                        reinstateBreakpointJob(bj);
                    }
                    return result;
                }

                if (result.nSelected() == 1) {
                    if (bj.kind() == BreakpointJob.Kind.SPONTANEOUS) {
                        // spontaneous case.
                        // we'll create the parent when we come back.
                        // Don't need to reinstatre a BJ.
                    } else {
                        rememberOverloadedBreakpoint(bj, result.nSelected(), true);
                    }
                // we'll get a newHandler()

                } else {
                    if (bj.kind() == BreakpointJob.Kind.SPONTANEOUS) {
                        // We get an overload menu due to a bpt command in dbx.
                        // W/o an RT we won't be able to assign the mutiply
                        // created handlers to the proper top-level handler, so
                        // create a new RT, assign it to the BJ and return it
                        // to dbx.
                        // Dbx needs to be very careful and reset it
                        // appropriately (glue_reset_routing_token()).
                        result.setRoutingToken(
                                RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt());
                        bj.setRoutingToken(result.getRoutingToken());
                    }

                    // Even though we got a popup if all choices were
                    // selected then we don't consider this as an intervention
                    // because that is what happens in the default (RESTORE)
                    // case.
                    // However, with spontaneous bpts it gets a bit messy
                    // because we don't have a midlevel or toplevel bpt
                    // and we clone it from the subbpt which means the
                    // toplevel bpt ends up with the fully qualified form and
                    // on a subsequent debug only that form is converted.
                    // So even if all items are selected we mark the
                    // spontaneous bpt with the intervention bit.

                    boolean requiredIntervention;
                    if (bj.kind() == BreakpointJob.Kind.SPONTANEOUS) {
                        requiredIntervention = true;
                    } else if (result.nSelected() == nitems) {
                        requiredIntervention = false;	// selected all
                    } else {
                        requiredIntervention = true;
                    }

                    rememberOverloadedBreakpoint(bj,
		                             result.nSelected(),
                                             requiredIntervention);
                // on CHANGE
                // breakpoint is editable
                // we'll get a replaceHandler and a bunch of newHandler()s

                // on NEW
                // breakpoint is non-editable
                // we'll get a bunch of newHandler()s
                }

                return result;
        }
        return null;
    }

    // interface NativeDebugger
    public BreakpointBag breakpointBag() {
	return manager().breakpointBag();
    }

    /*
     * Breakpoint, Handler and Event stuff
     */

    // interface NativeDebugger
    public Handler[] getHandlers() {
        return handlers.toArray();
    }


    // interface NativeDebugger
    public ModelChangeDelegator breakpointUpdater() {
	return manager().breakpointUpdater();
    }

    /**
     * Utility method to convert error HandlerCommand's to something
     * reasonable.
     * SHOULD move into HandlerExpert?
     */
    private String handlerError(NativeBreakpoint b, HandlerCommand hc) {
	final String msg;
	if (hc.getData() == null) {
	    msg = Catalog.format("FMT_UnsupportedBpt",		// NOI18N
				 b.getBreakpointType().getTypeDisplayName(),
				 debugger().debuggerType());
	} else {
	    msg = hc.getData();
	}
	return msg;
    }

    public BreakpointPlan getBreakpointPlan(int rt, BreakpointMsg msg) {
        BreakpointJob bj = getBreakpointJob(rt);
	final BreakpointPlan bp;
        switch (bj.kind()) {

	    // The following three cases correspond to the three cases in
	    // postRestoreBreakpoints().
	    // Also see newBrokenHandler().

            case RESTORE1:
		// restored new handler not matching any context
		// "virgin" bpt
		// create new sub-bpt
		assert msg == BreakpointMsg.NEW;
		bp = new BreakpointPlan(BreakpointOp.NEW, bj);
		break;

            case RESTORE2:
		// restored handler matching existing unbound ghost bpt
		// (first session)
		// bind existing sub-bpt
		assert msg == BreakpointMsg.NEW;
		bp = new BreakpointPlan(BreakpointOp.RESTORE, bj);
		break;

            case RESTORE3:
		// restored handler matching existing bound bpt
		// (second session)
		// create new sub-bpt
		assert msg == BreakpointMsg.NEW;
		bp = new BreakpointPlan(BreakpointOp.NEW, bj);
		break;

            case NEW:
            case SPONTANEOUS:
		// regular new handler
		assert msg == BreakpointMsg.NEW;
		bp = new BreakpointPlan(BreakpointOp.NEW, bj);
		break;

            case CHANGE:
		// A change which results in an overloaded menu will result
		// in one replaceHandler and many newHandlers.

		if (msg == BreakpointMsg.NEW && bj.isOverload()) {
		    // assert bj.isOverload(); not valid for gdb syscall breakpoints
		    bp = new BreakpointPlan(BreakpointOp.NEW, bj);
		} else {
		    bp = new BreakpointPlan(BreakpointOp.MODIFY, bj);
		}

		break;

            case REPAIR:
		// successful fix of a broken handler:
		// very similar to GdbDebuggerImpl.repairHandler() (SHOULD factor)
		// somewhat similar to replaceHandler
		assert msg == BreakpointMsg.NEW;
		bp = new BreakpointPlan(BreakpointOp.MODIFY, bj);
		break;

	    default:
		bp = null;
		break;
        }

	return bp;
    }

    public void noteReplacedHandler(BreakpointPlan bp, Handler replacementHandler) {
        BreakpointJob bj = bp.job();

        NativeBreakpoint targetBreakpoint = bj.target();
	Handler targetHandler = targetBreakpoint.getHandler();

	if (replacementHandler != null) {
	    assert provider().handlerExpert().replacementPolicy() ==
		   HandlerExpert.ReplacementPolicy.EXPLICIT;
	    // swap in new handler for old one
	    targetBreakpoint.setHandler(replacementHandler);
	    handlers.add(replacementHandler);
	} else {
	    assert provider().handlerExpert().replacementPolicy() ==
		   HandlerExpert.ReplacementPolicy.INPLACE;
	}

        NativeBreakpoint template = bj.template();
        if (template.isChangeInDefiningProperty()) {
            targetHandler.setFired(false);
        }

	// spread the change
        NativeBreakpoint target = targetHandler.breakpoint();
        NativeBreakpoint mid = target.getParent();

        mid.spreadChange(target, template, bj.gen());
    }

    public void noteNewHandler(int rt, BreakpointPlan bp, Handler handler) {
        BreakpointJob bj = bp.job();
	NativeBreakpoint mid = bj.midlevel();
        switch (bj.kind()) {
            case RESTORE1:
		// restored new handler not matching any context
		// "virgin" bpt
		// create new sub-bpt
		mid.addSubBreakpoint(handler.breakpoint());
                handlers.add(handler);
		break;

            case RESTORE2:
		// restored handler matching existing unbound ghost bpt
		// (first session)
		// bind existing sub-bpt
		NativeBreakpoint restored = bj.template();
		mid.bindTo(debugger());
		restored.bindTo(debugger());
                handlers.add(handler);

		if (bj.isOverload() && bj.isFirstOverload())
		    bj.mutate(BreakpointJob.Kind.RESTORE3);
		break;

            case RESTORE3:
		// restored handler matching existing bound bpt
		// (subseqent session)
		// create new sub-bpt
		mid.addSubBreakpoint(handler.breakpoint());
                handlers.add(handler);
                break;

            case NEW:
            case SPONTANEOUS:
                 {
                    // regular new handler
                    NativeBreakpoint created;
                    if (bj.kind() == BreakpointJob.Kind.SPONTANEOUS) {
                        // spontaneous creation from dbx cmdline
                        // need to create both a toplevel and a midlevel bpt.

                        // We store the created top-level one in the BJ template
                        // field so it may get reused with overloaded bpts.
                        created = bj.template();
                        if (created == null) {
                            created = handler.breakpoint().makeToplevelCopy(bj.isOverload());
                            bj.setTemplate(created);
                        }

                        // create midlevel bpt
                        if (!bj.isOverload() || bj.isFirstOverload()) {
                            mid = created.makeMidlevelCopy();
                            created.setMidBreakpointFor(mid, debugger());
                            mid.setAdjusted(bj.isRequiredIntervention());
                        } else {
                            mid = created.getMidlevelFor(debugger());
                            assert mid.isMidlevel();
                        }
                    } else {
                        // created in gui
                        mid = bj.midlevel();
                    }

                    mid.addSubBreakpoint(handler.breakpoint());

                    created = bj.template();

                    handlers.add(handler);

                    if (!bj.isOverload() || bj.isFirstOverload()) {
                        breakpointBag().add(created);
                    }

                    if (bj.kind() == BreakpointJob.Kind.SPONTANEOUS &&
                            bj.gen().isPrimary()) {

                        // mimic what we do at the beginning of
                        // Handler.postNewHandler
                        // SHOULD do this in one central place?
                        created.seedToplevelAnnotations();
                    }

                    manager().bringDownDialog();

                    if (!bj.isOverload()) {
                        spreadBreakpointCreation(debugger(), handler.breakpoint());
                    } else {
                        if (bj.isFirstOverload()) {
                            spreadBreakpointCreation(debugger(), handler.breakpoint());
                        }
                    }

                }
                break;

            case CHANGE:
                 {
                    // A change which results in an overloaded menu will result
                    // in one replaceHandler and many newHandlers.
                    assert bj.isOverload();
                    NativeBreakpoint target = bj.target();
                    NativeBreakpoint template = bj.template();
                    NativeBreakpoint root = bj.target().getParent().getParent();

                    mid = root.getMidlevelFor(debugger());
                    assert mid.isMidlevel();

                    mid.addSubBreakpoint(handler.breakpoint());

                    handlers.add(handler);
                }
                break;

            case REPAIR:
                 {
                    // successful fix of a broken handler:
                    // very similar to GdbDebuggerImpl.repairHandler() (SHOULD factor)
                    // somewhat similar to replaceHandler

                    // target is the sub-bpt we're modifying
                    // template is the pattern we want to propagate

                    NativeBreakpoint target = bj.target();
                    NativeBreakpoint template = bj.template();

                    // We don't have a dbx handler (that's why we're getting a
                    // newHandler, but we do have a local Handler associated with
                    // target!
                    // We remove and add it so it goes under a different key

                    Handler targetHandler = target.getHandler();

		    if (provider().handlerExpert().replacementPolicy() == HandlerExpert.ReplacementPolicy.INPLACE) {
			assert handler == targetHandler;
		    } else {
			assert handler != bp.originalHandler();
			handlers.remove(bp.originalHandler());
			handlers.add(handler);
		    }

                    mid = target.getParent();
                    mid.spreadChange(target, template, bj.gen());

                    if (template.isChangeInDefiningProperty()) {
                        bj.mutate(BreakpointJob.Kind.CHANGE);
                    }

                    manager().bringDownDialog();

                }
                break;
        }
    }


    /**
     * In effect "convert" 'originalJob' into a new job with
     * isOverload() == true;
     */
    private void rememberOverloadedBreakpoint(BreakpointJob originalJob,
            int nOverloads,
            boolean requiredIntervention) {
        int rt = originalJob.getRoutingToken();
        NativeBreakpoint target = originalJob.target();
        NativeBreakpoint mid = originalJob.midlevel();
        NativeBreakpoint template = originalJob.template();

        BreakpointJob bj = null;
        switch (originalJob.kind()) {
            case SPONTANEOUS:
            case RESTORE1:
            case RESTORE2:
            case RESTORE3:
            case NEW:
            case REPAIR:
            case CHANGE:
                bj = new BreakpointJob(originalJob.kind, target, template, mid);
                break;
            case DELETE:
                assert false;
                break;
        }
        if (nOverloads > 1) {
            // marks job as overload
            bj.setExpectedOverloads(nOverloads);
        } else {
            // 6610102
            // User chose one item it will act like a plain bpt
        }
        bj.setGen(originalJob.gen());
        bj.setFallback(originalJob.isFallback());
        bj.setRoutingToken(rt);
        assert rt != 0;
        breakpointJobs.put(rt, bj);

        if (mid == null) {
            // Arrange to pass on requiredIntervention when we create the
            // midlevel in newHandler().
            bj.setRequiredIntervention(requiredIntervention);
        } else {
            mid.setAdjusted(requiredIntervention);
        }
    }

    private void rememberRestoredBreakpoint(NativeBreakpoint template,
            NativeBreakpoint mid,
            int rt) {

        assert template.isToplevel() || template.isSubBreakpoint();
        assert !template.isEditable();
        assert mid.isMidlevel();

	final BreakpointJob.Kind kind;

	if (template.isToplevel()) {
	    // restored new handler not matching any context
	    // "virgin" bpt
	    // create new sub-bpt
	    kind = BreakpointJob.Kind.RESTORE1;

	} else if (! template.isBound()) {
	    // restored handler matching existing unbound ghost bpt
	    // (first session)
	    // bind existing sub-bpt
	    NativeBreakpoint restored = template;
	    assert restored.isSubBreakpoint();
	    assert !restored.isBound();
	    kind = BreakpointJob.Kind.RESTORE2;

	} else {
	    // restored handler matching existing bound bpt
	    // (subseqent session)
	    // create new sub-bpt
	    kind = BreakpointJob.Kind.RESTORE3;
	}

        BreakpointJob bj =
                new BreakpointJob(kind, null, template, mid);
        bj.setRoutingToken(rt);
        bj.setFallback(template instanceof FallbackBreakpoint);
        assert rt != 0;
        breakpointJobs.put(rt, bj);
    }

    private void rememberNewBreakpoint(NativeBreakpoint template,
            NativeBreakpoint mid,
            int rt) {

        assert template.isToplevel();
        assert !template.isEditable();
        assert mid.isMidlevel();

        BreakpointJob bj =
                new BreakpointJob(BreakpointJob.Kind.NEW, null, template, mid);
        bj.setRoutingToken(rt);
        bj.setFallback(template instanceof FallbackBreakpoint);
        bj.setGen(Gen.primary(null));
        assert rt != 0;
        breakpointJobs.put(rt, bj);
    }

    private void rememberRepairedBreakpoint(NativeBreakpoint target,
            NativeBreakpoint template,
            Gen gen) {

        assert template.isEditable();
        assert !target.isEditable();

        int rt = target.getRoutingToken();
        BreakpointJob bj =
                new BreakpointJob(BreakpointJob.Kind.REPAIR, target, template, null);
        bj.setGen(gen);
        bj.setRoutingToken(rt);
        bj.setFallback(target instanceof FallbackBreakpoint);
        assert rt != 0;
        breakpointJobs.put(rt, bj);
    }

    private void rememberChangingBreakpoint(NativeBreakpoint target,
            NativeBreakpoint template,
            Gen gen) {
        assert template.isEditable();
        assert !target.isEditable();

        int rt = target.getRoutingToken();
        BreakpointJob bj =
                new BreakpointJob(BreakpointJob.Kind.CHANGE, target, template, null);
        bj.setGen(gen);
        bj.setRoutingToken(rt);
        bj.setFallback(target instanceof FallbackBreakpoint);
        assert rt != 0;
        breakpointJobs.put(rt, bj);
    }

    private void rememberDeletedBreakpoint(NativeBreakpoint target, Gen gen) {
        assert target.isSubBreakpoint();

        int rt = target.getRoutingToken();
        BreakpointJob bj =
                new BreakpointJob(BreakpointJob.Kind.DELETE, target, null, null);
        bj.setGen(gen);
        bj.setRoutingToken(rt);
        bj.setFallback(target instanceof FallbackBreakpoint);
        assert rt != 0;
        breakpointJobs.put(rt, bj);
    }

    void postRestoreBreakpoint(NativeBreakpoint top) {
        // If we find matches we use the sub-breakpoints as templates
            // othewise we use the topLevel one as a template
        if (!top.isEnabled()) {
            return;
        }

        List<NativeBreakpoint> matches = top.findByContext(debugger().context());

        NativeBreakpoint mid;

        // The following three cases correspond to the three cases in
        // newHandler() case RESTORE.
        // Also see newBrokenHandler().

        if (matches.isEmpty()) {
            if (NativeDebuggerManager.isPerTargetBpts()) {
                // if per-target bpts don't restore it.
                return;
            }

            // restored new handler not matching any context
            // "virgin" bpt
            // create new midlevel bpt
            mid = top.makeMidlevelCopy();
            top.setMidBreakpointFor(mid, debugger());

            restoreBreakpoint(top, mid);

        } else {
            // We may have more than one match for a given context
            // I can't think of a good algorithm for deciding which one
            // to pick so we pick the first one.
            // This is a bit analogous to what happens when we start two
            // sessions off the same project where the sessions share
            // RunProfiles etc.

            NativeBreakpoint match = matches.get(0);
            assert match.isMidlevel();

            if (!match.isBound()) {
                // restoring from a ghost, reuse the midlevel bpt
                mid = match;
            } else {
                // restoring from a bound bpt, need new midlevel bpt
                mid = match.makeMidlevelCopy();
                top.setMidBreakpointFor(mid, debugger());
            }

            for (NativeBreakpoint match2 : match.getChildren()) {
                restoreBreakpoint(match2, mid);
            }
        }
    }

    /**
     * Restore bpts associated with a profile.
     * Called from profileBridge.initialApply()
     * Will call back to postRestoreHandler()
     */
    public void postRestoreBreakpoints(BreakpointBag bb) {

        for (NativeBreakpoint top : bb.getBreakpoints()) {
            postRestoreBreakpoint(top);
        }
    }

    private void restoreBreakpoint(NativeBreakpoint template,
            NativeBreakpoint midLevel) {
        final HandlerCommand hc = provider().handlerExpert().commandFormNew(template);

        final int rt = template.getRoutingToken();
        rememberRestoredBreakpoint(template, midLevel, rt);

        if (!hc.isError()) {
            provider().postRestoreHandler(rt, hc);
        } else {
	    BreakpointJob bj = getBreakpointJob(rt);
            newBrokenHandler(template,
			     midLevel,
			     handlerError(template, hc),
		             bj);
        }
    }

    private void spreadBreakpointCreation(NativeBreakpoint b) {
        // very similar to postRestoreBreakpoints
        NativeBreakpoint topLevel = b.getParent().getParent();

        NativeBreakpoint midLevel = topLevel.makeMidlevelCopy();
        topLevel.setMidBreakpointFor(midLevel, debugger());

        final int rt = topLevel.getRoutingToken();
        rememberRestoredBreakpoint(topLevel, midLevel, rt);

        final HandlerCommand hc = provider().handlerExpert().commandFormNew(topLevel);
        if (!hc.isError()) {
            provider().postRestoreHandler(rt, hc);
        } else {
	    BreakpointJob bj = getBreakpointJob(rt);
            newBrokenHandler(topLevel,
		midLevel,
		handlerError(topLevel, hc),
		bj);
        }
    }
    /**
     * Spread breakpoint to all sessions other than 'origin'.
     */
    private void spreadBreakpointCreation(NativeDebugger origin,
        NativeBreakpoint b) {
        // DEBUG System.out.println("Spreading bpt creation ...");

        if (NativeDebuggerManager.isPerTargetBpts()) {
            return;		// no spreading if per-target bpts
        }
        NativeSession[] sessions = NativeDebuggerManager.get().getSessions();
        for (int sx = 0; sx < sessions.length; sx++) {
            NativeSession s = sessions[sx];
            DebuggerEngine engine = s.coreSession().getCurrentEngine();
            NativeDebugger candidate = engine.lookupFirst(null, NativeDebugger.class);
            if (candidate == null) {
                // DEBUG System.out.println("\t... null candidate");
                continue;
            } else if (candidate == origin) {
                // DEBUG System.out.println("\t... origin candidate (skipped)");
                continue;
            } else {
                // DEBUG System.out.println("\t... " + candidate);
                candidate.bm().spreadBreakpointCreation(b);
            }
        }
    }

    public void postCreateHandler(int routingToken,
            HandlerCommand hc,
            NativeBreakpoint template) {

	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Bpt.pathway) {
	    System.out.printf("DbxDebuggerImpl.postCreateHandler(%s)\n", // NOI18N
		hc);
	}
        assert template.isToplevel();

        NativeBreakpoint mid = template.makeMidlevelCopy();
        template.setMidBreakpointFor(mid, debugger());
        rememberNewBreakpoint(template, mid, routingToken);

        if (hc.isError()) {
	    BreakpointJob bj = getBreakpointJob(routingToken);
            newBrokenHandler(template, mid, handlerError(template, hc), bj);
        } else {
	    provider().postCreateHandlerImpl(routingToken, hc);
	    // We'll come back either via newHandler() or noteBreakpointError().
        }
    }

    public void postChangeHandler(NativeBreakpoint editedBreakpoint,
            HandlerCommand hc,
            NativeBreakpoint targetBreakpoint,
            Gen gen) {

        assert targetBreakpoint.isSubBreakpoint();

        rememberChangingBreakpoint(targetBreakpoint, editedBreakpoint, gen);

        if (!hc.isError()) {
            // this will show up as a handler_replace or error
            int rt = targetBreakpoint.getRoutingToken();
            provider().postChangeHandlerImpl(rt, hc);
        }
    }

    public void postRepairHandler(NativeBreakpoint editedBreakpoint,
                                  HandlerCommand hc,
                                  NativeBreakpoint targetBreakpoint,
                                  Gen gen) {

        assert targetBreakpoint.isSubBreakpoint();

        rememberRepairedBreakpoint(targetBreakpoint, editedBreakpoint, gen);

        if (!hc.isError()) {
            // this will show up as a handler_new or error
            int rt = targetBreakpoint.getRoutingToken();
            provider().postRepairHandlerImpl(rt, hc);
        }
    }

    // interface NativeDebugger
    public void postDeleteHandler(NativeBreakpoint b, Gen gen) {

        // We come through here for both primary and secondary Gen

        if (b.isBroken()) {
            deleteHandler(b.getHandler(), gen, false);
        } else {
            rememberDeletedBreakpoint(b, gen);
	    provider().postDeleteHandlerImpl(b.getRoutingToken(), b.getId());
        }
    }

    public Handler findHandler(int id) {
        return handlers.byKey(id);
    }

    /**
     * Replace a 'target' with a broken one.
     * This only happens to a second generation.
     * In a first generation scenario we post an error and don't touch
     * existing NB's.
     */
    private void replaceBrokenHandler(NativeBreakpoint template,
            NativeBreakpoint target,
            String msg,
            BreakpointJob bj) {

        assert target.isSubBreakpoint();

        Handler targetHandler = target.getHandler();
        handlers.remove(targetHandler);

        target.copyFrom(template);

        // Create a new handler
        final Handler handler = provider().handlerExpert().childHandler(target);

        // Mark it as broken.
        handler.setError(msg);

        handlers.add(handler);
        target.setHandler(handler);
        target.update();
        target.getParent().changeOne(template, null);
    }

    private void newBrokenHandler(NativeBreakpoint template,
            NativeBreakpoint midLevel,
            String msg,
            BreakpointJob bj) {

        // 'breakpoint' is either a bpt being restored or a new breakpoint
        // It may be top-level or a sub-bpt if a match was used as a template

        NativeBreakpoint topLevel;
        NativeBreakpoint subBreakpoint;
	final boolean restored;

	// 6636805
	// mimic case RESTORE in newHandler
	switch (bj.kind()) {
	    case RESTORE1:
                // restored new handler not matching any context
                // "virgin" bpt
                // create new sub-bpt
                topLevel = (template.isEditable()) ? template.original() : template;
                subBreakpoint = null;
		restored = true;
		break;

	    case RESTORE2:
                // restored handler matching existing unbound ghost bpt
                // (first session)
                // bind existing sub-bpt
                topLevel = midLevel.getParent();
                subBreakpoint = template;
		restored = true;
		break;

	    case RESTORE3:
                // restored handler matching existing bound bpt
                // (subseqent session)
                // create new sub-bpt
                topLevel = template.getParent().getParent();
                subBreakpoint = null;
		restored = true;
		break;

	    default:
		// new bpt
		// create new sub-bpt
		// (spreading too?)
		topLevel = (template.isEditable()) ? template.original() : template;
		subBreakpoint = null;
		restored = false;
		break;
	}

        Handler handler;

        if (subBreakpoint == null) {
            handler = provider().handlerExpert().childHandler(topLevel);
            midLevel.addSubBreakpoint(handler.breakpoint());
            if (!restored) {
                breakpointBag().add(topLevel);
            }
            handlers.add(handler);

            if (handler.breakpoint() instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) handler.breakpoint();
                lb.addAnnotation(lb.getFileName(), lb.getLineNumber(), 0);
            }


        } else {
            handler = subBreakpoint.getHandler();
            if (handler == null) {
                handler = provider().handlerExpert().childHandler(subBreakpoint);
                subBreakpoint.bindTo(debugger());
                midLevel.bindTo(debugger());
                handlers.add(handler);
            // SHOULD we do something with annotations?
            }
        }

        // Mark it as broken.
        handler.setError(msg);

        handler.breakpoint().update();

        if (!restored) {
             spreadBreakpointCreation(debugger(), handler.breakpoint());
        }
    }



    // interface NativeDebugger
    public void deleteHandlerById(int routingToken, int hid) {
        Handler handler = findHandler(hid);
        if (handler != null) {
            BreakpointJob bj = getBreakpointJob(routingToken);
            deleteHandler(handler, bj.gen(), false);
        }
    }

    public void deleteHandler(Handler h, Gen gen, boolean finishing) {

        h.cleanup();

        // take out of our list
        handlers.remove(h);

        final NativeBreakpoint sub = h.breakpoint();
        final NativeBreakpoint mid = sub.getParent();

        if (! finishing) {
            // user initiated delete
            sub.primDelete(false, gen);

        } else {
            // finishing a session
            if (mid == null) {
                return;
            }
            if (!mid.isBound()) {
                return;
            }
            if (mid.isUnique()) {
                mid.unbind();		// turn the breakpoint into a ghost
            } else {
                mid.primDelete(true, gen);
            }
        }
    }

    /**
     * Used during restoration and spreading to choose all variations
     * automatically w/o asking the user.
     */
    private ItemSelectorResult chooseAllItems(BreakpointJob bj, int nitems) {
        rememberOverloadedBreakpoint(bj, nitems, false);

	return ItemSelectorResult.selectAll(nitems);
    }

    public void removeHandlers() {
        for (Handler h : getHandlers()) {
            NativeBreakpoint b = h.breakpoint();
            b.getParent().getParent().showAnnotationsFor(false, debugger);
            b.getParent().unbind();
            handlers.remove(h);
        }
    }

    public void simpleRemove(Handler h) {
	handlers.remove(h);
    }
}
