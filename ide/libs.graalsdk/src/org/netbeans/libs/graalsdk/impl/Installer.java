
package org.netbeans.libs.graalsdk.impl;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void validate() {
        super.validate();
        // If truffle runtimes are executed directly they try to load another
        // copy of the native integration library. This fails. The fallback
        // runtime can run independently of this.
        System.setProperty( "polyglot.engine.WarnInterpreterOnly", "false" ); //NOI18N
        System.setProperty( "truffle.UseFallbackRuntime", "true" ); //NOI18N
    }
}