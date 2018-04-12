# Demo Android CloudSight API Consumer

This demo app built with Java demonstrates how easy it is to work with CloudSight's API. To start you'll need to acquire an `API KEY` and `API SECRET`.

# Building

You can build the project with Android Studio 2 but version 3 is preferred for a faster build time. The target SDK version for this app is 23 but this demo app has been compilete for SDK version 25. Once you start make an attempt to build, Android Studio should prompt you of missing SDKs that you can easily install to have a successful build.

## Deploying in an Emulator
With Android Studio 3 testing this demo app couldn't be easier. You only need to clone this project to a location you prefer on your local machine and open the project with Android Studio then choose a deployment target on your available list of virtual devices with API version 26 of Android.

## Deploying on a Physical Device
To test out this demo app on your physical device, there are a few things you must do:

On the device, open the **Settings** app, select **Developer** options, and then enable **USB** debugging.
**Note:** If you do not see Developer options, follow the instructions to [enable developer options](https://developer.android.com/studio/debug/dev-options.html).

### Set up your system to detect your device.
- Windows: Install a USB driver for Android Debug Bridge (adb).
- Mac OS X: It just works. Skip this step.
- Ubuntu Linux: Use `apt-get install` to install the `android-tools-adb package`.

Make sure that you are in the plugdev group. If you see the following error message, adb did not find you in the plugdev group:

`error: insufficient permissions for device: udev requires plugdev group membership`

Use `id` to see what groups you are in. Use `sudo usermod -aG plugdev $LOGNAME` to add yourself to the plugdev group.

The following example shows how to install the Android adb tools package.

`apt-get install android-tools-adb`

### Connect to your device
When you are set up and plugged in over USB, you can click **Run ▶︎** in Android Studio to build and run your app on the device.

You can also use adb to issue commands, as follows:

- Verify that your device is connected by running the adb devices command from your `android_sdk/platform-tools/` directory. If connected, you'll see the device listed.
- Issue any adb command with the `-d` flag to target your device.

## Sending a Request

Once Gradle is finished successfully and upon launching the app, you should see a permission request similar to the this one: 

<img src="https://raw.githubusercontent.com/cloudsight/cloudsight-android-demo/add_readme/permission.png" alt="Permission" />

From this point, after permiting the demo to access your images, you can capture an image or send one already on your device to CloudSight Inc. and watch the magic happen.

**Note:** You don't have to allow permission to access images already on your device but you do need to allow the app to use your device's camera to capture images to send to CloudSight's API

## Issues

Please feel free to post issues or even contribute. If you have problems with setting this up on your local machines or device, please contact:

* **Chris Weilemann** - CloudSight Sr. Developer (2016-2017), CTO (2017 to present). [cweilemann@github](https://github.com/cweilemann), [chris@cloudsight](mailto:chris@cloudsight.ai)
* **Emmanuel Hayford** - CloudSight Developer (2017 to present) [siaw23@github](https://github.com/siaw23), [emmanuel@cloudsight](mailto:emmanuel@cloudsight.ai)