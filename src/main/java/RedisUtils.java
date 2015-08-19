import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RedisUtils {

    static ArrayList<String> isoCountries = new ArrayList<String>(Arrays.asList("AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI", "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI", "HR", "CU", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "AN", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SK", "SI", "SB", "SO", "ZA", "GS", "ES", "LK", "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY", "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW"));


    static Object lock = new Object();
    public static void refreshLastMinuteCountryCounts(long minute, String uuid, HashMap<String, Integer> counts){
        Database.insertCountryCounts(minute, counts);
        synchronized(lock) {
            Jedis jedis = new Jedis("localhost");
            jedis.set("last_minute_uuid", uuid);
            jedis.set("last_minute_data", (new JSONObject(counts)).toString());
            jedis.close();
        }
    }

    public static ArrayList<String> getLastMinuteCountryCounts() throws JSONException {
        ArrayList<String> data = new ArrayList<String>();

        synchronized (lock) {
            Jedis jedis = new Jedis("localhost");
            data.add(jedis.get("last_minute_uuid"));
            JSONObject tmp = new JSONObject(jedis.get("last_minute_data"));
            for(String cc : isoCountries){
                if(tmp.has(cc))
                    continue;
                tmp.put(cc, 0);
            }
            data.add(tmp.toString());
            jedis.close();
        }

        return data;
    }

    public static synchronized void addNewTweet(long time, String lang, String text, double tweet_lat, double tweet_long, ArrayList<String> hashTags) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("time", time);
        object.put("lang", lang);
        object.put("text", text);
        object.put("lat", tweet_lat);
        object.put("long", tweet_long);
        Jedis jedis = new Jedis("localhost");

        long length = jedis.llen("tweets_queue");
        if(length == 100000) {
            jedis.lpop("tweets_queue");
        }
        jedis.rpush("tweets_queue", object.toString());
        jedis.close();
    }

    public static ArrayList<String> getLast100Tweets() {
        Jedis jedis = new Jedis("localhost");
        List<String> output = jedis.lrange("tweets_queue", -100, -1);
        return new ArrayList<String>(output);
    }
}
