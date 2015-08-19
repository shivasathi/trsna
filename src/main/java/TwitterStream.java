
import org.json.JSONException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TwitterStream {

    static double[][] boundingBox= {
            {-180, -90},
            {180, 90}
    };

    static StatusListener listener = new StatusListener() {

        HashMap<Long, HashMap<String, Integer>> countsPerMinute = new HashMap<Long,HashMap<String,Integer>>();

        public void onStatus(Status status) {
            GeoLocation loc = status.getGeoLocation();
            String screenName = status.getUser().getScreenName();

            double tweet_lat, tweet_long;

            if(loc == null) {
                GeoLocation[][] bounding = status.getPlace().getBoundingBoxCoordinates();
                double latitude = 0.0, longitude = 0.0;
                for (int i = 0; i < bounding.length; i++) {
                    for (int j = 0; j < bounding[i].length; j++) {
                        latitude += bounding[i][j].getLatitude();
                        longitude += bounding[i][j].getLongitude();
                    }
                }

                tweet_lat = latitude/4;
                tweet_long = longitude/4;
            }else{
                tweet_lat = loc.getLatitude();
                tweet_long = loc.getLongitude();
            }

            ArrayList<String> hashTags = new ArrayList<String>();
            for(HashtagEntity entity : status.getHashtagEntities()){
                hashTags.add(entity.getText());
            }
            try {
                RedisUtils.addNewTweet(status.getCreatedAt().getTime(), status.getLang(), status.getText(), tweet_lat, tweet_long, hashTags);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            long min = status.getCreatedAt().getTime()/60000;
            String country = status.getPlace().getCountryCode();
            if(!countsPerMinute.containsKey(min))
                countsPerMinute.put(min, new HashMap<String, Integer>());
            if(!countsPerMinute.get(min).containsKey(country))
                countsPerMinute.get(min).put(country,0);
            countsPerMinute.get(min).put(country, countsPerMinute.get(min).get(country)+1);

            if(countsPerMinute.containsKey(min-3)){

                String presentID = UUID.randomUUID().toString();
                System.out.println(presentID);
                HashMap<String, Integer> counts = countsPerMinute.remove(min-3);
                RedisUtils.refreshLastMinuteCountryCounts(min - 3, presentID, counts);
            }

        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        }

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        public void onScrubGeo(long userId, long upToStatusId) {
        }

        public void onStallWarning(StallWarning warning) {
        }

        public void onException(Exception ex) {
        }
    };

    public static void main(String[] args) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("FZ29JqPOkaQ4xe7pOea68R393")
                .setOAuthConsumerSecret("5zh4FThtjt3HiRv1Fp8UQFTaikMIeAJOdmLWM1ZVuFC2u3HvtF")
                .setOAuthAccessToken("152924084-EmWUxjOo2lp9DcZUNK5FIiYeV57xside0eRkSzhl")
                .setOAuthAccessTokenSecret("jXjG4h9fp4PMO0cyPsitVsTA8lymkM6MKhNePDLtTwkwp");



        twitter4j.TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);

        FilterQuery filter = new FilterQuery();
        filter.locations(boundingBox);


        twitterStream.filter(filter);

    }

}
