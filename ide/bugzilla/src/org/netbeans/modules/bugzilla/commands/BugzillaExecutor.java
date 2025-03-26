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

package org.netbeans.modules.bugzilla.commands;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaStatus;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaUserMatchResponse;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.autoupdate.BugzillaAutoupdate;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Executes commands against one bugzilla Repository and handles errors
 *
 * @author Tomas Stupka
 */
public class BugzillaExecutor {

    private static final String HTTP_ERROR_NOT_FOUND         = "http error: not found";         // NOI18N
    private static final String EMPTY_PASSWORD               = "Empty password not allowed to login"; // NOI18N
    private static final String USERNAME_CONFIRM_MATCH       = "Confirm Match"; // NOI18N
    private static final String INVALID_USERNAME_OR_PASSWORD = "invalid username or password";  // NOI18N
    private static final String REPOSITORY_LOGIN_FAILURE     = "unable to login to";            // NOI18N
    private static final String COULD_NOT_BE_FOUND           = "could not be found";            // NOI18N
    private static final String REPOSITORY                   = "repository";                    // NOI18N
    private static final String MIDAIR_COLLISION             = "mid-air collision occurred while submitting to"; // NOI18N

    private final BugzillaRepository repository;

    private static final Map<String, Callable<Boolean>> handlerCalls = new HashMap<>();
    
    public BugzillaExecutor(BugzillaRepository repository) {
        this.repository = repository;
    }

    public void execute(BugtrackingCommand cmd) {
        execute(cmd, true);
    }

    public void execute(BugtrackingCommand cmd, boolean handleExceptions) {
        execute(cmd, handleExceptions, true);
    }

    public void execute(BugtrackingCommand cmd, boolean handleExceptions, boolean checkVersion) {
        execute(cmd, handleExceptions ? 0 : -1, checkVersion, true, true);
    }

    public void execute(BugtrackingCommand cmd, boolean handleExceptions, boolean checkVersion, boolean ensureCredentials) {
        execute(cmd, handleExceptions ? 0 : -1, checkVersion, ensureCredentials, true);
    }
    
    private void execute(BugtrackingCommand cmd, int handleCounter, boolean checkVersion, boolean ensureCredentials, boolean reexecute) {
        boolean handleExceptions = handleCounter++ > -1 && handleCounter < 10;
        
        try {
            
            cmd.setFailed(true);

            if(checkVersion) {
                checkAutoupdate();
            }

            if(ensureCredentials) {
                if( ( cmd instanceof AddAttachmentCommand ||
                      cmd instanceof SubmitCommand ) 
                    && !checkAndEnsureNBCredentials()) 
                {
                    return;
                }
                repository.ensureCredentials();
            }
            
            Bugzilla.LOG.log(Level.FINE, "execute {0}", cmd); // NOI18N
            cmd.execute();

            if(cmd instanceof SynchronizeQueryCommand) {
                SynchronizeQueryCommand pqc = (SynchronizeQueryCommand) cmd;
                if(handleStatus(pqc, handleExceptions)) {
                    return;
                }
            }

            cmd.setFailed(false);
            cmd.setErrorMessage(null);
            
            synchronized ( handlerCalls ) {
                handlerCalls.remove(repository.getUrl());
            }
            
        } catch (CoreException ce) {
            Bugzilla.LOG.log(Level.FINE, null, ce);

            ExceptionHandler handler;
            if(cmd instanceof ValidateCommand) {
                handler = ExceptionHandler.createHandler(ce, this, null, ((ValidateCommand)cmd), reexecute);
            } else {
                handler = ExceptionHandler.createHandler(ce, this, repository, null, reexecute);
            }
            assert handler != null;

            String msg = handler.getMessage();

            cmd.setFailed(true);
            cmd.setErrorMessage(msg);

            if(handleExceptions) {
                if(handler.handle()) {
                    // execute again
                    execute(cmd, handleCounter, checkVersion, ensureCredentials, !handler.reexecuteOnce());
                }
            }
            return;

        } catch(MalformedURLException me) {
            cmd.setErrorMessage(me.getMessage());
            Bugzilla.LOG.log(Level.SEVERE, null, me);
        } catch(IOException ioe) {
            cmd.setErrorMessage(ioe.getMessage());

            if(!handleExceptions) {
                Bugzilla.LOG.log(Level.FINE, null, ioe);
                return;
            }

            handleIOException(ioe);
        } catch(RuntimeException re) {
            Throwable t = re.getCause();
            if(t instanceof InterruptedException || !handleExceptions) {
                Bugzilla.LOG.log(Level.FINE, null, t);
            } else {
                Bugzilla.LOG.log(Level.SEVERE, null, re);
            }
        }
    }

