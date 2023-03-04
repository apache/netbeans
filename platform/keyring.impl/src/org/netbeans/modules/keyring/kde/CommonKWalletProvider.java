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

package org.netbeans.modules.keyring.kde;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.keyring.impl.KeyringSupport;
import org.netbeans.spi.keyring.KeyringProvider;

/**
 *
 * @author psychollek, ynov
 */
class CommonKWalletProvider implements KeyringProvider{

    private static final Logger logger = Logger.getLogger(CommonKWalletProvider.class.getName());
    private static final char[] defaultLocalWallet = "kdewallet".toCharArray();

    private char[] handler = "0".toCharArray();
    private boolean timeoutHappened = false;

    private final char[] appName;
    private final String kwalletVersion;
    private final String pathVersion;


    CommonKWalletProvider(String kwalletVersion, String pathVersion) {
        assert kwalletVersion != null;
        assert pathVersion != null;
        this.kwalletVersion = kwalletVersion;
        this.pathVersion = pathVersion;
        this.appName = KeyringSupport.getAppName().toCharArray();
    }

    @Override
    public boolean enabled(){
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            logger.fine("native keyring integration disabled");
            return false;
        }
        CommandResult result = runCommand("isEnabled");
        if(new String(result.retVal).equals("true")) {
            return updateHandler();
        }
        return false;
    };

    @Override
    public char[] read(String key){
        if (updateHandler()){
            CommandResult result = runCommand("readPassword", handler, appName, key.toCharArray(), appName);
            if (result.exitCode != 0){
                warning("read action returned not 0 exitCode");
            }
            return result.retVal.length > 0 ? result.retVal : null;
        }
        return null;
        //throw new KwalletException("read");
    };

    @Override
    public void save(String key, char[] password, String description){
        //description is forgoten ! kdewallet dosen't have any facility to store
        //it by default and I don't want to do it by adding new fields to kwallet
        if (updateHandler()){
            CommandResult result = runCommand("writePassword", handler , appName
                    , key.toCharArray(), password , appName);
            if (result.exitCode != 0 || (new String(result.retVal)).equals("-1")){
                warning("save action failed");
            }
            return;
        }
        //throw new KwalletException("save");
    };

    @Override
    public void delete(String key){
        if (updateHandler()){
            CommandResult result = runCommand("removeEntry" ,handler,
            appName, key.toCharArray() , appName);
             if (result.exitCode != 0  || (new String(result.retVal)).equals("-1")){
                warning("delete action failed");
            }
            return;
        }
        //throw new KwalletException("delete");
    };

    private boolean updateHandler(){
        if(timeoutHappened) {
            return false;
        }
        handler = new String(handler).equals("")? "0".toCharArray() : handler;
        CommandResult result = runCommand("isOpen",handler);
        if(new String(result.retVal).equals("true")){
            return true;
        }
        char[] localWallet = defaultLocalWallet;
        result = runCommand("localWallet");
        if(result.exitCode == 0) {
            localWallet = result.retVal;
        }

        if(new String(localWallet).contains(".service")) {
            //Temporary workaround for the bug in kdelibs/kdeui/util/kwallet.cpp
            //The bug was fixed http://svn.reviewboard.kde.org/r/5885/diff/
            //but many people currently use buggy kwallet
            return false;
        }
        result = runCommand("open", localWallet , "0".toCharArray(), appName);
        if(result.exitCode == 2) {
            warning("time out happened while accessing KWallet");
            //don't try to open KWallet anymore until bug https://bugs.kde.org/show_bug.cgi?id=259229 is fixed
            timeoutHappened = true;
            return false;
        }
        if(result.exitCode != 0 || new String(result.retVal).equals("-1")) {
            warning("failed to access KWallet");
            return false;
        }
        handler = result.retVal;
        return true;
    }



    private CommandResult runCommand(String command,char[]... commandArgs) {
        String[] argv = new String[commandArgs.length+4];
        argv[0] = "qdbus";
        argv[1] = "org.kde.kwalletd" + kwalletVersion; // NOI18N
        argv[2] = "/modules/kwalletd" + pathVersion; // NOI18N
        argv[3] = "org.kde.KWallet."+command;
        for (int i = 0; i < commandArgs.length; i++) {
            //unfortunatelly I cannot pass char[] to the exec in any way - so this poses a security issue with passwords in String() !
            //TODO: find a way to avoid changing char[] into String
            argv[i+4] = new String(commandArgs[i]);
        }
        Runtime rt = Runtime.getRuntime();
        String retVal = "";
        String errVal = "";
        int exitCode = 0;
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "executing {0}", Arrays.toString(argv));
            }
            Process pr = rt.exec(argv);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line;
            while((line = input.readLine()) != null) {
                if (!retVal.equals("")){
                    retVal = retVal.concat("\n");
                }
                retVal = retVal.concat(line);
            }
            input.close();
            input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            while((line = input.readLine()) != null) {
                if (!errVal.equals("")){
                    errVal = errVal.concat("\n");
                }
                errVal = errVal.concat(line);
            }
            input.close();

            exitCode = pr.waitFor();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "application exit with code {0} for commandString: {1}; errVal: {2}",
                            new Object[]{exitCode, Arrays.toString(argv), errVal});
            }
        } catch (InterruptedException ex) {
            logger.log(Level.FINE,
                    "exception thrown while invoking the command \""+Arrays.toString(argv)+"\"",
                    ex);
        } catch (IOException ex) {
            logger.log(Level.FINE,
                    "exception thrown while invoking the command \""+Arrays.toString(argv)+"\"",
                    ex);
        }
        return new CommandResult(exitCode, retVal.trim().toCharArray(), errVal.trim());
    }

    private void warning(String descr) {
        logger.log(Level.WARNING, "Something went wrong: {0}", descr);
    }

    @Override
    public String toString() {
        return "CommonKWalletProvider{" + "kwalletVersion=" + kwalletVersion + ", pathVersion=" + pathVersion + '}'; // NOI18N
    }

    private class CommandResult {
        private int exitCode;
        private char[] retVal;
        private String errVal;

        public CommandResult(int exitCode, char[] retVal, String errVal) {
            this.exitCode = exitCode;
            this.retVal = retVal;
            this.errVal = errVal;
        }
    }

}
