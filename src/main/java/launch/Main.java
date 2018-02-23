package launch;

import Servlet.HelloServlet;
import Servlet.HelloServlet.FlowMetadata;
import Servlet.HelloServlet.RawData;
import Servlet.HelloServlet.ResponseRawData;
import Servlet.HelloServlet.Variable;
import com.google.gson.Gson;
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

    public static Set<String> getActionCallsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> actionCalls = md.actionCalls;
        Set<String> res = new HashSet<>();
        if (actionCalls.isEmpty()) { return res; }
        String rd = "";
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        for (Object obj : actionCalls) {
            Map<String, Object> item = (Map<String, Object>) obj;
            if (item.get("actionType").toString().equals("chatterPost")) { continue; }
            List<Object> processMetadataValue = (ArrayList<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                for (Object pmv : processMetadataValue) {
                    HashMap<String, Object> pmvMap = (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmv), HashMap.class);
                    if (pmvMap.get("value") == null) { continue; }
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(pmvMap.get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            
            List<Object> inputParameters = (ArrayList<Object>) item.get("inputParameters");
            if (inputParameters != null) {
                for (Object pmv : inputParameters) {
                    HashMap<String, Object> pmvMap = (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmv), HashMap.class);
                    if (pmvMap.get("value") == null) { continue; }
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(pmvMap.get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                }
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                    break;
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getAssignmentsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> assignments = (List<Object>) md.assignments;
        Set<String> res = new HashSet<>();
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
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            if (item.get("value") == null) { continue; }
            ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(item.get("value")), ItemValue.class);
            if (iValue != null) {
                if (!StringUtils.isBlank(iValue.stringValue)) {
                    stringValues.add(iValue.stringValue);
                }
                if (!StringUtils.isBlank(iValue.elementReference)) {
                    elementReferences.add(iValue.elementReference);
                }
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                    break;
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getDecisionsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> decisions = (List<Object>) md.decisions;
        Set<String> res = new HashSet<>();
        if (decisions.isEmpty()) { return res; }
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
        String rd = "";
        for (Object rule : allRules) {
            List<Object> conditions = (List<Object>) ((Map<String, Object>) rule).get("conditions");
            if (conditions == null) { continue; }
            
            for (Object condition : conditions) {
                Map<String, Object> conditionMap = (Map<String, Object>) condition;
                rd = conditionMap.get("leftValueReference").toString();
                elementReferences.add(rd);
                ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(conditionMap.get("rightValue")), ItemValue.class);
                if (iValue != null) {
                    if (!StringUtils.isBlank(iValue.stringValue)) {
                        stringValues.add(iValue.stringValue);
                    }
                    if (!StringUtils.isBlank(iValue.elementReference)) {
                        elementReferences.add(iValue.elementReference);
                    }
                }            
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getRecordUpdatesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordUpdates = md.recordUpdates;
        Set<String> res = new HashSet<>();
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
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
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
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
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
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                } 
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getRecordLookupsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordLookups = (List<Object>) md.recordLookups;
        Set<String> res = new HashSet<>();
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
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
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
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getRecordCreatesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordCreates = (List<Object>) md.recordCreates;
        Set<String> res = new HashSet<>();
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
                    if (!StringUtils.isBlank(iValue.stringValue)) {
                        stringValues.add(iValue.stringValue);
                    }
                    if (!StringUtils.isBlank(iValue.elementReference)) {
                        elementReferences.add(iValue.elementReference);
                    }
                } 
            }
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                }
            }
        }
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getFlowFormulasFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> formulas = (ArrayList<Object>) md.formulas;
        Set<String> res = new HashSet<>();
        if (formulas.isEmpty()) { return res; }
        
        Set<String> stringValues = new HashSet<String>();
        for (Object obj : formulas) {
            Map<String, Object> item = (Map<String, Object>) obj;
            String expression = "" + item.get("expression");
            stringValues.add(expression);
//            List<Object> objs = (List<Object>) item.get("processMetadataValues");
//            if (objs != null) {
//                for (Object o : objs) {
//                    Map<String, Object> oMap = (Map<String, Object>) o;
//                    if ("" + oMap.get("name") == "originalFormula") {
//                        expression = "" + ((Map<String, Object>) oMap.get("value")).get("stringValue");
//                        stringValues.add(expression);
//                    }
//                }
//            }
        }
        return setOfParsedFormulas(res, stringValues, vars);
    }
    
    public static Set<String> getProcessMetadataValuesFromMDFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> processMetadataValues = (ArrayList<Object>) md.processMetadataValues;
        Set<String> res = new HashSet<>();
        if (processMetadataValues.isEmpty()) { return res; }
        
        Set<String> stringValues = new HashSet<String>();
        Set<String> elementReferences = new HashSet<String>();
        for (Object obj : processMetadataValues) {
            Map<String, Object> item = (Map<String, Object>) obj;
            if (((Map<String, Object>) item).get("value") == null) { continue; }
            ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) item).get("value")), ItemValue.class);
            if (!StringUtils.isBlank(iValue.stringValue)) {
                stringValues.add(iValue.stringValue);
            }
            if (!StringUtils.isBlank(iValue.elementReference)) {
                elementReferences.add(iValue.elementReference);
            }
        }
        return setOfParsedStringValues(res, stringValues, vars);
    }

    public static Set<String> setOfParsedStringValues(Set<String> res, Set<String> stringValues, HashMap<String, String> vars) {
        System.out.println("HERE WE ARE 411: ");
        System.out.println("res: " + res.toString());
        System.out.println("stringValues: " + stringValues.toString());
        System.out.println("vars: " + vars);
        //For stringValue
//        String firstSymbol = "[a-zA-Z]" + "([_]?[a-zA-Z0-9]+)*";
//        String stringValueRegex = "[{]!(" + firstSymbol + ")" +  
//                                  "([.]{1}" + firstSymbol + "([_]{2}[rR])?){0,8}([.]{1}" + firstSymbol + "([_]{2}[crCR])?)?[}]";
        String startOfExpression = "[{]!";
        String swlanmtoubl = "[a-zA-Z]" + "([_]?[a-zA-Z0-9]+)*";
        String caseForObject1 = swlanmtoubl + "([_]{2}" + swlanmtoubl + "([_]{2}[crCR])?)?";
        String caseForObject2 = "(\\u005B" + caseForObject1 + "([_]{2}[crCR])?\\u005D)";
        String caseForField1 = "([.]{1}" + swlanmtoubl + "([_]{2}" + swlanmtoubl  + ")?([_]{2}[rR])?){0,8}";
        String caseForField2 = "([.]{1}" + swlanmtoubl + "([_]{2}" + swlanmtoubl  + ")?([_]{2}[crCR])?)?";
        String exprssionForCasses = "((" + caseForObject1  + ")|(" + caseForObject2 + "))";
        String endOfExpression = "[}]";
//        System.out.println(startOfExpression + exprssionForCasses + caseForField1 + caseForField2 + endOfExpression);
        String stringValueRegex = "((" + startOfExpression + exprssionForCasses + caseForField1 + caseForField2 + endOfExpression + ")|($Setup." + exprssionForCasses + caseForField1 + caseForField2 + "))";
        Pattern p = Pattern.compile(stringValueRegex);
        for (String sValue : stringValues) {
            Matcher m = p.matcher(sValue);
            while (m.find()) {
                System.out.println("LINE 432: " + m.group() + ", length : " + m.group().length() + ", indexOf: " + m.group().indexOf("."));
                if (m.group().indexOf(".") > 0) {
                    String keyMatch = m.group().substring(2, m.group().indexOf("."));
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v).replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }

                    });
                } else {
                    String keyMatch =  m.group().substring(2, m.group().length()-1);
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v).replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }
                    });
                }
            }
        }
        return res;
    }
        
    public static Set<String> setOfParsedFormulas(Set<String> res, Set<String> stringValues, HashMap<String, String> vars) {
        System.out.println("HERE WE ARE 411: ");
        System.out.println("res: " + res.toString());
        System.out.println("stringValues: " + stringValues.toString());
        System.out.println("vars: " + vars);
        //For stringValue
//        String firstSymbol = "[a-zA-Z]" + "([_]?[a-zA-Z0-9]+)*";
//        String stringValueRegex = "[{]!(" + firstSymbol + ")" +  
//                                  "([.]{1}" + firstSymbol + "([_]{2}[rR])?){0,8}([.]{1}" + firstSymbol + "([_]{2}[crCR])?)?[}]";
        String startOfExpression = "[{]!";
        String swlanmtoubl = "[a-zA-Z]" + "([_]?[a-zA-Z0-9]+)*";
        String caseForObject1 = swlanmtoubl + "([_]{2}" + swlanmtoubl + "([_]{2}[crCR])?)?";
        String caseForObject2 = "(\\u005B" + caseForObject1 + "([_]{2}[crCR])?\\u005D)";
        String caseForField1 = "([.]{1}" + swlanmtoubl + "([_]{2}" + swlanmtoubl  + ")?([_]{2}[rR])?){0,8}";
        String caseForField2 = "([.]{1}" + swlanmtoubl + "([_]{2}" + swlanmtoubl  + ")?([_]{2}[crCR])?)?";
        String exprssionForCasses = "((" + caseForObject1  + ")|(" + caseForObject2 + "))";
        String endOfExpression = "[}]";
//        System.out.println(startOfExpression + exprssionForCasses + caseForField1 + caseForField2 + endOfExpression);
        String stringValueRegex = "((" + startOfExpression + exprssionForCasses + caseForField1 + caseForField2 + endOfExpression + ")|($Setup." + exprssionForCasses + caseForField1 + caseForField2 + "))";
        Pattern p = Pattern.compile(stringValueRegex);
        for (String sValue : stringValues) {
            Matcher m = p.matcher(sValue);
            while (m.find()) {
                System.out.println("LINE 432: " + m.group() + ", length : " + m.group().length() + ", indexOf: " + m.group().indexOf("."));
                if (m.group().indexOf(".") > 0) {
                    String keyMatch = m.group().substring(2, m.group().indexOf("."));
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v).replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }

                    });
                } else {
                    String keyMatch =  m.group().substring(2, m.group().length()-1);
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v).replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }
                    });
                }
            }
        }
        return res;
    }
    
    public static Set<String> setOfParsedChatterStringValues(FlowMetadata md, HashMap<String, String> vars) {
        //For chatter message
        List<Object> actionCalls = md.actionCalls;
        Set<String> res = new HashSet<>();
        if (actionCalls.isEmpty()) { return res; }
        String rd = "";
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        for (Object obj : actionCalls) {
            Map<String, Object> item = (Map<String, Object>) obj;
            if (!item.get("actionType").toString().equals("chatterPost")) { continue; }
            List<Object> processMetadataValue = (ArrayList<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                for (Object pmv : processMetadataValue) {
                    HashMap<String, Object> pmvMap = (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmv), HashMap.class);
                    if (pmvMap.get("value") == null) { continue; }
                    ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(pmvMap.get("value")), ItemValue.class);
                    if (iValue != null) {
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
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
                        if (!StringUtils.isBlank(iValue.stringValue)) {
                            stringValues.add(iValue.stringValue);
                        }
                        if (!StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                    }
                }
            }
            
        }
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                    break;
                }
            }
        }
        String startOfExpression = "[{]!";
        String swlanmtoubl = "[a-zA-Z]" + "([_]?[a-zA-Z0-9]+)*";
        String caseForObject1 = swlanmtoubl + "([_]{2}" + swlanmtoubl + "([_]{2}[crCR])?)?";
        String caseForObject2 = "(\\u005B" + caseForObject1 + "([_]{2}[crCR])?\\u005D)";
        String caseForField1 = "([.]{1}" + swlanmtoubl + "([_]{2}" + swlanmtoubl  + ")?([_]{2}[rR])?){0,8}";
        String caseForField2 = "([.]{1}" + swlanmtoubl + "([_]{2}" + swlanmtoubl  + ")?([_]{2}[crCR])?)?";
        String exprssionForCasses = "((" + caseForObject1  + ")|(" + caseForObject2 + "))";
        String endOfExpression = "[}]";
