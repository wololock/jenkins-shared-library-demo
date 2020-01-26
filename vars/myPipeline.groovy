/**
 * myPipeline {
 *     build {
 *         mvn "3.5-jdk-9-slim"
 *
 *         before {
 *             sh "echo '123'"
 *         }
 *     }
 *
 *     deploy {
 *         credentialsId = "xxx-xxx-xxx-xxx"
 *         serviceAccount = "xxx@xxx.iam.gserviceaccount.com"
 *         branches = ["master", "develop"]
 *     }
 * }
 * @param closure
 */
def call(final Closure body) {
    final MyPipelineConfig config = new MyPipelineConfig()
    body.delegate = config
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.call()

    pipeline {
        agent {
            label "docker"
        }

        stages {
            stage("Build") {
                steps {
                    script {
                        if (config.build.before) {
                            withMaven(config.build.mvn, config.build.before)
                        }

                        withMaven(config.build.mvn) {
                            // HERE GOES OUR DEFAULT MVN BUILD COMMAND. THE ONE BELOW IS JUST A DUMMY PLACEHOLDER.
                            sh "mvn -version"
                        }
                    }
                }
            }

            stage("Deploy") {
                when {
                    expression {
                        env.BRANCH_NAME in config.deploy.branches
                    }
                }

                steps {
                    script {
                        withGCloud(credentialsId: config.deploy.credentialsId, serviceAccount: config.deploy.serviceAccount) {
                            // HERE GOES OUR DEFAULT GCLOUD DEPLOY COMMAND. THE ONE BELOW IS JUST A DUMMY PLACEHOLDER.
                            sh "gcloud projects list"
                        }
                    }
                }
            }
        }
    }
}

class MyPipelineConfig {
    BuildConfig build = new BuildConfig()
    DeployConfig deploy = new DeployConfig()

    void build(final Closure body) {
        body.delegate = build
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.call()
    }

    void deploy(final Closure body) {
        body.delegate = deploy
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.call()
    }
}

class BuildConfig {
    String mvn = "3.6.3-jdk-11-slim"
    Closure before

    void mvn(final String version) {
        this.mvn = version
    }
    void before(final Closure body) {
        this.before = body
    }
}

class DeployConfig {
    String credentialsId
    String serviceAccount
    List<String> branches = ["master"]
}
