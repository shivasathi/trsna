import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name="testServlet", urlPatterns={"/counts"} )

public class TweetCountsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject json = new JSONObject();
        try {
            ArrayList<String> data = RedisUtils.getLastMinuteCountryCounts();
            json.put("id", data.get(0));
            json.put("object", data.get(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html");
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");
        PrintWriter out = response.getWriter();
        String callback = request.getParameter("callback");
        out.println(callback+"("+json.toString()+")");
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }
}