//        System.out.println(startOfExpression + exprssionForCasses + caseForField1 + caseForField2 + endOfExpression);
        String middleValueRegex = startOfExpression + exprssionForCasses + caseForField1 + caseForField2 + endOfExpression;
//        String middleValueRegex = "[{]!((" + swlanmtoubl + ")|(\\u005B" + swlanmtoubl + "([_]{2}[crCR]){0,1}\\u005D))" +  
//                                  "([.]{1}" + swlanmtoubl + "([_]{2}[rR])?){0,8}([.]{1}" + swlanmtoubl + "([_]{2}[crCR])?)?[}]";
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
                                res.add(m.group().replace(k, v).replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }
                            if(!valueMatch.equals("") && valueMatch.equals(v)){
        //                        System.out.println("value v: " + v);
                                res.add(m.group().replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }
                    });
                } else {
                    Integer LBracketIndex = m.group().indexOf("]");
                    String keyMatch = LBracketIndex < 0 ? m.group().substring(2, m.group().length()-1) : "";
                    String valueMatch = LBracketIndex > 0 ? m.group().substring(3, m.group().length()-2) : "";
                    vars.forEach((k,v)->{
                            if(!keyMatch.equals("") && keyMatch.equals(k)){
        //                        System.out.println("key k: " + k);
                                res.add(m.group().replace(k, v).replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }
                            if(!valueMatch.equals("") && valueMatch.equals(v)){
        //                        System.out.println("value v: " + v);
                                res.add(m.group().replace("[", "").replace("]", "").replace("{!", "").replace("}", ""));
                            }
                    });
                }
            }
        }
        return res;
    }
    
    public class ItemValue {
        public String elementReference;
        public String stringValue;
    }
    
    
    public static void main(String[] args) throws Exception {
        
//        System.out.println(HelloServlet.returnJsonString());
        String bodyStr = HelloServlet.returnJsonString();
        ResponseRawData rawD = new Gson().fromJson(bodyStr, ResponseRawData.class);
//        System.out.println(rawD.toString());
//        System.out.println(rawD.rawData.toString());
//        System.out.println(rawD.rawData.Metadata.toString());
//        System.out.println(rawD.rawData.Metadata.variables.toString());
        Set<String> superReturn = new HashSet<>();
        for (RawData rd : rawD.rawData) {

            HashMap<String, String> vars = new HashMap<String, String>();
            for (Variable var : rd.Metadata.variables) {
                if (StringUtils.isBlank(var.objectType)) { continue; }
                vars.put(var.name, var.objectType);
            }
            vars.forEach((k,v)->{
                    System.out.println("\nkey: " + k + ", value: " + v);
            });        
            System.out.println("\n\n1: getActionCallsFU Response : " + getActionCallsFU(rd.Metadata, vars));
            System.out.println("\n\n2: getAssignmentsFU Response : " + getAssignmentsFU(rd.Metadata, vars));
            System.out.println("\n\n3: getDecisionsFU Response : " + getDecisionsFU(rd.Metadata, vars));
            System.out.println("\n\n4: getRecordCreatesFU Response : " + getRecordCreatesFU(rd.Metadata, vars));
            System.out.println("\n\n5: getRecordLookupsFU Response : " + getRecordLookupsFU(rd.Metadata, vars));
            System.out.println("\n\n6: getRecordUpdatesFU Response : " + getRecordUpdatesFU(rd.Metadata, vars));
            System.out.println("\n\n7: getFlowFormulasFU Response : " + getFlowFormulasFU(rd.Metadata, vars));
            System.out.println("\n\n8: getProcessMetadataValuesFromMDFU Response : " + getProcessMetadataValuesFromMDFU(rd.Metadata, vars));
            System.out.println("\n\n9: setOfParsedChatterStringValues Response : " + setOfParsedChatterStringValues(rd.Metadata, vars));
            
            superReturn.addAll(HelloServlet.returnAllFields(rd.Metadata, vars));
            
            System.out.println("returnAllFields: " + HelloServlet.returnAllFields(rd.Metadata, vars));
        }
        System.out.println(superReturn.toString());
//        String expression ="{!SecondValid_2.Name} .{!NotValid_2,Name }{!NotValid_2,Name {!SecondValid_7.Name} }{! NotValid_1.Name} {!NotValid_3.name__r.__c} {![FirstValid__c].Name__r.createdBy.Id}{!{!{!Valid_4.Name__c}}{!SecondValid_2.Name}{![ThirdValid].createdBy.Name}";
//        expression = "{!SObject.Name} + .{![Account__c].Name}...{![Account2].Name}.. ...{!SObject} {![asddsa__c]} ..{!dsaasd} {!dsaasd2__c}";
//        
//        HashMap<String, String> vars = new HashMap<>();
//        vars.put("SObject", "Account");
//        vars.put("SObject2", "Account2");
//        vars.put("asddsa", "asddsa__c");
//        vars.put("dsaasd", "dsaasd__c");
//        vars.put("dsaasd2", "dsaasd2__c");
//        
//        String abc = "";
//        HashMap<String, Object> flow;
////        flow = (HashMap<String, Object>) new Gson().fromJson(abc, (HashMap<String, Object>).class);
//        Set<String> stringValues = new HashSet<>();
//        stringValues.add(expression);
//        
//        String body = "{ \"rawData\": { \"Metadata\": { \"variables\": [{ \"asd\" : \"zxc\"}, { \"123\" : \"456\"}] } }, \"token\": \"ASDYQW127BFYWEBCAQWUQWNCE38ASDNCNUEO12\" }";
//        ResponseRawData rawD = new Gson().fromJson(body, ResponseRawData.class);
//        System.out.println(rawD.toString());
        
    }
    
}