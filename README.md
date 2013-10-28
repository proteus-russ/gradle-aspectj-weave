Gradle AspectJ plugin
=====================

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
        classpath "com.i2rd:gradle-aspectj:1.0"
    }
}

project {
    ext.aspectjVersion = '1.7.4'
}

apply plugin: 'aspectj'
```

Use the `aspectpath` and `ajInpath` to specify external aspects or external code to weave:

```groovy
dependencies {
    aspectpath "org.springframework:spring-aspects:${springVersion}"
}
```

By default, `xlint: ignore` is used. Specify a different value for the `xlint` variable of the `compileAspect` or
`compileTestAspect` task to show AspectJ warnings:

```groovy
compileAspect {
    xlint = 'warning'
}
```

License
-------

The project is licensed under the Apache 2.0 license. Most/all of the code
originated from the Spring Security project and was created by Luke Taylor and 
Rob Winch. See `LICENSE` for details.