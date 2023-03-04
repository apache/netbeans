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

package org.netbeans.modules.beans.beaninfo;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.concurrent.Future;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.beans.PatternAnalyser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;
import org.openide.util.actions.NodeAction;

/**
* Generate BI action.
*
* @author   Petr Hrebejk
*/
@ActionID(id="org.netbeans.modules.beans.beaninfo.GenerateBeanInfoAction", category="Tools")
@ActionRegistration(lazy=false, displayName="#CTL_GENBI_MenuItem")
@ActionReference(path="Loaders/text/x-java/Actions", position=2120, separatorAfter=2140)
public final class GenerateBeanInfoAction extends NodeAction implements java.awt.event.ActionListener {
    private Dialog biDialog;

    /** generated Serialized Version UID */
    //static final long serialVersionUID = 1391479985940417455L;

    // The dialog for BeanInfo generation

    static final long serialVersionUID =-4937492476805017833L;
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return getString ("CTL_GENBI_MenuItem");
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    @Override
    protected String iconResource () {
        return null;
        //return "/org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable( Node[] activatedNodes ) {
        if (activatedNodes.length != 1) {
            return false;
        } else {
            FileObject fo = findFileObject(activatedNodes[0]);
            return fo != null && JavaSource.forFileObject(fo) != null
                    && !fo.getName().endsWith("BeanInfo"); //NOI18N
        }
    }


    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( final Node[] nodes ) {

        if (nodes.length != 1)
            return;

        // Open the diaog for bean info generation

        final BiPanel biPanel = new BiPanel();

        // Get pattern analyser & bean info and create BiAnalyser & BiNode

        FileObject javaFile = findFileObject(nodes[0]);
        final BeanInfoWorker performer = new BeanInfoWorker(javaFile, biPanel);
        
        class Task implements TaskListener, Runnable {

            public void taskFinished(org.openide.util.Task task) {
                EventQueue.invokeLater(this);
            }

            public void run() {
                if (performer.error != null) {
                    DialogDisplayer.getDefault().notify(performer.error);
                }
                if (performer.bia != null) {
                    performer.bia.openSource();
                }
            }
            
        }

        performer.analyzePatterns().addTaskListener(new Task());

    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void initAccessibility() {
        biDialog.getAccessibleContext().setAccessibleDescription(getString("ACSD_BeanInfoEditorDialog"));
    }    
    
    private static FileObject findFileObject(Node n) {
        DataObject dobj = n.getCookie(DataObject.class);
        return dobj != null? dobj.getPrimaryFile() : null;
    }
    
    static String getString(String key) {
        return NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle").getString(key);
    }
    
    static final class BeanInfoWorker implements Runnable, org.netbeans.api.java.source.Task<CompilationController> {

        private final BiPanel biPanel;
        private final FileObject javaFile;
        private boolean isCancelled = false;
        private Node biNode;
        private BiAnalyser bia;
        private Task task;
        private int state = 0;
        private NotifyDescriptor error;

        public BeanInfoWorker(FileObject javaFile, BiPanel biPanel) {
            this.javaFile = javaFile;
            this.biPanel = biPanel;
        }
        
        public Task analyzePatterns() {
            checkState(0);
            task = RequestProcessor.getDefault().post(this);
            return task;
        }
        
        public void updateUI() {
            waitFinished();
            checkState(1);
            state = 2;
//            fillBiPanel();
            EventQueue.invokeLater(this);
        }
        
        public void generateSources() {
            waitFinished();
            checkState(2);
            state = 3;
//            task.schedule(0);
            try {
                run();
            } finally {
                state = 2;
            }
        }
        
        public boolean isCancelled() {
            return isCancelled;
        }
        
        public void waitFinished() {
            if (task == null) {
                throw new IllegalStateException();
            }
            task.waitFinished();
        }
        
        public boolean isModelModified() {
            waitFinished();
            return bia != null? bia.isModified(): false;
        }
        
        public void run() {
            if (isCancelled) {
                return;
            }
            switch(state) {
                case 0:
                    analyzePatternsImpl();
                    break;
                case 2:
                    fillBiPanel();
                    break;
                case 3:
                    generateSourcesImpl();
                    break;
            }
        }

        public void run(CompilationController javac) throws Exception {
            if (isCancelled) {
                return;
            }
            javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            switch(state) {
                case 1:
                    analyzePatternsImpl(javac);
                    break;
            }
        }
        
        private void analyzePatternsImpl(CompilationInfo javac) {
            checkState(1);
            String clsname = javaFile.getName();
            TypeElement clselm = null;
            for (TypeElement top : javac.getTopLevelElements()) {
                if (clsname.contentEquals(top.getSimpleName())) {
                    clselm = top;
                }
            }
            
            if (clselm == null) {
                isCancelled = true;
                error = new NotifyDescriptor.Message(
                        NbBundle.getMessage(
                                GenerateBeanInfoAction.class,
                                "MSG_FileWitoutTopLevelClass",
                                clsname, FileUtil.getFileDisplayName(javaFile)
                                ),
                        NotifyDescriptor.ERROR_MESSAGE);
                return;
            }
            
            PatternAnalyser pa = new PatternAnalyser(javaFile, null, true);
            pa.analyzeAll(javac, clselm);
            // XXX analyze also superclasses here
            try {
                bia = new BiAnalyser(pa, javac);
            } catch (Exception ex) {
                isCancelled = true;
                Exceptions.printStackTrace(ex);
            }
        }
        
        private void analyzePatternsImpl() {
            if (javaFile == null) {
                isCancelled = true;
                return;
            }
            checkState(0);
            state = 1;
            try {
                JavaSource.forFileObject(javaFile).runUserActionTask(this, true);
            } catch (Exception ex) {
                isCancelled = true;
                Exceptions.printStackTrace(ex);
            }
        }
        
        private void fillBiPanel() {
            biNode = BiNode.createBiNode(bia, error);
            biPanel.setContext( biNode );
            biPanel.expandAll();
        }
        
        private void generateSourcesImpl() {
            if (!isCancelled() && bia != null && !bia.isBeanBroken()) {
                bia.regenerateSource();
            }
        }
        
        private void checkState(int expected) {
            if (state != expected) {
                throw new IllegalStateException();
            }
        }
    }
}