    /**
     * Returnes true if the given commands status != ok
     * @param cmd
     * @param handleExceptions
     * @return
     * @throws CoreException
     */
    private boolean handleStatus(SynchronizeQueryCommand cmd, boolean handleExceptions) throws CoreException {
        IStatus status = cmd.getStatus();
        if(status == null || status.isOK()) {
            return false;
        }
        Bugzilla.LOG.log(Level.FINE, "command {0} returned status : {1}", new Object[] {cmd, status.getMessage()}); // NOI18N

        if (status.getException() instanceof CoreException) {
            throw (CoreException) status.getException();
        }

        boolean isHtml = false;
        String errMsg = null;
        if(status instanceof RepositoryStatus) {
            RepositoryStatus rstatus = (RepositoryStatus) status;
            errMsg = rstatus.getHtmlMessage();
            isHtml = errMsg != null;
        }
        if(errMsg == null) {
            errMsg = status.getMessage();
        }
        cmd.setErrorMessage(errMsg);
        cmd.setFailed(true);

        if(!handleExceptions) {
            return true;
        }

        BugzillaConfiguration conf = repository.getConfiguration();
        if(conf.isValid()) {
            BugzillaVersion version = conf.getInstalledVersion();
            if(version.compareMajorMinorOnly(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION) > 0) {
                notifyErrorMessage(
                        NbBundle.getMessage(BugzillaExecutor.class, "MSG_BUGZILLA_ERROR_WARNING", status.getMessage()) + "\n\n" + 
                        NbBundle.getMessage(BugzillaExecutor.class, "MSG_BUGZILLA_VERSION_WARNING1", version) + "\n" +          // NOI18N
                        (true ? NbBundle.getMessage(BugzillaExecutor.class, "MSG_BUGZILLA_VERSION_WARNING2") : ""));        // NOI18N
                return true;
            }
        }
        if(isHtml) {
            notifyHtmlMessage(errMsg, repository, true);
        } else {
            notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_BUGZILLA_ERROR_WARNING", errMsg)); // NOI18N
        }
        return true;
    }

