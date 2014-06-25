## Wearable Speedometer

Uses a series of images similar to the one below to display a user-friendly notification on Wear devices which contains the users current speed in miles per hour.

![23 miles per hour](https://raw.githubusercontent.com/dambrisco/wearablespeedometer/master/WearableSpeedometer/src/main/res/drawable/speedometer_23.png)

The value of [`CurrentSpeedService.DEMO`](https://github.com/dambrisco/wearablespeedometer/blob/master/WearableSpeedometer/src/main/java/com/dambrisco/wearablespeedometer/CurrentSpeedService.java#L24) determines if the app runs in a basic debug mode which simply increments speed once per second or a full location-based mode.

## Known issues

* `locationListener` is not unregistered when the `STOP` intent is received. Causes the app to continue to post notifications to the device when `DEMO` is `false`.
