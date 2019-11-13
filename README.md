
# Airfeed-Android

Airfeed is an anonymous location based chat app, like the late app Yik Yak but with some added features. This repo is for the Android version of Airfeed, developed by Joe Connolly.   Airfeed used to be called Kipper.  During the summer (2019) several classmates joined the project and the app was renamed Airfeed.  Joe Connolly left the project in October 2019.  Since then, several new features have been added to the app.  

Users can post, comment, make polls, and vote on other user's content anonymously.  Find the Android version on  [Google Play](https://play.google.com/store/apps/details?id=com.harshityadav.airfeed)

Key Technologies 
 - **Firebase Realtime Database** - used to store post data. 
 - **Firebase Notifications** - used to notify users if someone replies to their post or comment.  
 - **Firebase Node.js Cloud Functions** - used for several tasks, including to send a firebase notification to all users whose posts or comments have been replied to.   