    static void notifyErrorMessage(String msg) {
        if("true".equals(System.getProperty("netbeans.t9y.throwOnClientError", "false"))) { // NOI18N
            Bugzilla.LOG.info(msg);
            throw new AssertionError(msg);
        }
        NotifyDescriptor nd =
                new NotifyDescriptor(
                    msg,
                    NbBundle.getMessage(BugzillaExecutor.class, "LBLError"),    // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }

    private static boolean notifyHtmlMessage(String html, BugzillaRepository repository, boolean htmlTextIsAllYouGot) throws MissingResourceException {
        if (html != null && !html.trim().equals("")) {                          // NOI18N
            html = parseHtmlMessage(html, htmlTextIsAllYouGot);
            if(html == null) {
                return false;
            }
            final HtmlPanel p = new HtmlPanel();
            String label = NbBundle.getMessage(
                                BugzillaExecutor.class,
                                "MSG_ServerResponse",                           // NOI18N
                                new Object[] { repository.getDisplayName() }
                           );
            p.setHtml(repository.getUrl(), html, label);
            DialogDescriptor dialogDescriptor =
                    new DialogDescriptor(
                            p,
                            NbBundle.getMessage(BugzillaExecutor.class, "CTL_ServerResponse"), // NOI18N
                            true,
                            new Object[] { NotifyDescriptor.CANCEL_OPTION },
                            NotifyDescriptor.CANCEL_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN,
                            new HelpCtx(p.getClass()),
                            null
                    );
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            return true;
        }
        return false;
    }

    @SuppressWarnings("empty-statement")
    private static String parseHtmlMessage(String html, boolean htmlTextIsAllYouGot) {
        int idxS = html.indexOf("<div id=\"bugzilla-body\">");              // NOI18N
        if(idxS < 0) {
            return html;
        }
        int idx = idxS;
        int idxE = html.indexOf("</div>", idx);                             // NOI18N
        if(!htmlTextIsAllYouGot && idxE < 0) {
            // there is no closing </div> tag and we don't have to relly on the html text 
            // as on the only msg we got, so skip parsing
            return null;
        }
        
        int levels = 1;
        while(true) {
            idx = html.indexOf("<div", idx + 1);                            // NOI18N
            if(idx < 0 || idx > idxE) {
                break;
            }
            levels++;
        }
        idxE = idxS;
        for (int i = 0; i < levels; i++) {
            idxE = html.indexOf("</div>", idxE + 1);                        // NOI18N
        }
        idxE = idxE > 6 ? idxE + 6 : html.length();
        html = html.substring(idxS, idxE);

        // very nice
        html = html.replace("Please press <b>Back</b> and try again.", ""); // NOI18N

        return html;
    }

    public boolean handleIOException(IOException io) {
        Bugzilla.LOG.log(Level.SEVERE, null, io);
        return true;
    }

    @NbBundle.Messages({"MSG_MissingUsername=Missing username."})
    private boolean checkAndEnsureNBCredentials() {
        if( NBBugzillaUtils.isNbRepository(repository.getUrl()) && 
            (repository.getUsername() == null || repository.getUsername().trim().equals("")) ) 
        {
            boolean ret = repository.authenticate(Bundle.MSG_MissingUsername());
            if(!ret) {
                notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
            }
            return ret;
        }
        return true;
    }

    private abstract static class ExceptionHandler {

        protected String errroMsg;
        protected CoreException ce;
        protected BugzillaExecutor executor;
        protected BugzillaRepository repository;

        protected ExceptionHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
            this.errroMsg = msg;
            this.ce = ce;
            this.executor = executor;
            this.repository = repository;
        }

        static ExceptionHandler createHandler(CoreException ce, BugzillaExecutor executor, BugzillaRepository repository, ValidateCommand validateCommand, boolean forRexecute) {
            String errormsg = getLoginError(repository, validateCommand, ce);
            if(errormsg != null) {
                return new LoginHandler(ce, errormsg, executor, repository);
            }
            errormsg = getNotFoundError(ce);
            if(errormsg != null) {
                return new NotFoundHandler(ce, errormsg, executor, repository);
            }
            errormsg = getMidAirColisionError(ce);
            if(errormsg != null) {
                if(forRexecute) { 
                    return new MidAirHandler(ce, errormsg, executor, repository);
                } else {
                    errormsg = MessageFormat.format(errormsg, repository.getDisplayName());
                    return new DefaultHandler(ce, errormsg, executor, repository);
                }
            }
            return new DefaultHandler(ce, null, executor, repository);
        }

        abstract boolean handle();

        boolean reexecuteOnce() {
            return false;
        }
        
        private static String getLoginError(BugzillaRepository repository, ValidateCommand validateCommand, CoreException ce) {
            String msg = getMessage(ce);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(INVALID_USERNAME_OR_PASSWORD.equals(msg) ||
                   msg.contains(INVALID_USERNAME_OR_PASSWORD) ||
                   msg.contains(EMPTY_PASSWORD) || 
                   msg.contains(USERNAME_CONFIRM_MATCH))
                {
                    Bugzilla.LOG.log(Level.FINER, "returned error message [{0}]", msg);                     // NOI18N
                    String url;
                    if(validateCommand != null) {
                        url = validateCommand.getUrl();
                    } else {
                        url = repository.getUrl();
                    }
                    if(url != null && NBBugzillaUtils.isNbRepository(url)) {
                        String user;
                        if(validateCommand != null) {
                            user = validateCommand.getUser();
                        } else {
                            user = repository.getUsername();
                        }
                        if(user != null && user.contains("@")) {
                            return NbBundle.getMessage(BugzillaExecutor.class, "MSG_INVALID_USERNAME_OR_PASSWORD") + // NOI18N
                                   " " + // NOI18N
                                   NbBundle.getMessage(BugzillaExecutor.class, "MSG_INVALID_USERNAME_OR_PASSWORD_NO_MAIL"); // NOI18N
                        }
                    }
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_INVALID_USERNAME_OR_PASSWORD"); // NOI18N
                } else if(msg.startsWith(REPOSITORY_LOGIN_FAILURE) ||
                         (msg.startsWith(REPOSITORY) && msg.endsWith(COULD_NOT_BE_FOUND)))
                {
                    Bugzilla.LOG.log(Level.FINER, "returned error message [{0}]", msg);                     // NOI18N
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_UNABLE_LOGIN_TO_REPOSITORY");   // NOI18N
                }
            }
            return null;
        }

        private static String getMidAirColisionError(CoreException ce) {
            String msg = getMessage(ce);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(msg.startsWith(MIDAIR_COLLISION)) {
                    Bugzilla.LOG.log(Level.FINER, "returned error message [{0}]", msg);                     // NOI18N
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_MID-AIR_COLLISION");            // NOI18N
                }
            }
            return null;
        }

