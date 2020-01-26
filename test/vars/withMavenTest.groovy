package vars

import com.github.wololock.CustomBasePipelineTest
import org.junit.Test

class withMavenTest extends CustomBasePipelineTest {

    @Test
    void shouldExecuteShStepTwice() {
        //given:
        def withMaven = loadScript("withMaven.groovy")

        //when:
        withMaven {
            sh "mvn clean install"
            sh returnStdout: true, script: "ls -la"
        }

        //then:
        assertStep "sh", ~/mvn clean install/

        assertStep "sh", { Map params ->
            params.returnStdout && params.script.startsWith("ls -la")
        }

        assertJobStatusSuccess()
    }

    @Test
    void shouldNotExecuteAnyShStepForEmptyClosure() {
        //given:
        def withMaven = loadScript("withMaven.groovy")

        //when:
        withMaven { }

        //then:
        assertStep "sh", ~/.*/, 0
    }
}
