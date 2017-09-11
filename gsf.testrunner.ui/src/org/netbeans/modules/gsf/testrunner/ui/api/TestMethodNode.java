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

package org.netbeans.modules.gsf.testrunner.ui.api;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.CharConversionException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.TestMethodNodeChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**Node representing a test method
 *
 * @author Marian Petras, Erno Mononen
 */
public class TestMethodNode extends AbstractNode {

    /**
     * Specifies whether the failure message should be inlined in the test method node.
     * See #149315.
     */
    private static final boolean INLINE_RESULTS =
            Boolean.valueOf(System.getProperty("gsf.testrunner.inline_result", "true")); // NOI18N

    /** */
    protected final Testcase testcase;
    private WeakReference<Project> project;
    private final URI projectURI;

    /**
     * Creates a new instance of TestcaseNode
     */
    public TestMethodNode(final Testcase testcase, Project project) {
        this(testcase, project, null);
    }

    protected TestMethodNode(final Testcase testcase, Project project, Lookup lookup) {
        super(testcase.getTrouble() != null
              ? new TestMethodNodeChildren(testcase)
              : Children.LEAF, lookup);

        this.testcase = testcase;
        this.project = new WeakReference<Project>(project);
        this.projectURI = project.getProjectDirectory().toURI();

        setDisplayName();

        if (TestsuiteNode.DISPLAY_TOOLTIPS) {
            setShortDescription(TestsuiteNode.toTooltipText(testcase.getOutput()));
        }

    }

    /**
     *
     * @return the {@link Testcase} this method belongs to
     */
    public Testcase getTestcase() {
        return testcase;
    }

    @NbBundle.Messages({
        "# {0} - name of the test method", "MSG_TestMethodFailed={0} - Failed",
        "# {0} - name of the test method", "MSG_TestMethodPending={0} - Pending",
        "# {0} - name of the test method", "MSG_TestMethodError={0} - caused an ERROR",
        "# {0} - name of the test method", "# {1} - elapsed time in seconds", "MSG_TestMethodPassed_time={0}  ({1,number,0.0##} s)",
        "# {0} - name of the test method", "# {1} - elapsed time in seconds", "MSG_TestMethodFailed_time={0} - FAILED  ({1,number,0.0##} s)",
        "# {0} - name of the test method", "# {1} - elapsed time in seconds", "MSG_TestMethodPending_time={0} - PENDING ({1,number,0.0##} s)",
        "# {0} - name of the test method", "# {1} - elapsed time in seconds", "MSG_TestMethodError_time={0} - caused an ERROR  ({1,number,0.0##} s)"})
    private void setDisplayName() {
        final int status = (testcase.getTrouble() == null)
                           ? 0
                           : testcase.getTrouble().isError() ? 1 : 2;
        
        if ((status == 0) && (testcase.getTimeMillis() < 0)) {
            setDisplayName(testcase.getName());
            return;
        }
        
        if(testcase.getTimeMillis() < 0) {
            if(status == 1) {
                setDisplayName(Bundle.MSG_TestMethodError(testcase.getName()));
            } else if(status == 2) {
                setDisplayName(Bundle.MSG_TestMethodFailed(testcase.getName()));
            }
        } else {
            if(status == 0) {
                setDisplayName(Bundle.MSG_TestMethodPassed_time(testcase.getName(), testcase.getTimeMillis() / 1000f));
            } else if(status == 1) {
                setDisplayName(Bundle.MSG_TestMethodError_time(testcase.getName(), testcase.getTimeMillis() / 1000f));
            } else if(status == 2) {
                setDisplayName(Bundle.MSG_TestMethodFailed_time(testcase.getName(), testcase.getTimeMillis() / 1000f));
            }
        }
    }

    protected Project getProject() {
        Project prj = project.get();
        if (prj == null) {
            prj = FileOwnerQuery.getOwner(projectURI);
            project = new WeakReference<Project>(prj);
        }
        return prj;
    }
    
