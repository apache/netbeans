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

package org.netbeans.modules.profiler.ppoints.ui;

import java.awt.event.ActionEvent;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.Utils;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.io.File;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "ShowOppositeProfilingPointAction_NoEndDefinedMsg=No end point defined for this Profiling Point",
    "ShowOppositeProfilingPointAction_NoDataString=<No Data Available>",
    "ShowOppositeProfilingPointAction_EndActionName=Go To End Point",
    "ShowOppositeProfilingPointAction_StartActionName=Go To Start Point"
})
public class ShowOppositeProfilingPointAction extends SystemAction implements ContextAwareAction {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class InvocationLocationDescriptor {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private CodeProfilingPoint.Location location;
        private CodeProfilingPoint.Location oppositeLocation;
        private boolean isStartLocation;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        InvocationLocationDescriptor(CodeProfilingPoint.Paired profilingPoint, CodeProfilingPoint.Location location) {
            this.location = location;
            computeOppositeLocation(profilingPoint);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        CodeProfilingPoint.Location getLocation() {
            return location;
        }

        CodeProfilingPoint.Location getOppositeLocation() {
            return oppositeLocation;
        }

        boolean isStartLocation() {
            return isStartLocation;
        }

        private void computeOppositeLocation(CodeProfilingPoint.Paired profilingPoint) {
            CodeProfilingPoint.Location startLocation = profilingPoint.getStartLocation();

            if (new File(startLocation.getFile()).equals(new File(location.getFile()))
                    && (startLocation.getLine() == location.getLine())) {
                oppositeLocation = profilingPoint.getEndLocation();
                isStartLocation = true;
            } else {
                oppositeLocation = profilingPoint.getStartLocation();
                isStartLocation = false;
            }
        }
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ShowOppositeProfilingPointAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        setEnabled(false);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowOppositeProfilingPointAction.class);
    }

    @Override
    public String getName() {
        return Bundle.ShowOppositeProfilingPointAction_NoDataString();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (!ProfilingPointsManager.getDefault().isProfilingSessionInProgress()) {
            Collection<? extends CodeProfilingPoint.Annotation> anns = actionContext.lookupAll(CodeProfilingPoint.Annotation.class);
            final InvocationLocationDescriptor desc = getCurrentLocationDescriptor(anns);
            if (desc != null) {
                String name = desc.isStartLocation() ? 
                    Bundle.ShowOppositeProfilingPointAction_EndActionName() : 
                        Bundle.ShowOppositeProfilingPointAction_StartActionName();

                return new AbstractAction(name) {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        CodeProfilingPoint.Location oppositeLocation = desc.getOppositeLocation();

                        if (oppositeLocation != null) {
                            Utils.openLocation(oppositeLocation);
                        } else {
                            ProfilerDialogs.displayWarning(
                                    Bundle.ShowOppositeProfilingPointAction_NoEndDefinedMsg());
                        }
                    }
                };
            }
        }
        return this;
    }

    private InvocationLocationDescriptor getCurrentLocationDescriptor(Collection<? extends CodeProfilingPoint.Annotation> anns) {
        if (anns.size() == 1) {
            CodeProfilingPoint pp = anns.iterator().next().profilingPoint();
            CodeProfilingPoint.Location currentLocation = Utils.getCurrentLocation(0);

            if (pp instanceof CodeProfilingPoint.Paired) {
                return new InvocationLocationDescriptor((CodeProfilingPoint.Paired)pp, currentLocation);
            }
        }
        return null;
    }
}
