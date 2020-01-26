package com.github.wololock

import com.lesfurets.jenkins.unit.BasePipelineTest

import java.util.regex.Pattern

class CustomBasePipelineTest extends BasePipelineTest {

    final Set<String> ignoredStages = [] as Set<String>

    CustomBasePipelineTest() {
        super()
        this.scriptRoots = ["vars", "src"]
        this.scriptExtension = ["groovy"]
        super.setUp()

        helper.registerAllowedMethod("pipeline", [Closure], null)
        helper.registerAllowedMethod("agent", [Closure], null)
        helper.registerAllowedMethod("docker", [Closure], null)
        helper.registerAllowedMethod("label", [String], null)
        helper.registerAllowedMethod("stages", [Closure], null)
        helper.registerAllowedMethod("steps", [Closure]) { Closure cl ->
            def stageName = helper.callStack.reverse().find { it.methodName == 'stage' }.args.first()

            if (!ignoredStages.contains(stageName)) {
                cl.delegate = delegate
                helper.callClosure(cl)
            }
        }
        helper.registerAllowedMethod("script", [Closure]) { Closure cl ->
            cl.delegate = delegate
            helper.callClosure(cl)
        }
        helper.registerAllowedMethod("ws", [Closure]) { Closure cl ->
            cl.delegate = delegate
            helper.callClosure(cl)
        }
        helper.registerAllowedMethod("file", [Map], null)
        helper.registerAllowedMethod("when", [Closure]) { Closure cl ->
            cl.delegate = delegate
            helper.callClosure(cl)
        }
        helper.registerAllowedMethod("expression", [Closure]) { Closure cl ->
            cl.delegate = delegate

            if (!helper.callClosure(cl)) {
                ignoredStages << helper.callStack.reverse().find { it.methodName == 'stage' }.args.first()
            }
        }

        binding.setProperty("env", [:])
    }

    @Override
    Script loadScript(String scriptName) {
        def script =  super.loadScript(scriptName)

        def docker = new docker()
        docker.metaClass.invokeMethod = helper.getMethodInterceptor()
        docker.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        docker.metaClass.methodMissing = helper.getMethodMissingInterceptor()

        helper.registerAllowedMethod("image", [String]) {
            return script
        }
        helper.registerAllowedMethod("inside", [String, Closure]) { String str, Closure cl ->
            cl.delegate = delegate
            helper.callClosure(cl)
        }
        helper.registerAllowedMethod('withCredentials', [List, Closure]) { List list, Closure cl ->
            cl.delegate = delegate
            helper.callClosure(cl)
        }

        binding.setProperty("docker", docker)

        return script
    }

    void assertStep(final String step, final Pattern pattern, final int times = 1) {
        def values =  helper.callStack
            .findAll { it.methodName == step }
            .findAll { it.args.first().toString() =~ pattern }

        def actual = values.size()

        assert  actual == times : """
            Expected that given command matched ${times} times(s), but it matched ${actual} time(s).

            Executed commands: \n${values.join("\n")}

            """.stripIndent()
    }

    void assertStep(final String step, final Closure<Boolean> predicate, final int times = 1) {
        def values = helper.callStack
            .findAll { it.methodName == step }
            .findAll { [it.args*.getClass(), predicate.parameterTypes.toList()].transpose().every { Class a, Class b ->
                b.isAssignableFrom(a)
            }}

        def actual = values.count { predicate.call(it.args ? it.args.first() : null) }

        assert  actual == times : """
            Expected that given command matched ${times} times(s), but it matched ${actual} time(s).

            Executed commands: \n${values.join("\n")}

            """.stripIndent()
    }

    static class docker {}
}
