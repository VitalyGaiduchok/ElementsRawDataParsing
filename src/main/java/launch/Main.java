package launch;

import Servlet.HelloServlet.FlowMetadata;
import Servlet.HelloServlet.ResponseRawData;
import Servlet.HelloServlet.Variable;
import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

//import org.apache.catalina.WebResourceRoot;
//import org.apache.catalina.core.StandardContext;
//import org.apache.catalina.startup.Tomcat;
//import org.apache.catalina.webresources.DirResourceSet;
//import org.apache.catalina.webresources.StandardRoot;

public class Main {

    public static Set<Object> getActionCallsFU(FlowMetadata md, ArrayList<Variable> vars) {
        List<Object> actionCalls = md.actionCalls;
        Set<Object> res = new HashSet<Object>();
        if (actionCalls.isEmpty()) { return res; }
        String rd = "";
        Set<String> elementReferences = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : actionCalls) {
            Map<String, Object> item = (Map<String, Object>) obj;
            List<Object> processMetadataValue = (ArrayList<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                for (Object pmv : processMetadataValue) {
                    if (((HashMap<String, Object>) pmv).get("value") == null) { continue; }
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((HashMap<String, Object>) pmv).get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            
            List<Object> inputParameters = (ArrayList<Object>) item.get("inputParameters");
            if (inputParameters != null) {
                for (Object pmv : inputParameters) {
                    if (((Map<String, Object>) pmv).get("value") == null) { continue; }
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) pmv).get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                }
            }
            
        }
        for(String eR : elementReferences) {
            for (Variable var : vars) {
                if (eR.startsWith(var.name)) { 
                    eR = eR.replace(var.name, var.objectType);
                    res.add(eR);
                    break;
                }
            }
        }
//        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<Object> getAssignmentsFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> assignments = (List<Object>) md.get("assignments");
        Set<Object> res = new HashSet<Object>();
        if (assignments.isEmpty()) { return res; }
        String rd = "";
        List<Object> assignmentItems = new ArrayList<Object>();
        for (Object obj : assignments) { 
            Map<String, Object> item = (Map<String, Object>) obj;
            assignmentItems.add(item.get("assignmentItems"));
        }
        List<Object> allItems = new ArrayList<Object>();
        for (Object obj : assignmentItems) { 
            for (Object item : (List<Object>) obj) {
                allItems.add(item);
            }
        }
        Set<String> elementReferences = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : allItems) {
            Map<String, Object> item = (Map<String, Object>) obj;
            List<Object> processMetadataValue = (List<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                for (Object pmv : processMetadataValue) {
                    if (((Map<String, Object>) pmv).get("value") == null) continue;
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) pmv).get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            if (item.get("value") == null) { continue; }
            ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(item.get("value")), ItemValue.class);
            if (iValue != null) {
                if (StringUtils.isBlank(iValue.stringValue)) {
                    stringValues.add(iValue.stringValue);
                }
                if (StringUtils.isBlank(iValue.elementReference)) {
                    elementReferences.add(iValue.elementReference);
                }
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key)) { 
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<Object> getDecisionsFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> decisions = (List<Object>) md.get("decisions");
        Set<Object> res = new HashSet<Object>();
        if (decisions.isEmpty()) { return res; }
        String rd = "";
        List<Object> allRules = new ArrayList<Object>();
        for (Object obj : decisions) {
            
            Map<String, Object> item = (Map<String, Object>) obj;
            List<Object> rules = (List<Object>) item.get("rules");
            if (rules == null) { continue; }
            for (Object rule : rules) {
                allRules.add(rule); 
            }
        }
        if (allRules.isEmpty()) { return res; }
        
        Set<String> elementReferences = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        for (Object rule : allRules) {
            List<Object> conditions = (List<Object>) ((Map<String, Object>) rule).get("conditions");
            if (conditions == null) { continue; }
            
            for (Object condition : conditions) {
                Map<String, Object> conditionMap = (Map<String, Object>) condition;
                rd = "" + conditionMap.get("leftValueReference");
                elementReferences.add(rd);
                ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(conditionMap.get("rightValue")), ItemValue.class);
                if (iValue != null) {
                    if (StringUtils.isBlank(iValue.stringValue)) {
                        stringValues.add(iValue.stringValue);
                    }
                    if (StringUtils.isBlank(iValue.elementReference)) {
                        elementReferences.add(iValue.elementReference);
                    }
                }            
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key)) { 
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<Object> getRecordUpdatesFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> recordUpdates = (List<Object>) md.get("recordUpdates");
        Set<Object> res = new HashSet<Object>();
        if (recordUpdates.isEmpty()) { return res; }
        String recordUpdateRD = "";
        Set<String> elementReferences = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : recordUpdates) {
            Map<String, Object> item = (Map<String, Object>) obj;
            String objectName = "" + item.get("object");
            
            List<Object> filters = (List<Object> ) item.get("filters");
            if (filters != null) {
                for (Object filter : filters) {
                    Map<String, Object> iFilter = (Map<String, Object>) filter;
                    recordUpdateRD = objectName + "." + iFilter.get("field");
                    res.add(recordUpdateRD);
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(iFilter.get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                }
            }
            
            List<Object> inputAssignments = (List<Object>) item.get("inputAssignments");
            if (inputAssignments != null) {
                for (Object inputAssignment : inputAssignments) {
                    Map<String, Object> iAssignment = (Map<String, Object>) inputAssignment;
                    recordUpdateRD = objectName + "." + iAssignment.get("field");
                    res.add(recordUpdateRD);
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(iAssignment.get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            
            List<Object> processMetadataValue = (List<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                for (Object pmv : processMetadataValue) {
                    if (((Map<String, Object>) pmv).get("value") == null) continue;
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) pmv).get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key)) { 
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<Object> getRecordLookupsFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> recordLookups = (List<Object>) md.get("recordLookups");
        Set<Object> res = new HashSet<Object>();
        if (recordLookups.isEmpty()) { return res; }
        String recordLookupRD = "";
        Set<String> elementReferences = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : recordLookups) {
            Map<String, Object> item = (Map<String, Object>) obj;
            String objectName = "" + item.get("object");
            List<Object> filters = (List<Object> ) item.get("filters");
            if (filters != null) {
                for (Object filter : filters) {
                    Map<String, Object> iFilter = (Map<String, Object>) filter;
                    recordLookupRD = objectName + "." + iFilter.get("field");
                    res.add(recordLookupRD);
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(iFilter.get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                }
            }
            
            List<Object> outputAssignments = (List<Object> ) item.get("outputAssignments");
            if (outputAssignments != null) {
                for (Object outputAssignment : outputAssignments) {
                    Map<String, Object> outputAssignmentMap = (Map<String, Object>) outputAssignment;
                    recordLookupRD = objectName + "." + outputAssignmentMap.get("field");
                    res.add(recordLookupRD);
                }
            }
            List<Object> queriedFields = (List<Object> ) item.get("queriedFields");
            if (queriedFields != null) {
                for (Object field : queriedFields) {
                    res.add(objectName + "." + field);
                }
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key)) { 
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<Object> getRecordCreatesFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> recordCreates = (List<Object>) md.get("recordCreates");
        Set<Object> res = new HashSet<Object>();
        if (recordCreates.isEmpty()) { return res; }
        String recordCreateRD = "";
        Set<String> elementReferences = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : recordCreates) {
            Map<String, Object> item = (Map<String, Object>) obj;
            String objectName = "" + item.get("object");
            List<Object> inputAssignments = (List<Object>) item.get("inputAssignments");
            if (inputAssignments != null) {
                for (Object inputAssignment : inputAssignments) {
                    recordCreateRD = objectName + "." + ((Map<String, Object>) inputAssignment).get("field");
                    res.add(recordCreateRD);
                    if (((Map<String, Object>) inputAssignment).get("value") == null) { continue; }
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) inputAssignment).get("value")), ItemValue.class);
                    if (StringUtils.isBlank(iValue.stringValue)) {
                        stringValues.add(iValue.stringValue);
                    }
                    if (StringUtils.isBlank(iValue.elementReference)) {
                        elementReferences.add(iValue.elementReference);
                    }
                } 
            }
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key)) { 
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<Object> getFlowFormulasFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> formulas = (ArrayList<Object>) md.get("formulas");
        Set<Object> res = new HashSet<Object>();
        if (formulas.isEmpty()) { return res; }
        
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : formulas) {
            Map<String, Object> item = (Map<String, Object>) obj;
            String expression = "" + item.get("expression");
            if (expression.contains(".")) { 
                stringValues.add(expression);
            }
            List<Object> objs = (List<Object>) item.get("processMetadataValues");
            if (objs != null) {
                for (Object o : objs) {
                    Map<String, Object> oMap = (Map<String, Object>) o;
                    if ("" + oMap.get("name") == "originalFormula") {
                        expression = "" + ((Map<String, Object>) oMap.get("value")).get("stringValue");
                        stringValues.add(expression);
                    }
                }
            }
        }
        return setOfParsedStringValues(res, stringValues, vars);
    }
    
    public static Set<Object> getProcessMetadataValuesFromMDFU(Map<String, Object> md, HashMap<String, String> vars) {
        List<Object> processMetadataValues = (ArrayList<Object>) md.get("processMetadataValues");
        Set<Object> res = new HashSet<Object>();
        if (processMetadataValues.isEmpty()) { return res; }
        
        Set<String> stringValues = new HashSet<String>();
        Set<String> elementReferences = new HashSet<String>();
        for (Object obj : processMetadataValues) {
            Map<String, Object> item = (Map<String, Object>) obj;
            if (((Map<String, Object>) item).get("value") == null) { continue; }
            ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) item).get("value")), ItemValue.class);
            if (StringUtils.isBlank(iValue.stringValue)) {
                stringValues.add(iValue.stringValue);
            }
            if (StringUtils.isBlank(iValue.elementReference)) {
                elementReferences.add(iValue.elementReference);
            }
        }
        return setOfParsedStringValues(res, stringValues, vars);
    }

    public static Set<Object> setOfParsedStringValues(Set<Object> res, Set<String> stringValues, HashMap<String, String> vars) {
        
        //For stringValue
        String firstSymbol = "[a-zA-Z]" + "([_]{0,1}[a-zA-Z0-9]+)*";
        String stringValueRegex = "[{]!(" + firstSymbol + ")" +  
        "([.]{1}" + firstSymbol + "([_]{2}[rR]){0,1}){0,8}([.]{1}" + firstSymbol + "([_]{2}[crCR]){0,1}){0,1}[}]";
        Pattern p = Pattern.compile(stringValueRegex);
        for (String sValue : stringValues) {
            Matcher m = p.matcher(sValue);
            while (m.find()) {
                System.out.println(m.group() + ", length : " + m.group().length() + ", indexOf: " + m.group().indexOf("."));
                if (m.group().indexOf(".") > 0) {
                    String keyMatch = m.group().substring(2, m.group().indexOf("."));
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v));
                            }

                    });
                } else {
                    String keyMatch =  m.group().substring(2, m.group().length()-1);
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v));
                            }
                    });
                }
            }
        }
        return res;
    }
    
    public static Set<Object> setOfParsedChatterStringValues(Set<Object> res, Set<String> stringValues, HashMap<String, String> vars) {
        //For chatter message
        String swlanmtoubl = "[a-zA-Z]" + "([_]{0,1}[a-zA-Z0-9]+)*";
        String middleValueRegex = "[{]!((" + swlanmtoubl + ")|(\\u005B" + swlanmtoubl + "([_]{2}[crCR]){0,1}\\u005D))" +  
        "([.]{1}" + swlanmtoubl + "([_]{2}[rR]){0,1}){0,8}([.]{1}" + swlanmtoubl + "([_]{2}[crCR]){0,1}){0,1}[}]";
        Pattern p = Pattern.compile(middleValueRegex);
        List<String> strsList = new ArrayList<String>();
        for (String sValue : stringValues) {
            Matcher m = p.matcher(sValue);
            while (m.find()) {
                System.out.println(m.group() + ", length : " + m.group().length() + ", indexOf: " + m.group().indexOf("."));
                if (m.group().indexOf(".") > 0) {
                    Integer LBracketIndex = m.group().indexOf("].");
                    String keyMatch = LBracketIndex < 0 ? m.group().substring(2, m.group().indexOf(".")) : "";
                    String valueMatch = LBracketIndex > 0 ? m.group().substring(3, LBracketIndex) : "";
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v));
                            }
                            if(!valueMatch.equals("") && valueMatch.equals(v)){
        //                        System.out.println("value v: " + v);
                                res.add(m.group().replace("[", "").replace("]", ""));
                            }

                    });
                } else {
                    Integer LBracketIndex = m.group().indexOf("]");
                    String keyMatch = LBracketIndex < 0 ? m.group().substring(2, m.group().length()-1) : "";
                    String valueMatch = LBracketIndex > 0 ? m.group().substring(3, m.group().length()-2) : "";
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v));
                            }
                            if(!valueMatch.equals("") && valueMatch.equals(v)){
        //                        System.out.println("value v: " + v);
                                res.add(m.group().replace("[", "").replace("]", ""));
                            }
                    });
                }
            }
        }
        return res;
    }
    
    public class Var {
        String name;
        String objectType;
    }
    
    public class ItemValue {
        public String elementReference;
        public String stringValue;
    }
    
    
    public static void main(String[] args) throws Exception {
        String expression ="{!SecondValid_2.Name} .{!NotValid_2,Name }{!NotValid_2,Name {!SecondValid_7.Name} }{! NotValid_1.Name} {!NotValid_3.name__r.__c} {![FirstValid__c].Name__r.createdBy.Id}{!{!{!Valid_4.Name__c}}{!SecondValid_2.Name}{![ThirdValid].createdBy.Name}";
        expression = "{!SObject.Name} + .{![Account__c].Name}...{![Account2].Name}.. ...{!SObject} {![asddsa__c]} ..{!dsaasd} {!dsaasd2__c}";
        
        HashMap<String, String> vars = new HashMap<>();
        vars.put("SObject", "Account");
        vars.put("SObject2", "Account2");
        vars.put("asddsa", "asddsa__c");
        vars.put("dsaasd", "dsaasd__c");
        vars.put("dsaasd2", "dsaasd2__c");
        
        String abc = "";
        HashMap<String, Object> flow;
//        flow = (HashMap<String, Object>) new Gson().fromJson(abc, (HashMap<String, Object>).class);
        Set<String> stringValues = new HashSet<>();
        stringValues.add(expression);
        
        String body = "{ \"rawData\": { \"Metadata\": { \"variables\": [{ \"asd\" : \"zxc\"}, { \"123\" : \"456\"}] } }, \"token\": \"ASDYQW127BFYWEBCAQWUQWNCE38ASDNCNUEO12\" }";
        ResponseRawData rawD = new Gson().fromJson(body, ResponseRawData.class);
        System.out.println(rawD.toString());
        
    }
    
}