    @Override
    public String getHtmlDisplayName() {
        Status status = testcase.getStatus();

        StringBuffer buf = new StringBuffer(60);
        buf.append(testcase.getName());
        buf.append("&nbsp;&nbsp;");                                     //NOI18N
        buf.append("<font color='#");                                   //NOI18N
        buf.append(status.getHtmlDisplayColor() + "'>"); //NOI18N

        String cause = null;
        if (INLINE_RESULTS && testcase.getTrouble() != null && testcase.getTrouble().getStackTrace() != null &&
                testcase.getTrouble().getStackTrace().length > 0) {
            try {
                cause = XMLUtil.toElementContent(testcase.getTrouble().getStackTrace()[0]).replace("\n", "&nbsp;"); // NOI18N
            } catch (CharConversionException ex) {
                // We're messing with user testoutput - always risky. Don't complain
                // here, simply fall back to the old behavior of the test runner -
                // don't include the message
                cause = null;
            }
        }

        if (cause != null) {
            cause = TestsuiteNode.cutLine(cause, 
                                          TestsuiteNode.MAX_MSG_LINE_LENGTH,
                                          true); // Issue #172772
            buf.append(DisplayNameMapper.getCauseHTML(status, cause));
        } else {
            buf.append(testcase.getTimeMillis() < 0 ? DisplayNameMapper.getNoTimeHTML(status)
                    : DisplayNameMapper.getTimeHTML(status, testcase.getTimeMillis() / 1000f));
        }

        buf.append("</font>");                                          //NOI18N
        return buf.toString();
    }
    
    /**
     * Gets preferred action.
     * @return preferred action which defaults to {@code null}
     */
    @Override
    public Action getPreferredAction() {
        return null;
    }
    
