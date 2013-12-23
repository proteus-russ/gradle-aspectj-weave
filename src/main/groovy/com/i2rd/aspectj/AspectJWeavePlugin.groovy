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
            dependsOn project.processResources
            sourceSet = project.sourceSets.main
//            inputs.files(sourceSet.allSource)
//            outputs.dir(sourceSet.output.classesDir)
            aspectPath = project.configurations.aspectpath
        }
        project.tasks.classes.dependsOn project.tasks.weaveAspect


        project.tasks.create(name: 'weaveTestAspect', overwrite: true,
                description: 'Bytecode Weaves Binary Test Aspects', type: Ajc) {
            dependsOn project.processTestResources, project.compileJava
            mustRunAfter project.processTestResources
            sourceSet = project.sourceSets.test
//            inputs.files(sourceSet.allSource)
//            outputs.dir(sourceSet.output.classesDir)
            aspectPath = project.configurations.aspectpath
        }
        project.tasks.testClasses.dependsOn project.tasks.weaveTestAspect

    }
}

class Ajc extends DefaultTask {
    SourceSet sourceSet
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
        logger.info("\tinPath:     ${sourceSet.output.classesDir.absolutePath}")
        logger.info("\taspectPath: ${aspectPath.asPath}")
        if(sourceSet.output.classesDir.exists()) {
            ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: project.configurations.ajtools.asPath)
            ant.iajc(classpath: sourceSet.compileClasspath.asPath, fork: 'true', destDir: sourceSet.output.classesDir.absolutePath,
                    source: project.convention.plugins.java.sourceCompatibility,
                    target: project.convention.plugins.java.targetCompatibility,
                    xlint: xlint,
                    aspectPath: aspectPath.asPath, sourceRootCopyFilter: '**/*.java,**/*.aj', showWeaveInfo: 'true') {
                    inpath {
                        pathelement(location: sourceSet.output.classesDir.absolutePath)
                    }

            }
        }
    }
}