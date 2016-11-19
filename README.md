# Coffee Status - Hackathon Project

### Overview
This project is for the AT&T Hackathon at The DEC in Dallas on Nov 18th to 19th.

It is an Amazon Alexa skill written in Java and deployed to Amazon Lambda. It makes use of S3 to read a text file
and an image file, which are then used to send a tweet using Twitter for Java.

### Configuration
In the Amazon Lamda configuration, this project makes use of several environment variables for passing in the keys
needed for S3, Twitter and the Skill ID. The values required are:

* __APP_ID__ = the Alexa Skill App ID used to verify access to the Lambda
* __BUCKET_NAME__ = the name of the S3 bucket to read the image and the file from
* __ACCESS_KEY__ = the AWS Access Key for the S3 bucket where the image and text file are saved
* __SECRET_KEY__ = the AWS Secret Key for the S3 bucket where the image and text file are saved
* __CONSUMER_KEY__ = Twitter API Consumer Key
* __CONSUMER_SECRET__ = Twitter API Consumer Secret
* __ACCESS_TOKEN__ = Twitter API Access Token
* __ACCESS_TOKEN_SECRET__ = Twitter API Access Token Secret

 ### Files
 The S3 bucket should contain two files: last.txt and last.jpg. The text file should simply contain the name of the
 person who took the last cup of coffee on a single line. The jpeg should be a small image of the person who took the
 last cup.
