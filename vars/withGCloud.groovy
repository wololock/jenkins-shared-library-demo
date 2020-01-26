import groovy.transform.Field

@Field
final String DOCKER_IMAGE = "google/cloud-sdk"

/**
 * withGCloud([credentialsId: "xxx-xxx-xxx", serviceAccount: "xxx@xxx.iam.gserviceaccount.com"]) {
 *    sh "gcloud version"
 * }
 */
def call(final Map map, final Closure body) {
    final GCloudConfig config = new GCloudConfig(map)

    ws {
        withCredentials([file(credentialsId: config.credentialsId, variable: "FILE")]) {
            docker.image("${DOCKER_IMAGE}:${config.version}").inside("--entrypoint='' -v ${FILE}:/credentials.json:ro -u root") {
                sh "gcloud auth activate-service-account ${config.serviceAccount} --key-file=/credentials.json"

                body.delegate = this
                body.resolveStrategy = Closure.DELEGATE_FIRST
                body()
            }
        }
    }
}

class GCloudConfig {
    String credentialsId
    String serviceAccount
    String version = "277.0.0-slim"
}