    @Override
    @NbBundle.Messages("LBL_CopyStackTrace=&Copy Stack Trace")
    public Action[] getActions(boolean context) {
	List<Action> actions = new ArrayList<Action>();
	if ((testcase.getTrouble() != null) && (testcase.getTrouble().getComparisonFailure() != null)){
            actions.add(new DiffViewAction(testcase));
        }
	if (testcase.getTrouble() != null && testcase.getTrouble().getStackTrace() != null) {
	    StringBuilder callStack = new StringBuilder();
	    for(String stack : testcase.getTrouble().getStackTrace()) {
		if(stack != null) {
		    callStack.append(stack.concat("\n"));
		}
	    }
	    if (callStack.length() > 0) {
		final String trace = callStack.toString();
		actions.add(new AbstractAction(Bundle.LBL_CopyStackTrace()) {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(trace), null);
		    }
		});
	    }
	}
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    public Image getIcon(int type) {
        switch (testcase.getStatus()) {
        case PASSED:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/ok_16.png"); //NOI18N
        case PASSEDWITHERRORS:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/ok_withErrors_16.png"); //NOI18N
        case FAILED:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/warning_16.png"); //NOI18N
        case ERROR:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/error_16.png"); //NOI18N
	case ABORTED:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/aborted.png"); //NOI18N
        case SKIPPED:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/skipped_16.png"); //NOI18N
        default:
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/warning2_16.png"); //NOI18N
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     *
     * @return {@code true} if the test method is error or failed, {@code false} otherwise
     */
    public boolean failed() {
        return testcase.getStatus().equals(Status.FAILED)
                || testcase.getStatus().equals(Status.ERROR);
    }

    
    private static final class DisplayNameMapper {
        
        private final static Logger LOG = Logger.getLogger(TestMethodNode.class.getName());

        @NbBundle.Messages({
            "MSG_TestMethodPassed_HTML_cause=passed",
            "MSG_TestMethodPassedWithErrors_HTML_cause=passed with errors",
            "# {0} - the cause the test method did not pass", "MSG_TestMethodFailed_HTML_cause=Failed: {0}",
            "# {0} - the cause the test method did not pass", "MSG_TestMethodPending_HTML_cause=Pending: {0}",
            "# {0} - the cause the test method did not pass", "MSG_TestMethodSkipped_HTML_cause=Skipped: {0}",
            "# {0} - the cause the test method did not pass", "MSG_TestMethodError_HTML_cause=caused an ERROR: {0}",
            "# {0} - the cause the test method did not pass", "MSG_TestMethodAborted_HTML_cause=Aborted: {0}",
            "# {0} - the cause the test method did not pass", "MSG_TestMethodIgnored_HTML_cause=Ignored: {0}"})
        static String getCauseHTML(Status status, String cause) {
            if(status == Status.PASSED) {
                return Bundle.MSG_TestMethodPassed_HTML_cause();
            } else if(status == Status.PASSEDWITHERRORS) {
                return Bundle.MSG_TestMethodPassedWithErrors_HTML_cause();
            } else if(status == Status.ERROR) {
                return Bundle.MSG_TestMethodError_HTML_cause(cause);
            } else if(status == Status.FAILED) {
                return Bundle.MSG_TestMethodFailed_HTML_cause(cause);
            } else if(status == Status.PENDING) {
                return Bundle.MSG_TestMethodPending_HTML_cause(cause);
            } else if(status == Status.SKIPPED) {
                return Bundle.MSG_TestMethodSkipped_HTML_cause(cause);
            } else if(status == Status.ABORTED) {
                return Bundle.MSG_TestMethodAborted_HTML_cause(cause);
            } else if(status == Status.IGNORED) {
                return Bundle.MSG_TestMethodIgnored_HTML_cause(cause);
            }
            LOG.log(Level.INFO, "Unknown status: {0}", status);
            return "";
        }
        
        @NbBundle.Messages({
            "MSG_TestMethodPassed_HTML=passed",
            "MSG_TestMethodPassedWithErrors_HTML=passed with errors",
            "MSG_TestMethodFailed_HTML=Failed",
            "MSG_TestMethodPending_HTML=Pending",
            "MSG_TestMethodSkipped_HTML=Skipped",
            "MSG_TestMethodError_HTML=caused an ERROR",
            "MSG_TestMethodAborted_HTML=Aborted",
            "MSG_TestMethodIgnored_HTML=Ignored"})
        static String getNoTimeHTML(Status status) {
            if(status == Status.PASSED) {
                return Bundle.MSG_TestMethodPassed_HTML();
            } else if(status == Status.PASSEDWITHERRORS) {
                return Bundle.MSG_TestMethodPassedWithErrors_HTML();
            } else if(status == Status.ERROR) {
                return Bundle.MSG_TestMethodError_HTML();
            } else if(status == Status.FAILED) {
                return Bundle.MSG_TestMethodFailed_HTML();
            } else if(status == Status.PENDING) {
                return Bundle.MSG_TestMethodPending_HTML();
            } else if(status == Status.SKIPPED) {
                return Bundle.MSG_TestMethodSkipped_HTML();
            } else if(status == Status.ABORTED) {
                return Bundle.MSG_TestMethodAborted_HTML();
            } else if(status == Status.IGNORED) {
                return Bundle.MSG_TestMethodIgnored_HTML();
            }
            LOG.log(Level.INFO, "Unknown status: {0}", status);
            return "";
        }

        @NbBundle.Messages({
            "# {0} - elapsed time in seconds", "MSG_TestMethodPassed_HTML_time=passed  ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodPassedWithErrors_HTML_time=passed with errors  ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodFailed_HTML_time=FAILED  ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodPending_HTML_time=PENDING  ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodSkipped_HTML_time=SKIPPED ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodError_HTML_time=caused an ERROR  ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodAborted_HTML_time=Aborted  ({0,number,0.0##} s)",
            "# {0} - elapsed time in seconds", "MSG_TestMethodIgnored_HTML_time=Ignored  ({0,number,0.0##} s)"})
        static String getTimeHTML(Status status, float time) {
            if(status == Status.PASSED) {
                return Bundle.MSG_TestMethodPassed_HTML_time(time);
            } else if(status == Status.PASSEDWITHERRORS) {
                return Bundle.MSG_TestMethodPassedWithErrors_HTML_time(time);
            } else if(status == Status.ERROR) {
                return Bundle.MSG_TestMethodError_HTML_time(time);
            } else if(status == Status.FAILED) {
                return Bundle.MSG_TestMethodFailed_HTML_time(time);
            } else if(status == Status.PENDING) {
                return Bundle.MSG_TestMethodPending_HTML_time(time);
            } else if(status == Status.SKIPPED) {
                return Bundle.MSG_TestMethodSkipped_HTML_time(time);
            } else if(status == Status.ABORTED) {
                return Bundle.MSG_TestMethodAborted_HTML_time(time);
            } else if(status == Status.IGNORED) {
                return Bundle.MSG_TestMethodIgnored_HTML_time(time);
            }
            LOG.log(Level.INFO, "Unknown status: {0}", status);
            return "";
        }
    }
}
