package Servlet;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import launch.FlowParser;

@WebServlet(
        name = "ParseController",
        urlPatterns = {"/flowParse"}
    )
public class ParserController extends HttpServlet {

//    private String token = "ASDYQW127BFYWEBCAQWUQWNCE38ASDNCNUEO12";
    
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        ServletOutputStream out = resp.getOutputStream();
//        System.out.println("bodyStr");
//        out.write("hello heroku".getBytes());
//        out.flush();
//        out.close();
//    }
        
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

        Set<String> result = new HashSet<>();
        for (RawData rd : rawD.rawData) {
            HashMap<String, String> vars = new HashMap<String, String>();
            for (Variable var : rd.Metadata.variables) {
                vars.put(var.name, var.objectType);
            }
            vars.forEach((k,v)->{
                System.out.println("\nkey: " + k + ", value: " + v);
            }); 
            result.addAll(returnAllFields(rd.Metadata, vars));

        }
        resp.getWriter().append(new Gson().toJson(result));
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
    
    public static String returnJsonString() throws IOException {
        
//        File f = new File("FlowJson.json");
        File f = new File("RealFlow.json");
//        File f = new File("volarisJson.json");
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String bodyLines = null;
        String bodyStr = "";
        while ((bodyLines = br.readLine())!=null) {
            bodyStr+=bodyLines;
        }
        br.close();
        fr.close();
        return bodyStr;
    }
    
    public static Set<String> returnAllFields(FlowMetadata metadata, HashMap<String, String> vars) {
        Set<String> res = new HashSet<>();
        res.addAll(FlowParser.getActionCallsFU(metadata, vars));
        res.addAll(FlowParser.getAssignmentsFU(metadata, vars));
        res.addAll(FlowParser.getDecisionsFU(metadata, vars));
        res.addAll(FlowParser.getRecordCreatesFU(metadata, vars));
        res.addAll(FlowParser.getRecordLookupsFU(metadata, vars));
        res.addAll(FlowParser.getRecordUpdatesFU(metadata, vars));
        res.addAll(FlowParser.getFlowFormulasFU(metadata, vars));
        res.addAll(FlowParser.getProcessMetadataValuesFromMDFU(metadata, vars));
        res.addAll(FlowParser.setOfParsedChatterStringValues(metadata, vars));
        System.out.println("  this res:" + res.toString());
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        return res;
    }
    
}

