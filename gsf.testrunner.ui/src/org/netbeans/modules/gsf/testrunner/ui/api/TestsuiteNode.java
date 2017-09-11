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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.UIManager;
import org.netbeans.modules.gsf.testrunner.api.OutputLine;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.TestsuiteNodeChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**Node representing a {@link TestSuite}
 *
 * @author Marian Petras, Erno Mononen
 */
public class TestsuiteNode extends AbstractNode {

    /**
     * The max number of output lines to display in the tooltip.
     */
    static final int MAX_TOOLTIP_LINES = Integer.getInteger("testrunner.max.tooltip.lines", 4); //NOI18N
    /**
     * The max line length to display in the tooltip.
     */
    private static final int MAX_TOOLTIP_LINE_LENGTH = Integer.getInteger("testrunner.max.tooltip.line.length", 80); //NOI18N
    /**
     * The system property for enabling/disabling tooltips.
     */
    static final boolean DISPLAY_TOOLTIPS = Boolean.valueOf(System.getProperty("testrunner.display.tooltips", "true"));//NOI18N
    /**
     * The max line length to display in the messages.
     * 
     * By default, the max line length for the messages in the Test Results
     * window will be set to 120 for the GTK look and feel or JDK8, and won't be limited
     * for other look and feels or JDKs. For any look and feel or JDK a user can change the
     * default setting via the system property
     * {@code testrunner.max.msg.line.length=MAX_LINE_LENGTH},
     * where  {@code MAX_LINE_LENGTH} is a desired value.
     *
     * See Issue #172772
     * See Issue #175430
     * See Issue #188632
     */
    static final int MAX_MSG_LINE_LENGTH = 
          Integer.getInteger("testrunner.max.msg.line.length", //NOI18N
                             isGTK() || isJDK8() ? 120 : Integer.MAX_VALUE);

    protected String suiteName;
    protected TestSuite suite;
    protected Report report;
    protected int filterMask = 0;
    private TestsuiteNodeChildren children;

    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    public TestsuiteNode(final String suiteName, final boolean filtered) {
        this(null, suiteName, filtered, null);
    }
    
    /**
     * Creates a new instance of TestsuiteNode
     */
    public TestsuiteNode(final Report report, final boolean filtered) {
        this(report, null, filtered, null);
    }


    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    protected TestsuiteNode(final Report report,
                          final String suiteName,
                          final boolean filtered, Lookup lookup) {
        super(Children.LEAF, lookup);
        this.report = report; 
        this.suiteName = (report != null) ? report.getSuiteClassName() : (suiteName != null ? suiteName : TestSuite.ANONYMOUS_SUITE);
        
        assert this.suiteName != null;
        
        setDisplayName();
        children = null;
    }

    /**
     *  Update icon, display name and children of this TestsuiteNode
     */
    public void notifyTestSuiteFinished() {
        fireIconChange();
        setDisplayName();
        getTestsuiteNodeChildren().notifyTestSuiteFinished();
    }
    
    private TestsuiteNodeChildren getTestsuiteNodeChildren() {
        if (children == null) {
            children = new TestsuiteNodeChildren(report, filterMask);
            setChildren(Children.create(children, true));
        }
        return children;
    }

