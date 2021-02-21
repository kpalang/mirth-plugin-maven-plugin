# mirth-plugin-maven-plugin

A maven plugin to simplify and automate [NextGen Connect](https://github.com/nextgenhealthcare/connect) plugin development.

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
| \<outputPath> | Specifies where to put generated plugin.xml | distribution/plugin.xml |

---

## Installation
Clone repo and run `mvn clean install`

--- 

## Usage
See [Sample project](https://github.com/kpalang/mirth-sample-plugin) on Github
