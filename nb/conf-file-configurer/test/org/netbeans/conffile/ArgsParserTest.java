package org.netbeans.conffile;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.netbeans.conffile.ArgsParser.ArgsResult;

/**
 *
 * @author Tim Boudreau
 */
public class ArgsParserTest {

    @Test
    public void testArgsParsing() {
        ArgsParser ap = new ArgsParser()
                .add("foo").withHelpText("Set the foo.").withNoArgument()
                .add("bar").withHelpText("Set the bar.").shortName('b').required().takesArgument()
                .add("file").withHelpText("A file to process").shortName('f')
                .matching(str -> {
                    return str.indexOf('/') >= 0;
                })
                .add("glorp").withNoArgument();

        StringBuilder sb = new StringBuilder("Usage:\njava -jar x.jar");
        ap.printHelp(sb, "stuff", true);
        assertFalse(sb.length() == 0);
        String[] lines = sb.toString().split("\n");
        assertEquals("Usage:", lines[0]);
        assertEquals("java -jar x.jar [--bar / -b] <required> [--file / -f] <value> --foo --glorp stuff", lines[1]);
        assertEquals("    --bar / -b <b>  : Required. Set the bar.", lines[3]);


        System.out.println(sb);

        ArgsResult res = ap.parse("--foo", "-b", "52", "wookies");
        assertTrue(res.isSet("foo"));
        assertTrue(res.isSet("bar"));
        assertEquals("52", res.get("bar"));
        assertEquals("wookies", res.unhandledArgument());
        assertEquals(1, res.unhandled().size());
        assertEquals(Arrays.asList("wookies"), res.unhandled());
        assertEquals("true", res.get("foo"));

    }

}
