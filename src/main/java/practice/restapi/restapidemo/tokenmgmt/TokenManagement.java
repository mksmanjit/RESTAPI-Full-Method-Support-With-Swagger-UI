package practice.restapi.restapidemo.tokenmgmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TokenManagement {
    
    /**
     * Containing list of tokens.
     */
    private static List<String> userTokens =
                Collections.synchronizedList(new ArrayList<String>());
    
    /**
     * Generates a random number using java.util.Random. Seeds the random
     * generator on current system time. If the generated token already
     * exists in the userTokens, repeatedly tries to generate a
     * token that is unique.
     *
     * @return a generated unique token.
     */
    public static synchronized String createToken() {
        Random random = new Random(System.currentTimeMillis());
        String token = String.valueOf(random.nextLong());
        while (getUserTokens().contains(token)) {
            token = String.valueOf(random.nextLong());
        }
        getUserTokens().add(token);
        return token;
    }

    public static List<String> getUserTokens() {
        return userTokens;
    }

}
