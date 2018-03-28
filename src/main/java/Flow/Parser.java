package Flow;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class Parser {

    public static String parse(String item){
        ResponseRawData rawD = new Gson().fromJson(item, ResponseRawData.class);
        if (rawD == null) {
            return new Gson().toJson(new ArrayList<>()); 
        }
        
        Set<String> result = new HashSet<>();
        rawD.rawData.forEach((rd) -> {
            
            HashMap<String, String> vars = new HashMap<String, String>();
            rd.Metadata.variables.stream().filter((var) -> !(StringUtils.isBlank(var.objectType))).forEachOrdered((var) -> {
                vars.put(var.name, var.objectType);
            });
            
//            vars.forEach((k,v)->{
//                System.out.println("\nkey: " + k + ", value: " + v);
//            });

            result.addAll(getAllFieldsFromMD(rd.Metadata, vars));
        });
        return new Gson().toJson(result); 
    }
        
    public static Set<String> getAllFieldsFromMD(FlowMetadata metadata, HashMap<String, String> vars) {
        Set<String> res = new HashSet<>();
        res.addAll(getActionCallsFU(metadata, vars));
        res.addAll(getAssignmentsFU(metadata, vars));
        res.addAll(getDecisionsFU(metadata, vars));
        res.addAll(getRecordCreatesFU(metadata, vars));
        res.addAll(getRecordLookupsFU(metadata, vars));
        res.addAll(getRecordUpdatesFU(metadata, vars));
        res.addAll(getFlowFormulasFU(metadata, vars));
        res.addAll(getProcessMetadataValuesFromMDFU(metadata, vars));
        res.addAll(setOfParsedChatterStringValues(metadata, vars));
        System.out.println("  this res:" + res.toString());
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        return res;
    }
    
    public static Set<String> getActionCallsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> actionCalls = md.actionCalls;
        Set<String> res = new HashSet<>();
        
        if (actionCalls.isEmpty()) { 
            System.out.println("actionCalls: ");
            return setOfParsedStringValues(res, new HashSet<>(), vars);
        }
        String rd = "";
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        for (Object obj : actionCalls) {
            Map<String, Object> item = (Map<String, Object>) obj;
            if (item.get("actionType").toString().equals("chatterPost")) { continue; }
            List<Object> processMetadataValue = (ArrayList<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                processMetadataValue.stream().map((pmv) -> 
                        (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmv), HashMap.class)).filter((pmvMap) -> 
                                !(pmvMap.get("value") == null)).map((pmvMap) -> 
                                        (ItemValue) new Gson().fromJson(new Gson().toJson(pmvMap.get("value")), ItemValue.class)).filter((iValue) -> 
                                                (iValue != null)).map((iValue) -> 
                                                {
                                                    if (!StringUtils.isBlank(iValue.stringValue)) {
                                                        stringValues.add(iValue.stringValue);
                                                    }
                                                    return iValue; 
                                                }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                                    elementReferences.add(iValue.elementReference);
                                                });
                                            }
            List<Object> inputParameters = (ArrayList<Object>) item.get("inputParameters");
            if (inputParameters != null) {
                inputParameters.stream().map((pmv) -> 
                        (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmv), HashMap.class)).filter((pmvMap) -> 
                                !(pmvMap.get("value") == null)).map((pmvMap) -> 
                                        (ItemValue) new Gson().fromJson(new Gson().toJson(pmvMap.get("value")), ItemValue.class)).filter((iValue) -> 
                                                (iValue != null)).map((iValue) -> 
                                                {
                                                    if (!StringUtils.isBlank(iValue.stringValue)) {
                                                        stringValues.add(iValue.stringValue);
                                                    }
                                                    return iValue;
                                                }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                                    elementReferences.add(iValue.elementReference);
                                                });
                                            }
            List<Object> outputParameters = (ArrayList<Object>) item.get("outputParameters");
            if (outputParameters != null) {
                outputParameters.stream().map((op) -> (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(op), HashMap.class)).forEachOrdered((opMap) -> {
                    elementReferences.add((String) opMap.get("assignToReference"));
                });
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
        System.out.println("actionsCalls: ");
        res = setOfParsedStringValues(res, stringValues, vars);
        return res;
    }
    
    public static Set<String> getAssignmentsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> assignments = (List<Object>) md.assignments;
        Set<String> res = new HashSet<>();
        if (assignments.isEmpty()) { 
            System.out.println("assignements: ");
            return setOfParsedStringValues(res, new HashSet<>(), vars);
        }
        String rd = "";
        List<Object> assignmentItems = new ArrayList<>();
        assignments.stream().map((obj) -> (Map<String, Object>) obj).forEachOrdered((item) -> {
            assignmentItems.add(item.get("assignmentItems"));
        });
        List<Object> allItems = new ArrayList<>();
        assignmentItems.forEach((obj) -> { 
            for (Object item : (List<Object>) obj) {
                allItems.add(item);
            }
        });
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        allItems.stream().map((obj) -> (Map<String, Object>) obj).forEachOrdered((item) -> {
            List<Object> processMetadataValue = (List<Object>) item.get("processMetadataValues");
            if (processMetadataValue != null) {
                processMetadataValue.stream().filter((pmv) -> 
                        !(((Map<String, Object>) pmv).get("value") == null)).map((pmv) -> 
                                (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) pmv).get("value")), ItemValue.class)).filter((iValue) -> 
                                        (iValue != null)).map((iValue) -> {
                                            if (!StringUtils.isBlank(iValue.stringValue)) {
                                                stringValues.add(iValue.stringValue);
                                            }
                                            return iValue; 
                                        }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                            elementReferences.add(iValue.elementReference);
                                        });
                                    }
            if (!(item.get("value") == null)) {
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
        });
        for(String eR : elementReferences) {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key));
                    res.add(eR);
                    break;
                }
            }
        }
        System.out.println("assignements: ");
        return setOfParsedStringValues(res, stringValues, vars);
    }
    
    public static Set<String> getDecisionsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> decisions = (List<Object>) md.decisions;
        Set<String> res = new HashSet<>();
        if (decisions.isEmpty()) { return res; }
        List<Object> allRules = new ArrayList<>();
        decisions.stream().map((obj) -> (Map<String, Object>) obj).map((item) -> (List<Object>) item.get("rules")).filter((rules) -> !(rules == null)).forEachOrdered((rules) -> {
            rules.forEach((rule) -> {
                allRules.add(rule);
            });
        });
        if (allRules.isEmpty()) { return res; }
        
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        for (Object rule : allRules) {
            List<Object> conditions = (List<Object>) ((Map<String, Object>) rule).get("conditions");
            if (conditions == null) { continue; }
            
            conditions.stream().map((condition) -> (Map<String, Object>) condition).map((conditionMap) -> {
                elementReferences.add(conditionMap.get("leftValueReference").toString());
                return conditionMap;
            }).map((conditionMap) -> (ItemValue) new Gson().fromJson(new Gson().toJson(conditionMap.get("rightValue")), ItemValue.class)).filter((iValue) -> (iValue != null)).map((iValue) -> {
                if (!StringUtils.isBlank(iValue.stringValue)) {
                    stringValues.add(iValue.stringValue);
                }
                return iValue;
            }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                elementReferences.add(iValue.elementReference);
            });
            
        }
        elementReferences.forEach((eR) -> {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {
                    eR = eR.replace(key, vars.get(key)) + (eR.contains(".") ? "" : ".Id");
                    System.out.println("eR: " + eR);
                    res.add(eR);
                    break;
                }
            }
        });
        System.out.println("decisions: ");
        return setOfParsedStringValues(res, stringValues, vars);
    }
    
    public static Set<String> getRecordUpdatesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordUpdates = md.recordUpdates;
        Set<String> res = new HashSet<>();
        if (recordUpdates.isEmpty()) { return res; }
        String recordUpdateRD;
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
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
                processMetadataValue.stream().filter((pmv) -> 
                        !(((Map<String, Object>) pmv).get("value") == null)).map((pmv) -> 
                                (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) pmv).get("value")), ItemValue.class)).filter((iValue) -> 
                                        (iValue != null)).map((iValue) -> 
                                        {
                                            if (!StringUtils.isBlank(iValue.stringValue)) {
                                                stringValues.add(iValue.stringValue);
                                            }
                                            return iValue; 
                                        }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                            elementReferences.add(iValue.elementReference);
                                        });
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
        System.out.println("recordUpdate: ");
        return setOfParsedStringValues(res, stringValues, vars);
    }
    
    public static Set<String> getRecordLookupsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordLookups = (List<Object>) md.recordLookups;
        Set<String> res = new HashSet<>();
        if (recordLookups.isEmpty()) { return res; }
        String recordLookupRD = "";
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
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
        System.out.println("recordLookup: ");
        return setOfParsedStringValues(res, stringValues, vars);
    }
    
    public static Set<String> getRecordCreatesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordCreates = (List<Object>) md.recordCreates;
        Set<String> res = new HashSet<>();
        if (recordCreates.isEmpty()) { return res; }
        String recordCreateRD;
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
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
        elementReferences.forEach((eR) -> {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key)) + (eR.contains(".") ? "" : ".Id");
                    System.out.println("eR: " + eR);
                    res.add(eR);
                    break;
                }
            }
        });
        System.out.println("recordCreate: ");
        return setOfParsedStringValues(res, stringValues, vars);
    }
    
    public static Set<String> getFlowFormulasFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> formulas = (ArrayList<Object>) md.formulas;
        Set<String> res = new HashSet<>();
        if (formulas.isEmpty()) { return res; }
        
        Set<String> stringValues = new HashSet<>();
        formulas.stream().map((obj) -> (Map<String, Object>) obj).map((item) -> "" + item.get("expression")).forEachOrdered((expression) -> {
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
        });
        return setOfParsedFormulas(res, stringValues, vars);
    }
    
    public static Set<String> getProcessMetadataValuesFromMDFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> processMetadataValues = (ArrayList<Object>) md.processMetadataValues;
        Set<String> res = new HashSet<>();
        if (processMetadataValues.isEmpty()) { return res; }
        
        Set<String> stringValues = new HashSet<>();
        Set<String> elementReferences = new HashSet<>();
        processMetadataValues.stream().map((obj) -> 
                (Map<String, Object>) obj).filter((item) -> 
                        !(((Map<String, Object>) item).get("value") == null)).map((item) -> 
                                (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) item).get("value")), ItemValue.class)).map((iValue) -> 
                                {
                                    if (!StringUtils.isBlank(iValue.stringValue)) {
                                        stringValues.add(iValue.stringValue);
                                    }
                                    return iValue;
                                }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                    elementReferences.add(iValue.elementReference);
                                });
        
        elementReferences.forEach((eR) -> {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key)) + (eR.contains(".") ? "" : ".Id");
                    System.out.println("eR: " + eR);
                    res.add(eR);
                    break;
                }
            }
        });
        
        System.out.println("processMetadataValues: ");
        return setOfParsedStringValues(res, stringValues, vars);
    }

    /**
     * Not Chatter String Values support only expression like {!SObject.Name} where SObject is key in map(vars)
     * All other cases like {![Account].Name} or {! SObject.Name} don't have any affects on expression it will be only strings.
    **/
    public static Set<String> setOfParsedStringValues(Set<String> res, Set<String> stringValues, HashMap<String, String> vars) {
        System.out.println("setOfParsedStringValues: {");
        String startOfExpression = "[{]!";
        String swlanmtoubl = "[a-zA-Z]" + "((?!\\w*__\\w*)\\w*)*";
        String caseForField = "([.]" + "[a-zA-Z]" + "(?!\\w*___\\w*)\\w*" + "){0,10}";
        String endOfExpression = "[}]";
        String middleExpressionForValueRegex = swlanmtoubl + caseForField;
        String stringValueRegexPart3 = startOfExpression + middleExpressionForValueRegex + endOfExpression;
        String stringValueRegex =  stringValueRegexPart3;
        Pattern p = Pattern.compile(stringValueRegex);
        Set<String> allMatches = new HashSet<>();
        
        stringValues.stream().map((sValue) -> p.matcher(sValue)).forEachOrdered((m) -> {
            while (m.find()) {
                allMatches.add(m.group());
            }
        });
        allMatches.stream().map((m) -> m.replaceAll("[}{!]", "")).map((mKey) -> {
            return mKey; 
        }).forEachOrdered((mKey) -> {
            String keyMatch = mKey;
            String resultItem = "";
            if (mKey.contains(".")) {
                keyMatch = mKey.substring(0, mKey.indexOf("."));
                if (vars.containsKey(keyMatch)) {
                    resultItem = mKey.replaceFirst(keyMatch, vars.get(keyMatch));
                    res.add(resultItem);
                }
            } else {
                if (vars.containsKey(mKey)) {
                    resultItem = mKey.replaceFirst(mKey, vars.get(mKey)) + ".Id";;
                    res.add(resultItem);
                }
            }
            System.out.println("         s: " + mKey);
            System.out.println("resultItem: " + resultItem);
        });
        System.out.println("}");
        return res;
    }
        
    public static Set<String> setOfParsedFormulas(Set<String> res, Set<String> stringValues, HashMap<String, String> vars) {
        System.out.println("setOfParsedFormulas: {");

        String swlanmtoubl = "[a-zA-Z]" + "(?!\\w*___\\w*)\\w*";
        String stringValueRegex = swlanmtoubl + "([.]" + swlanmtoubl + "){1,10}";
        System.out.println("stringValueRegex: " + stringValueRegex);
        Pattern p = Pattern.compile(stringValueRegex);
        Set<String> allMatches = new HashSet<>();
        
        for (String sValue : stringValues) {
            Matcher m = p.matcher(getStringWhichContainOblyField(sValue));
            while(m.find()) {
                allMatches.add(m.group());
            }
        }

        allMatches.stream().map((m) -> 
                m.replaceAll("[}{!)(]", "").replaceAll("[+]", " ")).forEachOrdered((mKey) -> {
                    for (String s : mKey.split(" ")) {
                        if (s.isEmpty()) { continue; }
                        Boolean isKeyMatch = false;
                        String keyMatch = s;
                        if (s.startsWith("$Setup.")) {
                            keyMatch = s.replace("$Setup.", "");
                            String resultItem = keyMatch.replaceAll(" ", "");
                            if (!keyMatch.contains(".")) {
                                resultItem = resultItem + ".Id";
                            }
                            res.add(resultItem);
                            keyMatch = resultItem;
                            isKeyMatch = true;
                        } else if (s.contains(".")) {
                            keyMatch = s.replace("$", "").substring(0, s.indexOf("."));
                            if (vars.containsKey(keyMatch)) {
                                String resultItem = s.replace(keyMatch, vars.get(keyMatch));
                                resultItem = resultItem.replaceAll(" ", "");
                                res.add(resultItem);
                                isKeyMatch = true;
                                keyMatch = resultItem;
                            }
                        } else {
                            keyMatch =  s.replace("$", "");
                            if (vars.containsKey(keyMatch)) {
                                String resultItem = s.replace(keyMatch, vars.get(keyMatch));
                                resultItem = resultItem.replaceAll(" ", "") + ".Id";
                                res.add(resultItem);
                                isKeyMatch = true;
                                keyMatch = resultItem;
                            }
                        }
                        if (!isKeyMatch) {
//                          keyMatch = s.replaceAll(" ", "").replace("$", "");
                            keyMatch = s.replaceAll("(([$])|())", "") + ((keyMatch.contains(".") || s.contains("."))  ? "" : ".Id");
                            res.add(keyMatch);
                        }
                        System.out.println("         s: " + s.replaceFirst("[$]", ""));
                        System.out.println("resultItem: " + keyMatch);
                    }
        });
        System.out.println("}");
        return res;
    }

    
    /**
     * Chatter String Values support only: expression like {!SObject.Name} where SObject is key in map(vars) and {![Account].Name}
     * All other cases like {! [Account].Name} or {! SObject.Name} don't have any affects on expression it will be only strings.
    **/
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
                processMetadataValue.stream().map((pmv) -> 
                        (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmv), HashMap.class)).filter((pmvMap) -> 
                                !(pmvMap.get("value") == null)).map((pmvMap) -> 
                                        (ItemValue) new Gson().fromJson(new Gson().toJson(pmvMap.get("value")), ItemValue.class)).filter((iValue) -> 
                                                (iValue != null)).map((iValue) -> 
                                                {
                                                    if (!StringUtils.isBlank(iValue.stringValue)) {
                                                        stringValues.add(iValue.stringValue);
                                                    }
                                                    return iValue; 
                                                }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                                    elementReferences.add(iValue.elementReference);
                                                });
            }
            
            List<Object> inputParameters = (ArrayList<Object>) item.get("inputParameters");
            if (inputParameters != null) {
                inputParameters.stream().filter((pmv) -> 
                        !(((Map<String, Object>) pmv).get("value") == null)).map((pmv) -> 
                            (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) pmv).get("value")), ItemValue.class)).filter((iValue) -> 
                                    (iValue != null)).map((iValue) -> 
                                    {
                                        if (!StringUtils.isBlank(iValue.stringValue)) {
                                            stringValues.add(iValue.stringValue);
                                        }
                                        return iValue;
                                    }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                        elementReferences.add(iValue.elementReference);
                                    });
                                }
        }
        elementReferences.forEach((eR) -> {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key)) + (eR.contains(".") ? "" : ".Id");
                    System.out.println("eR: " + eR);
                    res.add(eR);
                    break;
                }
            }
        });
        System.out.println("chatterStringValues:\nsetOfParsedStringValues: { ");
        String startOfExpression = "[{]!";
        
        String swlanmtoubl1 = "[a-zA-Z]" + "((?!\\w*__\\w*)\\w*)*"; //do not allow 2 underscores in a row. 
        String swlanmtoubl2 = "[a-zA-Z]" + "((?!\\w*___\\w*)\\w*)*"; //do not allow 3 underscores in a row. 
        
        String caseForField1 = "([.]" + swlanmtoubl1 + "){0,10}";
        String caseForField2 = "([.]" + swlanmtoubl2 + "){1,10}";
        
        String caseForObject2 = "(\\u005B" + swlanmtoubl2 + "\\u005D)";
        String exprssionForObjectCasses = "((" + swlanmtoubl1 + caseForField1 +  ")|(" + caseForObject2 + caseForField2 + "))";
        String endOfExpression = "[}]";
        String stringValueRegex =  startOfExpression + exprssionForObjectCasses + endOfExpression;