        private static String getNotFoundError(CoreException ce) {
            IStatus status = ce.getStatus();
            Throwable t = status.getException();
            if(t instanceof UnknownHostException ||
               // XXX maybe a different msg ?     
               t instanceof SocketTimeoutException || 
               t instanceof NoRouteToHostException ||
               t instanceof ConnectException) 
            {
                Bugzilla.LOG.log(Level.FINER, null, t);
                return NbBundle.getMessage(BugzillaExecutor.class, "MSG_HOST_NOT_FOUND");                   // NOI18N
            }
            String msg = getMessage(ce);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(HTTP_ERROR_NOT_FOUND.equals(msg)) {
                    Bugzilla.LOG.log(Level.FINER, "returned error message [{0}]", msg);                     // NOI18N
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_HOST_NOT_FOUND");               // NOI18N
                }
            }
            return null;
        }

        static String getMessage(CoreException ce) {
            String msg = ce.getMessage();
            if(msg != null && !msg.trim().equals("")) {                             // NOI18N
                return msg;
            }
            IStatus status = ce.getStatus();
            msg = status != null ? status.getMessage() : null;
            return msg != null ? msg.trim() : null;
        }

        String getMessage() {
            return errroMsg;
        }

        private static void notifyError(CoreException ce, BugzillaRepository repository) {
            String msg = getMessage(ce);
            IStatus status = ce.getStatus();
            if (status instanceof BugzillaStatus) {
                BugzillaStatus bs = (BugzillaStatus) status;
                BugzillaUserMatchResponse res = bs.getUserMatchResponse();
                
                if(res != null) {
                    String assignedMsg = res.getAssignedToMsg();
                    String newCCMsg = res.getNewCCMsg();
                    String qaContactMsg = res.getQaContactMsg();

                    StringBuilder sb = new StringBuilder();
                    if(msg != null) {
                        sb.append(msg);
                    }
                    if(assignedMsg != null) {
                        sb.append('\n');
                        sb.append(assignedMsg);
                    }
                    if (newCCMsg != null) {
                        sb.append('\n');
                        sb.append(newCCMsg);
                    }
                    if (qaContactMsg != null) {
                        sb.append('\n');
                        sb.append(qaContactMsg);
                    }
                    msg = sb.toString();
                }
            }
            
            if (msg == null && status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                if(notifyHtmlMessage(html, repository, msg == null)) return;
            }
            notifyErrorMessage(msg);
        }

        private static class LoginHandler extends ExceptionHandler {
            public LoginHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                boolean ret = repository.authenticate(errroMsg);
                if(!ret) {
                    notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                }
                return ret;
            }
        }
        
        private static class MidAirHandler extends ExceptionHandler {
            public MidAirHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                repository.refreshConfiguration();
                BugzillaConfiguration bc = repository.getConfiguration();
                return bc != null && bc.isValid();
            }
            @Override
            boolean reexecuteOnce() {
                return true;
            }
        }
        
        private static class NotFoundHandler extends ExceptionHandler {
            public NotFoundHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                Callable<Boolean> c;
                synchronized ( handlerCalls ) {
                    final String key = repository.getUrl();
                    c = handlerCalls.get(key);
                    if(c == null) {
                        c = new Callable<Boolean>() {
                            private boolean alreadyCalled = false;
                            @Override
                            public Boolean call() {
                                if(alreadyCalled) {
                                    Bugzilla.LOG.log(Level.INFO, key, ce); 
                                    return false;
                                }
                                // do not handle this kind of erorr until flag turned false by a succesfull command
                                alreadyCalled = true;                                
                                boolean ret = Bugzilla.getInstance().getBugtrackingFactory().editRepository(executor.repository, errroMsg);
                                if(!ret) {
                                    notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                                }
                                return ret;
                            }
                        };
                        handlerCalls.put(key, c);
                    }
                }
                try {
                    return c.call();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                return false;
            }
        }
        private static class DefaultHandler extends ExceptionHandler {
            public DefaultHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                if(errroMsg != null) {
                    notifyErrorMessage(errroMsg);
                } else {
                    notifyError(ce, repository);
                }
                return false;
            }
        }
    }

    private void checkAutoupdate() {
        try {
            BugzillaAutoupdate.getInstance().checkAndNotify(repository);
        } catch (Throwable t) {
            Bugzilla.LOG.log(Level.SEVERE, "Exception in Bugzilla autoupdate check.", t);                   // NOI18N
        }
    }
}

