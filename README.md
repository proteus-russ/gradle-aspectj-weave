Gradle AspectJ Weave plugin
===========================

This is a slightly modified version of https://github.com/eveoh/gradle-aspectj for testing of bytecode 
weaving on some projects that use Java annotation processors as well as having a functional repo. Because 
of this we needed to change the name of the plugin to avoid conflicts. If it works out, we will try
to push changes upstream so that the aspectj plugin supports both source and bytecode.


Usage
-----

Either build this project yourself, and include the `.jar` in your buildscript dependencies,
or use our Maven repo. Then set `ext.aspectjVersion` to your AspectJ version and `apply plugin: 'aspectj'`.
Something like this:

```groovy
buildscript {
    repositories {
        maven {
            url "https://repo.venturetech.net/artifactory/repo"
        }
    }

    dependencies {
        classpath "com.i2rd:gradle-aspectj-weave:1.0"
    }
}

project {
    ext.aspectjVersion = '1.7.4'
}

apply plugin: 'aspectj-weave'
```

Use the `aspectpath` to specify external aspects or external code to weave:

```groovy
dependencies {
    aspectpath "org.springframework:spring-aspects:${springVersion}"
}
```




License
-------

The project is licensed under the Apache 2.0 license. Most/all of the code
originated from the Spring Security project and was created by Luke Taylor and 
Rob Winch. See `LICENSE` for details.