//        System.out.println("stringChatterValueRegex: " + stringValueRegex);
        Pattern p = Pattern.compile(stringValueRegex);
        Set<String> allMatches = new HashSet<>();
        stringValues.stream().map((sValue) -> p.matcher(sValue)).forEachOrdered((m) -> {
            while (m.find()) {
                allMatches.add(m.group());
            }
        });
        
        allMatches.stream().map((m) -> m.replaceAll("[}{!]", "")).map((mKey) -> {
            System.out.println("         s: " + mKey);
            return mKey; 
        }).forEachOrdered((mKey) -> {
            String keyMatch = mKey;
            String resultItem = "";
            if (mKey.contains("[")) {
                resultItem  = mKey.replaceAll("[\\u005B\\u005D]", "");
                res.add(resultItem);
            } else if (mKey.contains(".")) {
                keyMatch = mKey.substring(0, mKey.indexOf("."));
                if(vars.containsKey(keyMatch)) {
                    resultItem = mKey.replaceFirst(keyMatch, vars.get(keyMatch));
                    res.add(resultItem);
                }
            } else {
                if(vars.containsKey(keyMatch)) {
                    resultItem = mKey.replaceFirst(keyMatch, vars.get(keyMatch)) + ".Id";
                    res.add(resultItem);
                }
            }
            System.out.println("resultItem: " + resultItem);
        });
        System.out.println("}");
        return res;
    }
    
    public static String getStringWhichContainOblyField(String str) {
    
        int braceIndex = str.indexOf("'");
        int notBraceIndex = str.indexOf("\\'");
        int doubleBraceIndex = str.indexOf("\"");
        int notDoubleBraceIndex = str.indexOf("\\\"");
        int openCommentIndex = str.indexOf("/*");
        int closeCommentIndex = str.indexOf("*/");
        
        Set<Integer> notBraceIndexes = new HashSet<>();
        Set<Integer> notDoubleBraceIndexes = new HashSet<>();
        Set<Integer> openCommentIndexes = new HashSet<>();
        Set<IndexClass> sortedIndexes = new TreeSet<>();
        
        while (notBraceIndex >= 0) {
//            System.out.println("notBraceIndex: " + notBraceIndex);
            notBraceIndexes.add(notBraceIndex + 1);
            notBraceIndex = str.indexOf("\\'", notBraceIndex + 1);
        }
        
        while (notDoubleBraceIndex >= 0) {
//            System.out.println("notDoubleBraceIndex: " + notDoubleBraceIndex);
            notDoubleBraceIndexes.add(notDoubleBraceIndex + 1);
            notDoubleBraceIndex = str.indexOf("\\\"", notDoubleBraceIndex + 1);
        }
                
        while (braceIndex >= 0) {
            if (notBraceIndexes.contains(braceIndex)) {
                braceIndex = str.indexOf("'", braceIndex + 1);
                continue;
            }
            IndexClass ic = new IndexClass();
            ic.setIndex(braceIndex);
            ic.setType("braceIndex");
            sortedIndexes.add(ic);
//            System.out.println("braceIndex: " + braceIndex);
            braceIndex = str.indexOf("'", braceIndex + 1);
        }
        
        while (doubleBraceIndex >= 0) {
            if (notDoubleBraceIndexes.contains(doubleBraceIndex)) {
                doubleBraceIndex = str.indexOf("\"", doubleBraceIndex + 1);
                continue;
            }
            IndexClass ic = new IndexClass();
            ic.setIndex(doubleBraceIndex);
            ic.setType("doubleBraceIndex");
            sortedIndexes.add(ic);
//            System.out.println("doubleBraceIndex: " + doubleBraceIndex);
            doubleBraceIndex = str.indexOf("\"", doubleBraceIndex + 1);
        }
                
        while (openCommentIndex >= 0) {
            IndexClass ic = new IndexClass();
            ic.setIndex(openCommentIndex);
            ic.setType("openCommentIndex");
            sortedIndexes.add(ic);
//            System.out.println("openCommentIndex: " + openCommentIndex);
            openCommentIndexes.add(openCommentIndex);
            openCommentIndex = str.indexOf("/*", openCommentIndex + 1);
        }
                        
        while (closeCommentIndex >= 0) {    
            if (openCommentIndexes.contains(closeCommentIndex-1)) {
                closeCommentIndex = str.indexOf("*/", closeCommentIndex + 1);
                continue;
            }
            IndexClass ic = new IndexClass();
            ic.setIndex(closeCommentIndex);
            ic.setType("closeCommentIndex");
            sortedIndexes.add(ic);
//            System.out.println("closeCommentIndex: " + closeCommentIndex);
            closeCommentIndex = str.indexOf("*/", closeCommentIndex + 1);
        }
        
        Set<IndexClass> indexesForDelete = new TreeSet<>(new IndexComparator());
//        Set<IndexClass> indexesForDelete = new HashSet<>();
        boolean isStartIndexFound = false;
        boolean needCheck = false;
        String indexType = "";
        int firstIndex = -1;
        int lastIndex = -1;
        for (IndexClass ic : sortedIndexes) {
//            System.out.println("ic: " + ic.index + ", type: " + ic.type);
            if (indexType == ic.getType()) {
                lastIndex = ic.getIndex();
                isStartIndexFound = false;
                IndexClass icDeleted = new IndexClass();
                icDeleted.setFirstIndex(firstIndex);
                icDeleted.setLastIndex(lastIndex);
                icDeleted.setType(indexType);
                indexesForDelete.add(icDeleted);
                indexType = "";
                firstIndex = -1;
                lastIndex = -1;
                isStartIndexFound = false;
                needCheck = true;
            }
            
            if (!isStartIndexFound && !needCheck) {
                firstIndex = ic.getIndex();
                indexType = ic.getType() == "openCommentIndex" ? "closeCommentIndex" : ic.getType() ;
                isStartIndexFound = true;
            }
            needCheck = false;

        }
        String strFirst = str;
        for (IndexClass ic : indexesForDelete) {
//            System.out.println("index: " + ic.index + ", firstIndex: " + ic.firstIndex + ", lastIndex: " + ic.lastIndex + ", type: " + ic.type);
            if (ic.getType() == "closeCommentIndex") {
//                System.out.println("   token: " + str.substring(ic.firstIndex, ic.lastIndex+2));
                str = str.substring(0, ic.getFirstIndex()) + str.substring(ic.getLastIndex()+2);
            } else {
//                System.out.println("   token: " + str.substring(ic.firstIndex, ic.lastIndex+1));
                str = str.substring(0, ic.getFirstIndex()) + str.substring(ic.getLastIndex()+1);
            }
        }
        System.out.println("strFirst: " + strFirst);
        str = str.replaceAll("\\s", "");
        System.out.println("     str: " + str);
        return str;
    
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
    
    public class ItemValue {
        public String elementReference;
        public String stringValue;
    }
    
}

class IndexClass implements Comparable<IndexClass>{
    private int index, firstIndex, lastIndex;
    private String type;
    
    public int getIndex() {
        return this.index;
    }   
    
    public int getFirstIndex() {
        return this.firstIndex;
    }    
    
    public int getLastIndex() {
        return this.lastIndex;
    }    
    
    public String getType() {
        return this.type;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }   
    
    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }    
    
    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }    
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public int compareTo(IndexClass ic) {
        return index > ic.index ? 1 : (index == ic.index ? 0 : -1);
    }
}

class IndexComparator implements Comparator<IndexClass> {
    @Override
    public int compare(IndexClass ic1, IndexClass ic2){
        return ic1.getFirstIndex() > ic2.getFirstIndex() ? - 1 : (ic1.getFirstIndex() == ic2.getFirstIndex() ? 0 : 1);
    }
}