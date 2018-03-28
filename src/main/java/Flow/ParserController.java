package Flow;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "ParseController",
        urlPatterns = {"/flowParse"}
    )
public class ParserController extends HttpServlet {
        
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader body = req.getReader();
        String bodyLines = "";
        String bodyStr = "";
        while ((bodyLines = body.readLine())!=null) {
            bodyStr+=bodyLines;
        }
        System.out.println(bodyStr);

        ResponseRawData rawD = new Gson().fromJson(bodyStr, ResponseRawData.class);
        if (rawD == null) {
            Map<String, String> respMap = new HashMap<String, String>();
            respMap.put("error", "Bad request body.");
            resp.getWriter().append(new Gson().toJson(respMap));
            resp.setStatus(403);
            return; 
        }
        resp.getWriter().append(new Gson().toJson(Parser.parse(bodyStr)));
        resp.setStatus(200);

    }
    
    public class ResponseRawData {
        public ArrayList<RawData> rawData;
    }

    public class RawData {
        public FlowMetadata Metadata;
    }

    public class FlowMetadata {
        public ArrayList<Object> actionCalls;
        public ArrayList<Object> assignments;
        public ArrayList<Object> decisions;
        public ArrayList<Object> formulas;
        public ArrayList<Object> processMetadataValues;
        public ArrayList<Object> recordCreates;
        public ArrayList<Object> recordLookups;
        public ArrayList<Object> recordUpdates;
        public ArrayList<Variable> variables;
    }

    public class Variable {
        public String name;
        public String objectType;
        public String dataType;
    }
    
}

