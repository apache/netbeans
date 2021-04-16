/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.nbcode.integration;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.progress.spi.RunOffEDTProvider;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = RunOffEDTProvider.class, position = 90)
public class LspRunOffEdtProvider implements RunOffEDTProvider {
    
    private static final RequestProcessor TRIVIAL = new RequestProcessor(LspRunOffEdtProvider.class);
    
    @Override
    public void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorAfter, int dialogAfter) {
        if (Mutex.EVENT.isReadAccess()) {
            // PENDING: the EDT should continue to service events.
            TRIVIAL.post(operation).waitFinished();
        } else {
            operation.run();
        }
    }
}
