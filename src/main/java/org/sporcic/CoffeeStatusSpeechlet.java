package org.sporcic;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.*;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;

public class CoffeeStatusSpeechlet implements SpeechletV2 {

    private final static String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private final static String ACCESS_KEY = System.getenv("ACCESS_KEY");
    private final static String SECRET_KEY = System.getenv("SECRET_KEY");
    private final static String CONSUMER_KEY = System.getenv("CONSUMER_KEY");
    private final static String CONSUMER_SECRET = System.getenv("CONSUMER_SECRET");
    private final static String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
    private final static String ACCESS_TOKEN_SECRET = System.getenv("ACCESS_TOKEN_SECRET");


    private static final Logger log = LoggerFactory.getLogger(CoffeeStatusSpeechlet.class);

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}",
                requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}",
                requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}",
                requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("CoffeeStatusIntent".equals(intentName)) {
            return getCoffeeStatusResponse();
        } else if("ShameUserIntent".equals(intentName)) {
            return tweetTheShame();
        } else {
            return getUnknownCommandResponse();
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnd requestId={}, sessionId={}",
                requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to Coffee Status";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Coffee Pot");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getCoffeeStatusResponse() {

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Coffee Pot");
        String name;

        try {
            name = getNameFromBucket();
        } catch (IOException ex) {
            log.error("Failed to get name for bucket", ex);
            PlainTextOutputSpeech errorSpeech = new PlainTextOutputSpeech();
            errorSpeech.setText("Failed to get name for Bucket");
            return SpeechletResponse.newTellResponse(errorSpeech, card);
        }

        String speechText = name + " took the last cup";
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getUnknownCommandResponse() {

        String speechText = "I did not understand your command";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Coffee Pot");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private String getNameFromBucket() throws IOException {

        log.info("Getting name for S3 Bucket {} using Key {}", BUCKET_NAME, ACCESS_KEY);
        InputStream input = null;

        try {
            AmazonS3 s3Client = new AmazonS3Client(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
            S3Object nameFile = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, "last.txt"));

            input = nameFile.getObjectContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();

            while (true) {
                String line = reader.readLine();
                if(line == null) {
                    break;
                } else {
                    builder.append(line);
                }
            }

            reader.close();
            return builder.toString();
        } finally {
            if(input != null) {
                try { input.close(); } catch (Exception ignored) {}
            }
        }
    }

    private SpeechletResponse tweetTheShame() {

        String speechText = "It is done";
        InputStream input = null;

        try {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(CONSUMER_KEY)
                    .setOAuthConsumerSecret(CONSUMER_SECRET)
                    .setOAuthAccessToken(ACCESS_TOKEN)
                    .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

            Configuration conf = cb.build();

            TwitterFactory factory = new TwitterFactory(conf);
            Twitter twitter = factory.getInstance();

            String screenName = twitter.getScreenName();
            String shameName = getNameFromBucket();

            log.info("Shaming {} on Twitter from {}", shameName, screenName);

            input = getImage();

            StatusUpdate update = new StatusUpdate(shameName + " took the last cup of coffee, and didn't make another pot!");
            update.setMedia(shameName + ".jpg", input);
            Status status = twitter.updateStatus(update);

            log.info("Successfully update the status to [{}]", status.getText());

        } catch (Exception tex) {
            log.error("Failed to update Twitter status", tex);
            speechText = "Something went wrong";
        } finally {
            if(input != null) {
                try { input.close(); } catch (Exception ignored) {}
            }
        }

        SimpleCard card = new SimpleCard();
        card.setTitle("Tweet the Status");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }


    private InputStream getImage() throws IOException {

        log.info("Getting name for S3 Bucket {} using Key {}", BUCKET_NAME, ACCESS_KEY);
        AmazonS3 s3Client = new AmazonS3Client(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
        S3Object nameFile = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, "last.jpg"));

        return nameFile.getObjectContent();
    }

}