    /**
     * @return the given lines appropriately formatted for a tooltip.
     */
    @NbBundle.Messages({
        "MSG_NoOutput=No output.",
        "MSG_MoreOutput=Some lines not displayed."})
    static String toTooltipText(List<OutputLine> lines) {
        StringBuilder result = new StringBuilder();
        result.append("<html>"); //NOI18N
        if (lines.isEmpty()) {
            result.append("<i>" + Bundle.MSG_NoOutput() + "</i>"); //NOI18N
        } else {
            for (int i = 0; i < lines.size(); i++) {
                if (i >= MAX_TOOLTIP_LINES) {
                    result.append("<br><i>" + Bundle.MSG_MoreOutput() + "</i>"); //NOI18N
                    break;
                }
                String line = lines.get(i).getLine();
                line = cutLine(line, MAX_TOOLTIP_LINE_LENGTH, true); // #172772
                result.append(line);
                if (i < lines.size()) {
                    result.append("<br>"); //NOI18N
                }
            }
        }
        result.append("</html>"); //NOI18N
        return result.toString();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public Image getIcon(int type) {
        if (report != null){
            Status status = report.getStatus();
            if (!report.isCompleted()){
                switch (status){
                    case FAILED:
                    case ERROR: return ImageUtilities.mergeImages(
                            ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/run.gif"), //NOI18N
                            ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/error-badge.gif"), //NOI18N
                            8, 8);
                }
            }else{
                switch (status){
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
        }
        return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/run.gif"); //NOI18N
    }
    
    /**
     * @param report the report to display by this node
     */
    public void displayReport(final Report report) {
        assert (report != null);
        assert report.getSuiteClassName().equals(this.suiteName)
               || (this.suiteName == TestSuite.ANONYMOUS_SUITE);
        
        this.report = report;
        suiteName = report.getSuiteClassName();
        
        setDisplayName();
        getTestsuiteNodeChildren().setReport(report);
        if (DISPLAY_TOOLTIPS) {
            setShortDescription(toTooltipText(getOutput()));
        }
        fireIconChange();
    }
    
    /**
     * Returns a report represented by this node.
     *
     * @return  the report, or <code>null</code> if this node represents
     *          a running test suite (no report available yet)
     */
    public Report getReport() {
        return report;
    }

    @NbBundle.Messages({
        "# {0} - name of the test class", "MSG_TestsuiteRunning={0} - running...",
        "MSG_TestsuiteRunningNoname=Test suite running...",
        "# {0} - name of the test class", "MSG_TestsuitePassed={0} - Passed",
        "# {0} - name of the test class", "MSG_TestsuitePassedWithErrors={0} - Passed With Errors",
        "# {0} - name of the test class", "MSG_TestsuiteAborted={0} - Aborted",
        "# {0} - name of the test class", "MSG_TestsuiteFailed={0} - Failed",
        "# {0} - name of the test class", "MSG_TestsuiteSkipped={0} - Skipped"})
    private void setDisplayName() {
        String displayName;
        if (report == null) {
            if (!suiteName.equals(TestSuite.ANONYMOUS_SUITE)) {
                displayName = Bundle.MSG_TestsuiteRunning(suiteName);
            } else {
                displayName = Bundle.MSG_TestsuiteRunningNoname();
            }
        } else if (report.getAborted() > 0){
            displayName = Bundle.MSG_TestsuiteAborted(suiteName);
        } else if (report.getSkipped() > 0){
            displayName = Bundle.MSG_TestsuiteSkipped(suiteName);
        } else if (!report.isCompleted()){
            boolean containsFailed = containsFailed();
            displayName = containsFailed
                          ? Bundle.MSG_TestsuiteFailed(suiteName)
                          : suiteName;
            displayName = Bundle.MSG_TestsuiteRunning(displayName);
        } else {
            boolean containsFailed = containsFailed();
            displayName = containsFailed
                          ? Bundle.MSG_TestsuiteFailed(suiteName)
                          : suiteName;
        }
        setDisplayName(displayName);
    }
    
    @NbBundle.Messages({
        "MSG_TestsuiteNoname=Test suite",
        "MSG_TestsuiteRunning_HTML=running..."})
    @Override
    public String getHtmlDisplayName() {
        
        assert suiteName != null;
        
        StringBuilder buf = new StringBuilder(60);
        if (suiteName != TestSuite.ANONYMOUS_SUITE) {
            buf.append(suiteName);
        } else {
            buf.append(Bundle.MSG_TestsuiteNoname());
        }
        if (report != null) {
            Status status = report.getStatus();
            buf.append("&nbsp;&nbsp;");                                 //NOI18N

            buf.append("<font color='#");                               //NOI18N
            buf.append(status.getHtmlDisplayColor()).append("'>");       //NOI18N
            buf.append(suiteStatusToMsg(status, true));
            buf.append("</font>");                                      //NOI18N
        } 
        if (report == null || !report.isCompleted()){
            buf.append("&nbsp;&nbsp;");                                 //NOI18N
            buf.append(Bundle.MSG_TestsuiteRunning_HTML());
        }
        return buf.toString();
    }
    
    @NbBundle.Messages({
        "# {0} - name of the test class", "MSG_TestsuitePending={0} - Pending",
        "MSG_TestsuitePassed_HTML=passed",
        "MSG_TestsuitePassedWithErrors_HTML=passed with errors",
        "MSG_TestsuiteFailed_HTML=Failed",
        "MSG_TestsuitePending_HTML=Pending",
        "MSG_TestsuiteAborted_HTML=Aborted",
        "MSG_TestsuiteSkipped_HTML=Skipped"})
    static String suiteStatusToMsg(Status status, boolean html) {
        String result = null;
        if(Status.ABORTED == status){
            result = html ? Bundle.MSG_TestsuiteAborted_HTML() : Bundle.MSG_TestsuiteAborted("");
        } else if (Status.ERROR == status || Status.FAILED == status) {
            result = html ? Bundle.MSG_TestsuiteFailed_HTML() : Bundle.MSG_TestsuiteFailed("");
        } else if (Status.PENDING == status) {
            result = html ? Bundle.MSG_TestsuitePending_HTML() : Bundle.MSG_TestsuitePending("");
        } else if (Status.SKIPPED == status) {
            result = html ? Bundle.MSG_TestsuiteSkipped_HTML() : Bundle.MSG_TestsuiteSkipped("");
        } else if (Status.PASSEDWITHERRORS == status) {
            result = html ? Bundle.MSG_TestsuitePassedWithErrors_HTML() : Bundle.MSG_TestsuitePassedWithErrors("");
        } else {
            result = html ? Bundle.MSG_TestsuitePassed_HTML() : Bundle.MSG_TestsuitePassed("");
        }
        return result;
    }

    /**
     *
     * @param suite the {@link TestSuite} to associate with this node
     */
    public void setSuite(TestSuite suite){
        this.suite = suite;
    }

    /**
     *
     * @return the {@link TestSuite} associated with this node
     */
    public TestSuite getSuite(){
        return suite;
    }

    /**
     * @param filterMask the mask to associate with this node
     */
    public void setFilterMask(final int filterMask) {
        if (filterMask == this.filterMask) {
            return;
        }
        this.filterMask = filterMask;
        getTestsuiteNodeChildren().setFilterMask(filterMask);
    }
    
    /**
     */
    private boolean containsFailed() {
        return (report != null) && (report.getFailures() + report.getErrors() != 0);
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
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    private List<OutputLine> getOutput() {
        List<OutputLine> result = new ArrayList<OutputLine>();
        for (Testcase testcase : report.getTests()) {
            result.addAll(testcase.getOutput());
        }
        return result;
    }

    /**
     * Cuts the ending chars in the specified {@code line} if its length is more
     * than the specified value {@code maxLength}.
     *
     * @param line the line
     * @param maxLength maximum of the line length
     * @param isHTML if {@code true} then the HTML tags will be added.
     * @return If length of the {@code line} is more than the {@code maxLength}
     * then concatenation of the string that is limited up to {@code maxLength}
     * chars and a comment string that describes how many chars are omitted,
     * otherwise the {@code line} without any changes. The {@code line} without
     * any changes will be also returned if a comment string that describes how
     * many chars are omitted is longer than an message tail that is intended to
     * be omitted.
     * @throws MissingResourceException if resources for the comment string
     * can't be loaded.
     *
     * @see Issue #172772
     * @see Issue #175430
     */
    @NbBundle.Messages({
        "# {0} - number of charachters in the ending of the tooltip of a test case/suite", "MSG_CharsOmitted=...({0} chars omitted)"})
    public static String cutLine(String line, int maxLength, boolean isHTML)
                                               throws MissingResourceException {
        int length = line.length();
        if (length > maxLength) {
            int tailLength = length - maxLength;
            String cutMsg = Bundle.MSG_CharsOmitted(tailLength);
            if(tailLength > cutMsg.length()) {
                line = line.substring(0, maxLength);
                String startMsg = isHTML ? "<i> " : "";
                String endMsg = isHTML ? "</i>" : "";
                line = line.concat(startMsg + cutMsg + endMsg);
            }
        }
        return line;
    }


    /**
     * Checks whether a currently used look and feel is GTK.
     *
     * @return {@code true} if a currently used look and feel has got the
     * identifier "GTK", otherwise {@code true}.
     */
    private static boolean isGTK() {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }
    
    /**
     * Checks whether the currently used JDK version is 1.8.x_xx
     *
     * @return {@code true} if the currently used JDK version is 8, otherwise {@code false}.
     */
    private static boolean isJDK8() {
        final String javaVersion = System.getProperty("java.version");  //NOI18N
        return javaVersion != null && javaVersion.startsWith("1.8");    //NOI18N
    }

}
