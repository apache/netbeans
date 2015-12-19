package org.black.kotlin.diagnostics.netbeans.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinParser extends Parser {

    private Snapshot snapshot;
    //private JavaParser javaParser;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        Reader reader = new StringReader(snapshot.getText().toString());
//        try {
//            DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                    Message((char) reader.read()));
            //javaParser = new JavaParser(reader);
            //  javaParser.CompilationUnit();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        
    }

    @Override
    public Result getResult(Task task) {
        return new KotlinParserResult(snapshot/*, javaParser*/);
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    public static class KotlinParserResult extends Result {

        //private JavaParser javaParser;
        private boolean valid = true;

        KotlinParserResult(Snapshot snapshot){//, JavaParser javaParser) {
            super(snapshot);
        //    this.javaParser = javaParser;
        }

//        public JavaParser getJavaParser() throws org.netbeans.modules.parsing.spi.ParseException {
//            if (!valid) {
//                throw new org.netbeans.modules.parsing.spi.ParseException();
//            }
//            return javaParser;
//        }

        @Override
        protected void invalidate() {
            valid = false;
        }

    }

}
