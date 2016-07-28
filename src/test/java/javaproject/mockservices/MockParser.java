package javaproject.mockservices;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author Александр
 */
public class MockParser extends Parser{

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
    }

    @Override
    public Result getResult(Task task) throws ParseException {
    
        return new Result(null){
            @Override
            protected void invalidate() {
            }
        
        };
    
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
}
