# External JARs Directory

This directory contains JAR files that need to be checked into source control.

## Purpose

- **Custom Libraries**: JAR files that are not available in public Maven repositories
- **Proprietary Dependencies**: Third-party libraries with licensing restrictions
- **Modified Libraries**: Custom builds or patches of existing libraries
- **Legacy Dependencies**: Older versions not available in Maven Central

## Usage

### Adding JARs to Maven

To use JARs from this directory in your Spring Boot project, add them to the Maven build:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>custom-library</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/../external_jars/custom-library-1.0.0.jar</systemPath>
</dependency>
```

### Installing to Local Maven Repository

Alternatively, install JARs to your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=external_jars/custom-library-1.0.0.jar \
  -DgroupId=com.example \
  -DartifactId=custom-library \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

## Guidelines

1. **Document Dependencies**: Add entries below for each JAR file
2. **Version Control**: These files are tracked in Git (unlike typical JARs)
3. **Licensing**: Ensure you have rights to distribute these JARs
4. **Updates**: Keep this README updated when adding/removing JARs

## Current JARs

<!-- Add entries for each JAR file in this directory -->
<!-- Example:
- `aerospike-custom-client-1.2.3.jar` - Custom Aerospike client with patches
- `proprietary-lib-2.1.0.jar` - Proprietary library from vendor XYZ
-->

*No external JARs currently in this directory.*
