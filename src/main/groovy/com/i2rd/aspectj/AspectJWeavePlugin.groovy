package com.i2rd.aspectj

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

/**
 * Based on the https://github.com/eveoh/gradle-aspectj plugin
 * with modifications to suit the i2rd build needs.
 * @author Luke Taylor
 * @author Mike Noordermeer
 */
class AspectJWeavePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        if (!project.hasProperty('aspectjVersion')) {
            throw new GradleException("You must set the property 'aspectjVersion' before applying the aspectj weave plugin")
        }

        if (project.configurations.findByName('ajtools') == null) {
            project.configurations.create('ajtools')
            project.dependencies {
                ajtools "org.aspectj:aspectjtools:${project.aspectjVersion}"
                compile "org.aspectj:aspectjrt:${project.aspectjVersion}"
            }
        }

        if (project.configurations.findByName('aspectpath') == null) {
            project.configurations.create('aspectpath')
        }

        project.tasks.create(name: 'weaveAspect', overwrite: true, description: 'Bytecode Weaves Binary Aspects', type: Ajc) {
            dependsOn project.configurations*.getTaskDependencyFromProjectDependency(true, "compileJava")
            mustRunAfter project.tasks.jar
            sourceSet = project.sourceSets.main
//            inputs.files(sourceSet.allSource)
//            outputs.dir(sourceSet.output.classesDir)
            aspectPath = project.configurations.aspectpath
            jars = project.jar.outputs.files
        }
        project.tasks.jar.finalizedBy project.tasks.weaveAspect

    }
}

class Ajc extends DefaultTask {
    SourceSet sourceSet
    FileCollection jars
    FileCollection aspectPath
    String xlint = 'ignore'

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    @TaskAction
    def weave() {
        logger.info("="*30)
        logger.info("="*30)
        logger.info("Running ajc ...")
        logger.info("\tclasspath:  ${sourceSet.compileClasspath.asPath}")
        logger.info("\tinPath:     ${jars.toList().toString()}")
        logger.info("\taspectPath: ${aspectPath.asPath}")
        if(!sourceSet.output.classesDir.exists())
            return
        jars.each {file ->
            def weavedDir = new File(file.parentFile, 'weaved')
//            Files.copy(file.toPath(), new File(file.parentFile, 'COPY-' + file.name).toPath())
            ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: project.configurations.ajtools.asPath)

            ant.iajc(classpath: sourceSet.compileClasspath.asPath, fork: 'true',
                    outxml: true,
                    referenceinfo: true,
                    PreserveAllLocals: true,
                    maxmem: '2048m',
                    source: project.convention.plugins.java.sourceCompatibility,
                    target: project.convention.plugins.java.targetCompatibility,
                    xlint: xlint,
                    destDir: weavedDir.absolutePath,
                    aspectPath: aspectPath.asPath, sourceRootCopyFilter: '**/*.java,**/*.aj', showWeaveInfo: 'true') {
                    inpath {
                        pathelement(location: file.absolutePath)
                    }

            }
            ant.jar(
                index:true,
                basedir: weavedDir.absolutePath,
                destfile: file.absolutePath,
                update: true
            )
        }
    }
}