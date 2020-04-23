package uk.gov.justice.digital.hmpps.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.jar.JarFile

class JavaBuildFuncTest {

  @TempDir
  lateinit var projectDir: File

  @AfterEach
  fun `Delete project`() {
    FileUtils.cleanDirectory(projectDir)
  }

  @Test
  fun `Spring dependency versions are defaulted from the dependency management plugin`() {
    makeProject(javaProjectDetails(projectDir))

    val webVersion = getDependencyVersion(projectDir, "spring-boot-starter-web")
    val actuatorVersion = getDependencyVersion(projectDir, "spring-boot-starter-actuator")

    assertThat(webVersion).isEqualTo(actuatorVersion)
  }

  @Test
  fun `Manifest file contains project name and version`() {
    makeProject(javaProjectDetails(projectDir))

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, "spring-boot-project-java")
    val jarFile = JarFile(file)
    assertThat(jarFile.manifest.mainAttributes.getValue("Implementation-Version")).isEqualTo(LocalDate.now().format(ISO_DATE))
    assertThat(jarFile.manifest.mainAttributes.getValue("Implementation-Title")).isEqualTo("spring-boot-project-java")
  }

  @Test
  fun `The Owasp dependency analyze task is available`() {
    makeProject(javaProjectDetails(projectDir))

    val result = buildProject(projectDir, "dependencyCheckAnalyze", "-m")
    assertThat(result.output)
        .contains(":dependencyCheckAnalyze SKIPPED")
        .contains("SUCCESSFUL")
  }

  @Test
  fun `The Owasp dependency check suppression file is copied into the project`() {
    makeProject(javaProjectDetails(projectDir))

    val result = buildProject(projectDir, "tasks")
    assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val suppressionFile = findFile(projectDir, DEPENDENCY_SUPPRESSION_FILENAME)
    assertThat(suppressionFile).exists()
  }

  @Test
  fun `The gradle version dependency dependencyUpdates task is available`() {
    makeProject(javaProjectDetails(projectDir))

    val result = buildProject(projectDir, "dependencyUpdates", "-m")
    assertThat(result.output)
        .contains(":dependencyUpdates SKIPPED")
        .contains("SUCCESSFUL")
  }

  @Test
  fun `The application insights jar is copied into the build lib`() {
    makeProject(javaProjectDetails(projectDir))

    val result = buildProject(projectDir, "assemble")
    assertThat(result.task(":assemble")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, "applicationinsights-agent")
    assertThat(file).exists()
  }

  @Test
  fun `Junit 5 tests can be executed`() {
    makeProject(javaProjectDetails(projectDir))

    val result = buildProject(projectDir, "test")
    assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  fun `Junit 4 tests will not event compile`() {
    makeProject(javaProjectDetails(projectDir).copy(testClass = """
          package uk.gov.justice.digital.hmpps.app;
          
          import org.junit.Test;

          import static org.assertj.core.api.Assertions.assertThat;

          public class ApplicationTest {
              @Test
              public void aTest() {
                  assertThat("anything").isEqualTo("anything");
              }
          }
    """.trimIndent()))

    buildProjectAndFail(projectDir, "compileTest")
  }

}