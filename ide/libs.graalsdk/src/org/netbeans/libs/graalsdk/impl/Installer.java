
package org.netbeans.libs.graalsdk.impl;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void validate() {
        super.validate();
        // Truffle runtime is very vocal if it is run in interpreter only mode.
        // That mode is the case in "normal" JDKs, which are the NetBeans
        // baseline - the warnings should be silenced accordingly
        if (!System.getProperties().contains("polyglot.engine.WarnInterpreterOnly")) { //NOI18N
            System.setProperty("polyglot.engine.WarnInterpreterOnly", "false"); //NOI18N
        }
        // The default Truffle runtime uses a native library. That library fails
        // hard when it shall be loaded by multiple classloaders. So for now
        // use the fallback runtime
        if (!System.getProperties().contains("truffle.UseFallbackRuntime")) { //NOI18N
            System.setProperty( "truffle.UseFallbackRuntime", "true" ); //NOI18N
        }
    }
}