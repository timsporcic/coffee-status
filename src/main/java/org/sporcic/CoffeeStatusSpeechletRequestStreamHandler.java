package org.sporcic;

import java.util.HashSet;
import java.util.Set;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class CoffeeStatusSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<String>();

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        String appId = System.getenv("APP_ID");

        supportedApplicationIds.add(appId);
    }

    public CoffeeStatusSpeechletRequestStreamHandler() {
        super(new CoffeeStatusSpeechlet(), supportedApplicationIds);
    }
}
