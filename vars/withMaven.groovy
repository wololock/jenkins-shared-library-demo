import groovy.transform.Field

@Field
final String DEFAULT_VERSION = "3.6.3-jdk-11-slim"

@Field
final String DOCKER_IMAGE = "maven"

@Field
final List<String> PARAMS = [
    "-v /home/wololock/.m2/settings.xml:/var/maven/.m2/settings.xml:ro",
    "-v /home/wololock/.m2/repository/:/var/maven/.m2/repository/:rw,z",
    "-e MAVEN_CONFIG=/var/maven/.m2"
]

/**
 * withMaven {
 *     sh "mvn install ..."
 * }
 * @param body
 */
def call(final Closure body) {
    call(DEFAULT_VERSION, body)
}

/**
 * withMaven("3.5.4-jdk-8-slim") {
 *     sh "mvn -version"
 * }
 * @param version
 * @param body
 */
def call(final String version, final Closure body) {
    docker.image("${DOCKER_IMAGE}:${version}").inside(PARAMS.join(" ")) {
        body.delegate = this
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.call()
    }
}

