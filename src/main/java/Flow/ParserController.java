package Flow;

import Flow.Parser.ResponseRawData;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

@WebServlet(
        name = "ParseController",
        urlPatterns = {"/flowParse"}
    )
public class ParserController extends HttpServlet {
        
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader body = req.getReader();
        String bodyLines = "";
        StringBuffer bodyStr = new StringBuffer();
        while ((bodyLines = body.readLine())!=null) {
            bodyStr.append(bodyLines);
        }
        System.out.println(bodyStr.toString());

        ResponseRawData rawD = new Gson().fromJson(bodyStr.toString(), ResponseRawData.class);
        if (rawD == null) {
            Map<String, String> respMap = new HashMap<String, String>();
            respMap.put("error", "Bad request body.");
            resp.getWriter().append(new Gson().toJson(respMap));
            resp.setStatus(403);
            return; 
        }
        resp.getWriter().append(new Gson().toJson(Parser.parse(bodyStr.toString())));
        resp.setStatus(200);

    }
    
}

