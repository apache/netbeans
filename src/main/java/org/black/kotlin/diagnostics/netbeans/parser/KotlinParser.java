package org.black.kotlin.diagnostics.netbeans.parser;

import java.io.Reader;
import java.io.StringReader;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;


/**
 *
 * @author Александр
 */
public class KotlinParser extends Parser {

    private Snapshot snapshot;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        //Reader reader = new StringReader(snapshot.getText().toString());

    }

    @Override
    public Result getResult(Task task) {
        return new KotlinParserResult(snapshot/*, javaParser*/);
    }


    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    public static class KotlinParserResult extends Result {

        private boolean valid = true;

        KotlinParserResult(Snapshot snapshot){//, JavaParser javaParser) {
            super(snapshot);
        }

        @Override
        protected void invalidate() {
            valid = false;
        }

    }

}
