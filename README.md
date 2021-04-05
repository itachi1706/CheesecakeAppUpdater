CheesecakeAppUpdater
========
[![Maven Central](https://img.shields.io/maven-central/v/com.itachi1706.appupdater/appupdater)](https://search.maven.org/artifact/com.itachi1706.appupdater/appupdater)
[![JIRA Issues](https://img.shields.io/badge/JIRA-Issues-blue)](https://itachi1706.atlassian.net/browse/CAUANDLIB)
[![GitHub Actions](https://github.com/itachi1706/CheesecakeAppUpdater/workflows/Android%20CI/badge.svg)](https://github.com/itachi1706/CheesecakeAppUpdater/actions)
[![GitHub release](https://img.shields.io/github/release/itachi1706/CheesecakeAppUpdater.svg)](https://github.com/itachi1706/CheesecakeAppUpdater/releases) 
[![GitHub license](https://img.shields.io/github/license/itachi1706/CheesecakeAppUpdater.svg)](https://github.com/itachi1706/CheesecakeAppUpdater/blob/master/LICENSE) 
[![Code Climate](https://codeclimate.com/github/itachi1706/CheesecakeAppUpdater/badges/gpa.svg)](https://codeclimate.com/github/itachi1706/CheesecakeAppUpdater) 
[![Test Coverage](https://codeclimate.com/github/itachi1706/CheesecakeAppUpdater/badges/coverage.svg)](https://codeclimate.com/github/itachi1706/CheesecakeAppUpdater/coverage) 
[![Issue Count](https://codeclimate.com/github/itachi1706/CheesecakeAppUpdater/badges/issue_count.svg)](https://codeclimate.com/github/itachi1706/CheesecakeAppUpdater)

This is an updater library that was primarly designed to suit my needs and allows updating of my Android apps outside of the GPS.  
For more information see the sample applicatiion on how to use this library  

## Important Notice
* This library requires your minSDK to be set to at least 16 (Jelly Bean). Android ICS and before are not supported unfortunately  
* This library requires you to have Java 8 support compatibilty. You can do so by adding the following lines into your app-level build.gradle file
```gradle
android {
…
  compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
…
}
```

## Usage - Maven Central
To use this library in an Android Project, add the following lines into your app-level build.gradle file

```gradle
repositories {
	mavenCentral()
}
…
dependencies {
  implementation 'com.itachi1706.appupdater:appupdater:<latest-version>' // See badge for latest version number
}
```

## Usage - Artifactory
To use this library in an Android Project, add the following lines into your app-level build.gradle file

```gradle
repositories {
	maven {
		url "https://itachi1706.jfrog.io/artifactory/ccn-android-libs/"
	}
}
…
dependencies {
  implementation 'com.itachi1706.appupdater:appupdater:<latest-version>' // See badge for latest version number
}
```

## Usage - Bintray (Deprecated)
To use this library in an Android Project, add the following lines into your app-level build.gradle file

```gradle
repositories {
	maven {
		url  "https://dl.bintray.com/itachi1706/ccn-android-lib"
	}
}
…
dependencies {
  implementation 'com.itachi1706.appupdater:appupdater:<latest-version>' // See badge for latest version number
}
```

## Usage - JCenter (Deprecated)
To use this library in an Android Project, add the following lines into your app-level build.gradle file

```gradle
repositories {
	jcenter()
}
dependencies {
  implementation 'com.itachi1706.appupdater:appupdater:<latest-version>' // See badge for latest version number
}
```

## Notes on usage for Base URL

Your base server URL for the updater call MUST end with a query accepting the application packagename. The library will autofill the android packagename for you. An example url is:
```
https://localhost/update?packagename=
```

## Output format

To trigger the updater correctly, you need to provide a JSON file with the following format after requesting an update check from the Android app

An example JSON is shown below

```json
{
  "msg": {
    "index": 0,
    "id": "1",
    "packageName": "com.itachi1706.appupdatersample",
    "appName": "App Updater Sample",
    "dateCreated": "1454235037",
    "latestVersion": "1.0.0",
    "latestVersionCode": "1",
    "apptype": "Android Application",
    "updateMessage": [
      {
        "index": 0,
        "id": "1",
        "appid": "1",
        "updateText": "- Changelog here\r\n- Second line",
        "dateModified": "1454235542",
        "versionCode": "1",
        "versionName": "1.0.0",
        "labels": "<font color=\"green\">LATEST<\/font> ",
        "url": "https://github.com/itachi1706/CheesecakeAppUpdater/releases/download/1.0.0/app-release.apk"
      }
    ]
  },
  "error": 21
}
```
