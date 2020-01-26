package vars

import com.github.wololock.CustomBasePipelineTest
import org.junit.Test

class myPipelineTest extends CustomBasePipelineTest {

    myPipelineTest() {
        super()

        def withMaven = loadScript("withMaven.groovy")
        helper.registerAllowedMethod("withMaven", [String, Closure]) { String version, Closure cl ->
            withMaven.call(version, cl)
        }
        helper.registerAllowedMethod("withMaven", [Closure]) { Closure cl ->
            withMaven.call(cl)
        }

        def withGCloud = loadScript("withGCloud.groovy")
        helper.registerAllowedMethod("withGCloud", [Map, Closure]) { Map map, Closure cl ->
            withGCloud.call(map, cl)
        }

        binding.setProperty("FILE", "****")
    }

    @Test
    void shouldNotDeployOnBranchDifferentThanMasterAndDevelop() {
        // given:
        def myPipeline = loadScript("myPipeline.groovy")
        binding.getProperty("env").put("BRANCH_NAME", "FEATURE-123/test")

        //when:
        myPipeline {
            build {
                mvn "3.5-jdk-9-slim"
            }

            deploy {
                credentialsId = "xxx-xxx-xxx"
                serviceAccount = "xxx@xxx.iam.gserviceaccount.com"
                branches = ["master", "develop"]
            }
        }

        //then:
        assertJobStatusSuccess()
        //and:
        assertStep "sh", ~/gcloud/, 0
    }

    @Test
    void shouldBuildUsingMvn35Jdk9() {
        // given:
        def myPipeline = loadScript("myPipeline.groovy")
        binding.getProperty("env").put("BRANCH_NAME", "FEATURE-123/test")

        //when:
        myPipeline {
            build {
                mvn "3.5-jdk-9-slim"
            }
        }

        //then:
        assertJobStatusSuccess()
        //and:
        assertStep "image", ~/maven:3.5-jdk-9-slim/
        //and:
        assertStep "sh", ~/mvn/
    }

    @Test
    void shouldDeployFromMasterBranch() {
        // given:
        def myPipeline = loadScript("myPipeline.groovy")
        binding.getProperty("env").put("BRANCH_NAME", "master")

        //when:
        myPipeline {
            build {
                mvn "3.5-jdk-9-slim"
            }

            deploy {
                credentialsId = "xxx-xxx-xxx"
                serviceAccount = "xxx@xxx.iam.gserviceaccount.com"
                branches = ["master", "develop"]
            }
        }

        //then:
        assertJobStatusSuccess()
        //and:
        assertStep "sh", ~/gcloud/, 2
    }

    @Test
    void shouldExecuteBeforeBuildSteps() {
        // given:
        def myPipeline = loadScript("myPipeline.groovy")
        binding.getProperty("env").put("BRANCH_NAME", "FEATURE-123/test")

        //when:
        myPipeline {
            build {
                mvn "3.5-jdk-9-slim"

                before {
                    sh "mvn clean -P coverage -Dparam=123"
                }
            }
        }

        //then:
        assertJobStatusSuccess()
        //and:
        assertStep "sh", ~/mvn clean -P coverage -Dparam=123/
    }
}
