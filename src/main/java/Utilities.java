import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class Utilities {

    public static ArrayList<String> get_city_and_country(double latitude, double longitude) throws IOException, JSONException {
        String url  = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+","+longitude;
        String jsonString = convertStreamToString(new URL(url).openStream());
        JSONObject object = new JSONObject(jsonString);
        System.out.println(((JSONArray)object.get("results")).get(0));
        return null;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, JSONException {
        get_city_and_country(40.705597499999996, -73.977717);
    }

}
