package me.onetwo.aiautotrade.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

/**
 * 기본적인 아키텍처 규칙을 검증하는 테스트
 */
class SimpleArchitectureTest {

    private val importedClasses = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("me.onetwo.aiautotrade")

    @Test
    fun `Enum classes should be in common enums package`() {
        classes()
            .that().areEnums()
            .should().resideInAPackage("..common.enums..")
            .check(importedClasses)
    }

    @Test
    fun `Config classes should be in config package`() {
        classes()
            .that().haveSimpleNameEndingWith("Config")
            .should().resideInAPackage("..config..")
            .check(importedClasses)
    }

    @Test
    fun `Service interfaces should follow naming convention`() {
        classes()
            .that().areInterfaces()
            .and().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..infrastructure..")
            .check(importedClasses)
    }
}