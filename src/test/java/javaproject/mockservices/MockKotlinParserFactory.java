package javaproject.mockservices;

import java.util.Collection;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;

/**
 *
 * @author Александр
 */
public class MockKotlinParserFactory extends ParserFactory{

    @Override
    public Parser createParser(Collection<Snapshot> snapshots) {
        return new KotlinParser();
    }
    
}
