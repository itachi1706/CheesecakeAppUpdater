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

## Notification Permission
Android 13 and after devices require the app to have the notification permission to be able to show the notification.
Right now the library does not handle this permission and will presume the notification is granted and may hence crash.
Hence, ensure that the application has the notification permission before using any functions in this library.

In the future we will make it such that the application will fail gracefully without the notification permission instead.

## To Allow Package Installation
Due to changes in Google Play policy, there are restrictions on what apps can use the "REQUEST_INSTALL_PACKAGES" permission. Hence to allow other use of this library we are removing it as required.  
To add the ability to install packages, you will need to add the following to your AndroidManifest.xml file:

```xml
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

For more information, click here to view the [Google's help page on the policy update](https://support.google.com/googleplay/android-developer/answer/12085295?hl=en)

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
