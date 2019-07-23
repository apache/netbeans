package javaproject.mockservices

import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.netbeans.modules.parsing.api.Snapshot
import org.netbeans.modules.parsing.spi.Parser
import org.netbeans.modules.parsing.spi.ParserFactory


class MockKotlinParserFactory : ParserFactory() {
    override fun createParser(snapshots: Collection<Snapshot>?) = KotlinParser()
}