package vars

import com.github.wololock.CustomBasePipelineTest
import org.junit.Test

class withGCloudTest extends CustomBasePipelineTest {

    withGCloudTest() {
        super()

        binding.setProperty("FILE", "****")
    }

    @Test
    void shouldExecuteGCloudAuthLoginCommandOnStartup() {
        //given:
        def withGCloud = loadScript("withGCloud.groovy")

        //when:
        withGCloud(credentialsId: "xxx", serviceAccount: "xxx@xxx.iam.gserviceaccount.com") {
            echo "Executing some GCloud SDK commands"
        }

        //then:
        assertStep "sh", ~/gcloud auth activate-service-account xxx@xxx.iam.gserviceaccount.com/

        assertStep "echo", ~/^Executing some GCloud SDK commands$/
    }
}
