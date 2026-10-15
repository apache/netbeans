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

package org.netbeans.modules.maven.event;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author mkleint
 */
@Named("ide")
@Singleton
public class NbEventSpy implements EventSpy {

    @Inject
    private Logger logger;

    @Inject
    private PlexusContainer container;
    
    //#236768 guard against the mojos executing new mvn build in-JVM
    //needs to be static as the wrapped build will likely get a new instance
    //it's unlikely that we would legally trigger multiple builds inside a single jvm sequentially
    private static final AtomicBoolean insideSession = new AtomicBoolean(false);
    private static final AtomicBoolean ignoreInnerSessionEvents = new AtomicBoolean(false);

    @Override
    public void init(Context context) throws Exception {
        //as as by MavenCLI.java
        //data.put( "plexus", container );
        //data.put( "workingDirectory", cliRequest.workingDirectory );
        //data.put( "systemProperties", cliRequest.systemProperties );
        //data.put( "userProperties", cliRequest.userProperties );
        //data.put( "versionProperties", CLIReportingUtils.getBuildProperties() );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(Object event) throws Exception {
        //event can be:
        //org.apache.maven.execution.ExecutionEvent
        //org.sonatype.aether.RepositoryEvent
        //org.apache.maven.project.DefaultDependencyResolutionRequest
        //org.apache.maven.project.DependencyResolutionResult
        //org.apache.maven.execution.MavenExecutionRequest
        //org.apache.maven.execution.MavenExecutionResult
        //org.apache.maven.settings.building.SettingsBuildingRequest
        //org.apache.maven.settings.building.SettingsBuildingResult
        if (event instanceof ExecutionEvent) {
            ExecutionEvent ex = (ExecutionEvent) event;
            if (ignoreInnerSessionEvents.get()) { //#236768 guard against the mojos executing new mvn build in-JVM
                if (ExecutionEvent.Type.SessionEnded.equals(ex.getType())) {
                    ignoreInnerSessionEvents.set(false);
                }
                return;
            }
            JSONObject root = new JSONObject();
            
            //use base64 for complex structures or unknown values?
            {
                root.put("type", ex.getType().name());
                //the depth is as follows
                //Session -> Project -> [Fork -> ForkedProject] -> Mojo                
                
                if (ex.getProject() != null && 
                        (ExecutionEvent.Type.ProjectStarted.equals(ex.getType()) ||
                         ExecutionEvent.Type.ProjectFailed.equals(ex.getType()) ||
                         ExecutionEvent.Type.ProjectSkipped.equals(ex.getType()) ||
                         ExecutionEvent.Type.ProjectSucceeded.equals(ex.getType())
                        )) { // && not superpom
                    //only in Project* related event types
                    //project skipped called without ProjectStarted
                    MavenProject mp = ex.getProject();
                    JSONObject prj = new JSONObject();
                    prj.put("id", mp.getGroupId() + ":" + mp.getArtifactId()+ ":" + mp.getVersion());
                    if (mp.getFile() != null) { //file is null in superpom
                        prj.put("file", mp.getFile().getParentFile().getAbsolutePath());
                    }
                    root.put("prj", prj);
                }
                if (ExecutionEvent.Type.SessionStarted.equals(ex.getType()) || ExecutionEvent.Type.SessionEnded.equals(ex.getType())) {
                    //only in session events
                    root.put("prjcount", ex.getSession().getProjects().size());
                    if (ExecutionEvent.Type.SessionStarted.equals(ex.getType())) {
                        if (!insideSession.compareAndSet(false, true)) { //#236768 guard against the mojos executing new mvn build in-JVM
                            //
                            ignoreInnerSessionEvents.set(true);
                            return;
                        }
                        if (container != null && container.getContainerRealm() != null) {
                            ClassRealm cr = container.getContainerRealm();
                            JSONArray array = new JSONArray();
                            do {
                                URL[] urls = cr.getURLs();
                                for (URL url : urls) {
                                    array.add(url.toExternalForm());
                                }
                            } while ((cr = cr.getParentRealm()) != null);
                            root.put("mvncoreurls", array);
                        }
                    }
                    if (ExecutionEvent.Type.SessionEnded.equals(ex.getType())) {
                        insideSession.compareAndSet(true, false);//#236768 guard against the mojos executing new mvn build in-JVM
                    }
                }
                if (ex.getMojoExecution() != null && 
                        (ExecutionEvent.Type.MojoStarted.equals(ex.getType()) ||
                         ExecutionEvent.Type.MojoFailed.equals(ex.getType()) ||
                         ExecutionEvent.Type.MojoSkipped.equals(ex.getType()) || 
                         ExecutionEvent.Type.MojoSucceeded.equals(ex.getType())
                        )) {
                    //only in mojo events
                    //MojoSkipped .. only if requires online but build was offline, called without MojoStarted
                    MojoExecution me = ex.getMojoExecution();
                    JSONObject mojo = new JSONObject();
                    mojo.put("id",  me.getGroupId() + ":" + me.getArtifactId()+ ":" + me.getVersion());
                    if (me.getGoal() != null) {
                        mojo.put("goal", me.getGoal());
                    }
                    if (me.getSource() != null) {
                        mojo.put("source", me.getSource().name());
                    }
                    if (me.getExecutionId() != null) {
                        mojo.put("execId", me.getExecutionId());
                    }
                    if (me.getLifecyclePhase() != null) {
                        mojo.put("phase", me.getLifecyclePhase());
                    }
                    PluginExecution exec = me.getPlugin().getExecutionsAsMap().get(me.getExecutionId());
                    if (exec != null) {
                        InputLocation execLoc = exec.getLocation(""); //apparently getLocation("id" never returns a thing)
                        if (execLoc != null) {
                            JSONObject loc = new JSONObject();
                            loc.put("ln", execLoc.getLineNumber());
                            loc.put("col", execLoc.getColumnNumber());
                            String locS = execLoc.getSource().getLocation();
                            if (locS != null) {
                                //is path
                                loc.put("loc", locS);
                            }
                            String mid = execLoc.getSource().getModelId();
                            if (mid != null) {
                                loc.put("id", mid);
                            }
                            mojo.put("loc", loc);
                        }
                    }
                    //used to go to sources + debug build actions
                    MojoDescriptor md = me.getMojoDescriptor();
                    if (md != null) {
                        mojo.put("impl", md.getImplementation());
                        PluginDescriptor pd = md.getPluginDescriptor();
                        if (pd != null) {
                            ClassRealm cr = pd.getClassRealm();
                            if (cr != null) {
                                URL[] urls = cr.getURLs();
                                JSONArray array = new JSONArray();
                                for (URL url : urls) {
                                    array.add(url.toExternalForm());
                                }
                                mojo.put("urls", array);
                            }
                        }
                    }
                    MavenProject mp = ex.getProject();
                    if (mp != null) {
                        if (mp.getFile() != null) { //file is null in superpom
                            mojo.put("prjFile", mp.getFile().getParentFile().getAbsolutePath());
                        }
                    }
                    root.put("mojo", mojo);    
                }
                if (ExecutionEvent.Type.MojoFailed.equals(ex.getType()) && ex.getException() != null) {
                    Exception exc = ex.getException();
                    if (exc instanceof LifecycleExecutionException) {
                        JSONObject excep = new JSONObject();
                        //all mojo failed events in current codebase are lifecycle execs.
                        String message = exc.getCause().getMessage();
                        byte[] enc = Base64.encodeBase64(message.getBytes(StandardCharsets.UTF_8)); //NOW are these conversions correct?
                        String encString = new String(enc, StandardCharsets.UTF_8);
                        excep.put("msg", encString);
                        root.put("exc", excep);
                    }
                    
                }
            }    
            logger.info("NETBEANS-ExecEvent:"  + root.toString());
        }
//        if (event instanceof RepositoryEvent) {
//            RepositoryEvent re = (RepositoryEvent) event;
//            logger.info("NETBEANS-RE:" + re.getType() + ":" + re.getFile());
//        }
    }

    @Override
    public void close() throws Exception {}

}
