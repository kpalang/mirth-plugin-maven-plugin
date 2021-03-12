# mirth-plugin-maven-plugin

A maven plugin to simplify and automate [NextGen Connect](https://github.com/nextgenhealthcare/connect) plugin development.

---
## Installation 

```xml
<repository>
    <id>nexus</id>
    <url>https://nexus.kaurpalang.com/repository/maven-public/</url>
</repository>
```
```xml
<dependency>
  <groupId>com.kaurpalang</groupId>
  <artifactId>mirth-plugin-maven-plugin</artifactId>
  <version>1.0.1</version>
</dependency>
```
---
## Usage

See [Sample project](https://github.com/kpalang/mirth-sample-plugin) on Github

### `@ServerClass`
Annotated class will be generated as a `<string>` entry.
```xml
<serverClasses>
    <string>com.kaurpalang.mirthpluginsample.server.ServerPlugin</string>
    <string>com.kaurpalang.mirthpluginsample.server.ServerPlugin2</string>
</serverClasses>
```

### `@ClientClass`
Annotated class will be generated as a `<string>` entry.
```xml
<clientClasses>
    <string>com.kaurpalang.mirthpluginsample.client.ClientPlugin</string>
    <string>com.kaurpalang.mirthpluginsample.client.ClientPlugin2</string>
</clientClasses>
```

### `@ApiProvider(ApiProviderType type)`
Annotated class will be generated as a `<apiProvider>` entry.
```xml
<apiProvider name="com.kaurpalang.mirthpluginsample.shared.ApiProviderSample" type="SERVLET_INTERFACE"/>
```

### Libraries

All libraries inside `pluginroot/libs/runtime/{type}` are packaged into the `.zip` archive into `libs` directory.
```xml
<library path="libs/sample-external.jar" type="{type}"/>
```

---

## Maven goals overview

#### generate-aggregator
Pur pose is to generate a file to store all found classes before annotation processing.

| Parameter | Description | Default |
| ------ | ------ | ------  |
| \<aggregatorPath> | Specifies where to create aggregation file | distribution/aggregated.json |

#### generate-plugin-xml
Purpose is to generate the actual plugin.xml file.

| Parameter | Description | Default |
| ------ | ------ | ------  |
| \<name> | Plugin's name | Default plugin |
| \<author> | Plugin's author | John Doe |
| \<pluginVersion> | Plugin version | 42 |
| \<mirthVersion> | Mirth versions this plugin is compatible with | 3.10.1 |
| \<url> | Plugin's website | https://github.com/kpalang/mirth-sample-plugin |
| \<description> | Plugin's description | A sample Mirth plugin to showcase my Maven plugin |
| \<path> | The name of the directory that will be extracted into Mirth's extensions directory | sampleplugin |
| \<aggregatorPath> | Aggregation file location | distribution/aggregator/aggregated.json |
| \<outputPath> | Specifies where to put generated plugin.xml | ${project.basedir}/plugin.xml |
