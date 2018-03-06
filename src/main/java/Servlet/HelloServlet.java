package Servlet;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import launch.Main;
import static launch.Main.getActionCallsFU;
import static launch.Main.getDecisionsFU;
import static launch.Main.getFlowFormulasFU;
import static launch.Main.getProcessMetadataValuesFromMDFU;
import static launch.Main.getRecordCreatesFU;
import static launch.Main.getRecordLookupsFU;
import static launch.Main.getRecordUpdatesFU;
import static launch.Main.setOfParsedChatterStringValues;
import org.apache.commons.lang3.StringUtils;

@WebServlet(
        name = "MyServlet",
        urlPatterns = {"/hello"}
    )
public class HelloServlet extends HttpServlet {

    private String token = "ASDYQW127BFYWEBCAQWUQWNCE38ASDNCNUEO12";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        System.out.println("bodyStr");
        out.write("hello heroku".getBytes());
        out.flush();
        out.close();
    }
        
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader body = req.getReader();
        String bodyLines = "";
        String bodyStr = "";
        while ((bodyLines = body.readLine())!=null) {
            bodyStr+=bodyLines;
        }
        System.out.println(bodyStr);

//        resp.getWriter().append("Response : " + bodyStr);
        ResponseRawData rawD = new Gson().fromJson(bodyStr, ResponseRawData.class);
        
//        resp.getWriter().append("\nResponse : " + rawD.rawData.Metadata.toString());
//        HashMap<String, String> vars = new HashMap<String, String>();
//        for (Variable var : rawD.rawData.Metadata.variables) {
//            if (StringUtils.isBlank(var.objectType)) { continue; }
//            vars.put(var.name, var.objectType);
//        }
        Set<String> result = new HashSet<>();
//        resp.getWriter().append(new Gson().toJson(returnAllFields(rawD.rawData, vars)));
//        resp.getWriter().append("\nResponse : " + vars.toString());
        Set<String> superReturn = new HashSet<>();
        for (RawData rd : rawD.rawData) {
            HashMap<String, String> vars = new HashMap<String, String>();
            for (Variable var : rd.Metadata.variables) {
                vars.put(var.name, var.objectType);
            }
            vars.forEach((k,v)->{
                    System.out.println("\nkey: " + k + ", value: " + v);
            }); 
            superReturn.addAll(returnAllFields(rd.Metadata, vars));

        }
        resp.getWriter().append(new Gson().toJson(superReturn));

//        String disterString = "{![Account].AccountNumber + '  ' + [Account].Name} Account.Name Account.notName {![Account].JigsawCompanyId2} + "
//                + "{![Account].Name1} + [Account.Name] + Account. AccountNumber + [Account].Name  {![Account].Name}sd {zxc} [Account]. namz + Account.names + "
//                + "{![Account].Name3} {![Account].Name + \'1\'} [Account].Name $User.FirstName \'$User.FirstName\' z$User.FirstName $User.SSS 1{![Account].Name}zxccxz 2"
//                + "{![Account].Name} {![Account].Name}3 {! [Account].Name} 4 {!}{![Account].Name} {![}{![Account].Name} {![{![Account].Name} {![]}{![Account].Name} "
//                + "{![Account]}{![Account].Name}{![Account]}{![Account].Name}{![Account]}{![Account].Name} {![Account].Name}{!myVariable_current} {![Account]}{![Account].Name}"
//                + "{!myVariable_current} {!$User.FirstName}{![Account]}{![Account]}{![Account]} {!$User.FirstName}{![Account]}{![Account]} }{!{![Account].Name} "
//                + "{![Account].Fields['Name']} {![Account].Fields['Name', 'AccountNumber']} {![A__c].Name} {![Account].Name} {! myVariable_current.Name} {!myVariable_current.Name } "
//                + "{!User.FirstName} {![User].FirstName__c} [User.FirstName] [User].FirstName {!$User.FirstName} {!$[User].FirstName} \n {![User].FirstName}";
//      
//        String expression ="{!SecondValid_2.Name} .{!NotValid_2,Name }{!NotValid_2,Name {!SecondValid_7.Name} }{! NotValid_1.Name} {!NotValid_3.name__r.__c} {![FirstValid__c].Name__r.createdBy.Id}{!{!{!Valid_4.Name__c}}{!SecondValid_2.Name}{![ThirdValid].createdBy.Name}";
//        expression = "{!SObject.Name} + .{![Account__c].Name}...{![Account2].Name}.. ...{!SObject} {![asddsa__c]} ..{!dsaasd} {!dsaasd2__c}";
        
//        HashMap<String, String> vars = new HashMap<>();
//        vars.put("SObject", "Account");
//        vars.put("SObject2", "Account2");
//        vars.put("asddsa", "asddsa__c");
//        vars.put("dsaasd", "dsaasd__c");
//        vars.put("dsaasd2", "dsaasd2__c");
        
//            EmailSender.sendEmail(emailsData.emails);
//            resp.getWriter().append(
//                new Gson().toJson(
//                    Main.setOfParsedStringValues(new HashSet<Object>(), stringValues, vars)
//                ) 
//            );
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
        res.addAll(Main.getActionCallsFU(metadata, vars));
        res.addAll(Main.getAssignmentsFU(metadata, vars));
        res.addAll(Main.getDecisionsFU(metadata, vars));
        res.addAll(Main.getRecordCreatesFU(metadata, vars));
        res.addAll(Main.getRecordLookupsFU(metadata, vars));
        res.addAll(Main.getRecordUpdatesFU(metadata, vars));
        res.addAll(Main.getFlowFormulasFU(metadata, vars));
        res.addAll(Main.getProcessMetadataValuesFromMDFU(metadata, vars));
        res.addAll(Main.setOfParsedChatterStringValues(metadata, vars));
        return res;
    }
    
}

