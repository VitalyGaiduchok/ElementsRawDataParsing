package Servlet;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
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

        resp.getWriter().append("Response : " + bodyStr);
        ResponseRawData rawD = new Gson().fromJson(bodyStr, ResponseRawData.class);
        
        resp.getWriter().append("\nResponse : " + rawD.rawData.Metadata.toString());
        Map<String, String> vars = new HashMap<String, String>();
        for (Variable var : rawD.rawData.Metadata.variables) {
            if (StringUtils.isBlank(var.objectType)) { continue; }
            vars.put(var.name, var.objectType);
        }
        resp.getWriter().append("\nResponse : " + vars.toString());

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
    
    class ResponseRawData {
        RawData rawData;
    }

    class RawData {
        FlowMetadata Metadata;
    }

    class FlowMetadata {
        List<Variable> variables;
    }

    class Variable {
        String name;
        String objectType;
        String dataType;
    }
}

