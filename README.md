# Prank-Call-Audio-Video-Chat-App
A dynamic Prank Call Android app developed by me that allows users to schedule voice or video prank calls with customizable options. On the main screen, users can schedule calls on clicking the schedule a call card by selecting the call type (voice or video), specifying the name, time, date, ringtone, and color theme. For video calls if triggered through service, users can choose videos from their device gallery otherwise if the video call is triggered by user clicking on the fake character on the main screen then on accepting the call the video being fetched from the api is played through exoplayer and on clicking the decline button the call ends. The incoming call is triggered exactly at the scheduled time using AlarmManager.setExactAndAllowWhileIdle.

The app features fake characters, fetched via API, that users can interact with in fake chats, including sending/receiving images. The app also integrates Google AdMob app open and interstital ads, managed via Firebase Remote Config, with interstitial ads appearing after specific user actions. A premium option offers in-app subscriptions, and a history section lets users view their interaction logs.

The core functionality ensures precise triggering of prank calls, including video playback via ExoPlayer for incoming video calls. On accepting the incoming video calls users can see themselves via the device camera and pre-selected videos are being played in a loop until the user ends the call. The app supports receiving calls even in idle or sleep mode of the device using wake locks and keyguard manager, ensuring compatibility with the latest Android 14 version. The app when there is no internet connected to the device , so then on opening the app the app prompts the user through a dialog to check the internet connection and gives the user an option to try again to check the internet connection because the app is unable to work properly without internet as much of the data is being fetched through the api.
