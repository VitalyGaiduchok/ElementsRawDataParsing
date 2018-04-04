package Flow;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class Parser {

    private static Set<String> result;
    
    public static String parse(String item){
        if (StringUtils.isBlank(item)) {
            return new Gson().toJson(new ArrayList<>()); 
        }
        ResponseRawData rawD = new Gson().fromJson(item, ResponseRawData.class);
        if (rawD == null || rawD.body == null) {
            return new Gson().toJson(new ArrayList<>()); 
        }
        result = new HashSet<>();
        rawD.body.forEach((rd) -> {
            if (rd.Metadata != null) { 
                HashMap<String, String> vars = new HashMap<>();
                rd.Metadata.variables.stream().filter((var) -> !(StringUtils.isBlank(var.objectType))).forEachOrdered((var) -> {
                    vars.put(var.name, var.objectType);
                    if ("SObject".equals(var.dataType) && !StringUtils.isBlank(var.objectType)) {
                        result.add(var.objectType + ".Id");
                    }
                });

//            vars.forEach((k,v)->{
//                System.out.println("\nkey: " + k + ", value: " + v);
//            });
                getActionCallsFU(rd.Metadata, vars);
                getAssignmentsFU(rd.Metadata, vars);
                getChoicesFU(rd.Metadata, vars);
                getDecisionsFU(rd.Metadata, vars);
                getDynamicChoiceSetsFU(rd.Metadata, vars);
                getRecordCreatesFU(rd.Metadata, vars);
                getRecordLookupsFU(rd.Metadata, vars);
                getRecordUpdatesFU(rd.Metadata, vars);
                getFlowFormulasFU(rd.Metadata, vars);
                getProcessMetadataValuesFromMDFU(rd.Metadata, vars);
                getWaitsFU(rd.Metadata, vars);
                setOfParsedChatterStringValues(rd.Metadata, vars);
            }
        });
        System.out.println("{\"parseResult\" : " + new Gson().toJson(result) + "}");
        return new Gson().toJson(result); 
    }
    
    public static void getActionCallsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> actionCalls = md.actionCalls;
        if (actionCalls.isEmpty()) { 
            //System.out.println("actionCalls: ");
            return;
        }
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
                                                (iValue != null)).map((iValue) -> {
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
                
                String actionName = item.get("actionName").toString();
                inputParameters.stream().map((ip) -> {
                    if (((Map<String, Object>) ip).get("value") != null) {
                        ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) ip).get("value")), ItemValue.class);
                        if (!StringUtils.isBlank(iValue.elementReference)) {
//                            System.out.println("1 elementReference: " + iValue.elementReference);
                            elementReferences.add(iValue.elementReference);
                        }
                        if (!StringUtils.isBlank(iValue.stringValue)){
                            stringValues.add(iValue.stringValue);
                        }
                    }
                    return ip;
                }).forEachOrdered((ip) -> {
                    List<Object> pmvObjs = (List<Object>) ((HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(ip), HashMap.class)).get("processMetadataValues");
                    if (!(pmvObjs == null)) {
                        String ipName = (String) ((Map<String, Object>) ip).get("name");
                        pmvObjs.stream().map((pmvObj) -> (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(pmvObj), HashMap.class)).filter((pmv) -> !(pmv.get("value") == null)).forEachOrdered((pmv) -> {
                            String pmvName = pmv.get("name").toString();
                            ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(pmv.get("value")), ItemValue.class);
                            if (!StringUtils.isBlank(iValue.elementReference)) {
//                            System.out.println("2 elementReference: " + iValue.elementReference);
                                elementReferences.add(iValue.elementReference);
                            }
                            if (actionName.contains(".") && ipName.equals("contextId") && pmvName.equals("dataType")) {
//                            System.out.println("3 actionName.substring(0, actionName.indexOf(\".\")) + \".Id\": " + actionName.substring(0, actionName.indexOf(".")) + ".Id");
                                result.add(actionName.substring(0, actionName.indexOf(".")) + ".Id");
                            } else if (!StringUtils.isBlank(iValue.stringValue)){
                                stringValues.add(iValue.stringValue);
                            }
                        });
                    }
                });
                
            }

            List<Object> outputParameters = (ArrayList<Object>) item.get("outputParameters");
            if (outputParameters != null) {
                outputParameters.stream().map((op) -> (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(op), HashMap.class)).forEachOrdered((opMap) -> {
                    elementReferences.add((String) opMap.get("assignToReference"));
                });
            }

        }
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("actionsCalls: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getAssignmentsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> assignments = (List<Object>) md.assignments;
        if (assignments.isEmpty()) { 
            //System.out.println("assignements: ");
            return;
        }
        List<Object> assignmentItems = new ArrayList<>();
        assignments.stream().map((obj) -> (Map<String, Object>) obj).forEachOrdered((item) -> {
            assignmentItems.add(item.get("assignmentItems"));
        });
        List<Object> allItems = new ArrayList<>();
        assignmentItems.forEach((obj) -> { 
            ((List<Object>) obj).forEach((item) -> {
                allItems.add(item);
            });
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
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("assignements: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getDecisionsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> decisions = (List<Object>) md.decisions;
        if (decisions.isEmpty()) { return; }
        List<Object> allRules = new ArrayList<>();
        decisions.stream().map((obj) -> (Map<String, Object>) obj).map((item) -> (List<Object>) item.get("rules")).filter((rules) -> !(rules == null)).forEachOrdered((rules) -> {
            rules.forEach((rule) -> {
                allRules.add(rule);
            });
        });
        if (allRules.isEmpty()) { return; }
        
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
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("decisions: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getDynamicChoiceSetsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> dynamicChoiceSets = md.dynamicChoiceSets;
        if (dynamicChoiceSets.isEmpty()) { return; }
        
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        
        dynamicChoiceSets.stream().map((dynamicChoiceSet) -> (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(dynamicChoiceSet), HashMap.class)).forEachOrdered((item) -> {
            String picklistField = (String) item.get("picklistField");
            String displayField = (String) item.get("displayField");
            String sortField = (String) item.get("sortField");
            String valueField = (String) item.get("valueField");
            String objName = (String) (item.get("object") != null ? item.get("object") : item.get("picklistObject"));
            if (!StringUtils.isBlank(objName)) {
                if (!StringUtils.isBlank(picklistField)) {
                    result.add(objName + "." + picklistField); 
                }
                if (!StringUtils.isBlank(displayField)) {
                    result.add(objName + "." + displayField); 
                } 
                if (!StringUtils.isBlank(valueField)) {
                    result.add(objName + "." + valueField); 
                }
                if (!StringUtils.isBlank(sortField)) {
                    result.add(objName + "." + sortField); 
                }
            }
            List<Object> filters = (List<Object> ) item.get("filters");
            String filterField;
            if (filters != null) {
                for (Object filter : filters) {
                    Map<String, Object> iFilter = (Map<String, Object>) filter;
                    filterField = objName + "." + iFilter.get("field");
                    result.add(filterField);
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
                    if (!StringUtils.isBlank((String) outputAssignmentMap.get("assignToReference"))) {
                        elementReferences.add((String) outputAssignmentMap.get("assignToReference"));
                    }
                    filterField = objName + "." + outputAssignmentMap.get("field");
                    result.add(filterField);
                }
            }
        });
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("dynamicChoiceSets: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getChoicesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> choices = md.choices;
        if (choices.isEmpty()) { return; }
        
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        
        choices.stream().map((dynamicChoiceSet) -> (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(dynamicChoiceSet), HashMap.class)).forEachOrdered((item) -> {
            ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(item.get("value")), ItemValue.class);
            if (iValue != null) {
                if (!StringUtils.isBlank(iValue.stringValue)) {
                    stringValues.add(iValue.stringValue);
                }
                if (!StringUtils.isBlank(iValue.elementReference)) {
                    elementReferences.add(iValue.elementReference);
                }
            }

        });
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("dynamicChoiceSets: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getRecordUpdatesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordUpdates = md.recordUpdates;
        if (recordUpdates.isEmpty()) { return; }
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
                    result.add(recordUpdateRD);
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
                    result.add(recordUpdateRD);
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
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("recordUpdate: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getRecordLookupsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordLookups = (List<Object>) md.recordLookups;
        if (recordLookups.isEmpty()) { return; }
        String recordLookupRD;
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
                    result.add(recordLookupRD);
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
                    result.add(recordLookupRD);
                }
            }
            List<Object> queriedFields = (List<Object> ) item.get("queriedFields");
            if (queriedFields != null) {
                queriedFields.forEach((field) -> {
                    result.add(objectName + "." + field);
                });
            }
            
        }
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("recordLookup: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getRecordCreatesFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> recordCreates = (List<Object>) md.recordCreates;
        if (recordCreates.isEmpty()) { return; }
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
                    result.add(recordCreateRD);
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
        addElementReferencesToResultSet(elementReferences, vars);

        //System.out.println("recordCreate: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    public static void getFlowFormulasFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> formulas = (ArrayList<Object>) md.formulas;
        if (formulas.isEmpty()) { return; }
        
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
        setOfParsedFormulas(stringValues, vars);
    }
    
    //ProcessMetadataValues on Metadata level in rawData json.
    public static void getProcessMetadataValuesFromMDFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> processMetadataValues = (ArrayList<Object>) md.processMetadataValues;
        if (processMetadataValues.isEmpty()) { return; }
        
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
        
        addElementReferencesToResultSet(elementReferences, vars);
        
        //System.out.println("processMetadataValues: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }

    /**
     * Not Chatter String Values support only expression like {!SObject.Name} where SObject is key in map(vars)
     * All other cases like {![Account].Name} or {! SObject.Name} don't have any affects on expression it will be only strings.
    **/
    public static void addSetOfParsedStringValuesToResultSet(Set<String> stringValues, HashMap<String, String> vars) {
        //System.out.println("addSetOfParsedStringValuesToResultSet: {");
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
            String resultItem;
            if (mKey.contains(".")) {
                String keyMatch = mKey.substring(0, mKey.indexOf("."));
                if (vars.containsKey(keyMatch)) {
                    resultItem = mKey.replaceFirst(keyMatch, vars.get(keyMatch));
                    result.add(resultItem);
                }
            } else {
                if (vars.containsKey(mKey)) {
                    resultItem = mKey.replaceFirst(mKey, vars.get(mKey)) + ".Id";;
                    result.add(resultItem);
                }
            }
            //System.out.println("         s: " + mKey);
            //System.out.println("resultItem: " + resultItem);
        });
        //System.out.println("}");
    }
        
    public static void setOfParsedFormulas(Set<String> stringValues, HashMap<String, String> vars) {
        //System.out.println("setOfParsedFormulas: {");

        String swlanmtoubl = "[a-zA-Z]" + "(?!\\w*___\\w*)\\w*";
        String stringValueRegex = swlanmtoubl + "([.]" + swlanmtoubl + "){1,10}";
        //System.out.println("stringValueRegex: " + stringValueRegex);
        Pattern p = Pattern.compile(stringValueRegex);
        Set<String> allMatches = new HashSet<>();
        
        stringValues.stream().map((sValue) -> p.matcher(getStringWhichContainOnlyField(sValue))).forEachOrdered((m) -> {
            while(m.find()) {
                allMatches.add(m.group());
            }
        });

        allMatches.stream().map((m) -> 
                fastReplace(m.replaceAll("[}{!)(]", ""), "+", " ")).forEachOrdered((mKey) -> {
                    for (String s : mKey.split(" ")) {
                        if (s.isEmpty()) { continue; }
                        Boolean isKeyMatch = false;
                        String keyMatch = s;
                        if (s.startsWith("$Setup.")) {
                            keyMatch = fastReplace(s, "$Setup.", "");
                            String resultItem = fastReplace(keyMatch, " ", "");
                            if (!keyMatch.contains(".")) {
                                resultItem = resultItem + ".Id";
                            }
                            result.add(resultItem);
                            keyMatch = resultItem;
                            isKeyMatch = true;
                        } else if (s.contains(".")) {
                            keyMatch = fastReplace(s, "$", "").substring(0, s.indexOf("."));
                            if (vars.containsKey(keyMatch)) {
                                String resultItem = s.replace(keyMatch, vars.get(keyMatch));
                                resultItem = fastReplace(resultItem, " ", "");
                                result.add(resultItem);
                                isKeyMatch = true;
                                keyMatch = resultItem;
                            }
                        } else {
                            keyMatch = fastReplace(s, "$", "");
                            if (vars.containsKey(keyMatch)) {
                                String resultItem = s.replace(keyMatch, vars.get(keyMatch));
                                resultItem = fastReplace(resultItem, " ", "") + ".Id";
                                result.add(resultItem);
                                isKeyMatch = true;
                                keyMatch = resultItem;
                            }
                        }
                        if (!isKeyMatch) {
                            keyMatch = fastReplace(s, "$", "") + ((keyMatch.contains(".") || s.contains("."))  ? "" : ".Id");
                            result.add(keyMatch);
                        }
                        //System.out.println("         s: " + fastReplace(s, "$", ""));
                        //System.out.println("resultItem: " + keyMatch);
                    }
        });
        //System.out.println("}");
    }

    public static void getWaitsFU(FlowMetadata md, HashMap<String, String> vars) {
        List<Object> waits = (List<Object>) md.waits;
        if (waits.isEmpty()) { return; }
        
        List<Object> waitEvents = new ArrayList<>();
        waits.stream().map((obj) -> (Map<String, Object>) obj).map((item) -> (List<Object>) item.get("waitEvents")).filter((rules) -> !(rules == null)).forEachOrdered((rules) -> {
            rules.forEach((rule) -> {
                waitEvents.add(rule);
            });
        });
        if (waitEvents.isEmpty()) { return; }
        
        Set<String> elementReferences = new HashSet<>();
        Set<String> stringValues = new HashSet<>();
        for (Object waitEvent : waitEvents) {
            
            List<Object> conditions = (List<Object>) ((Map<String, Object>) waitEvent).get("conditions");
            if (conditions != null) {
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
            List<Object> inputParameters = (ArrayList<Object>) ((Map<String, Object>) waitEvent).get("inputParameters");
            if (inputParameters != null) {
                String objectName = "";
                String objectField = "";
                for (Object ip : inputParameters) {
                    String ipName = (String) ((Map<String, Object>) ip).get("name");
                    if (((Map<String, Object>) ip).get("value") != null) {
                        ItemValue iValue = (ItemValue) new Gson().fromJson(new Gson().toJson(((Map<String, Object>) ip).get("value")), ItemValue.class);
                        if (!StringUtils.isBlank(iValue.elementReference)) {
                            elementReferences.add(iValue.elementReference);
                        }
                        if (ipName.equals("TimeTableColumnEnumOrId")) {
                            objectName = iValue.stringValue;
                        } 
                        if (ipName.equals("TimeFieldColumnEnumOrId")) {
                            objectField = iValue.stringValue;
                        }
                    }
                }
                if (!StringUtils.isBlank(objectName) && !StringUtils.isBlank(objectField)) {
                    result.add(objectName + "." + objectField);
                }
            }
            
            List<Object> outputParameters = (ArrayList<Object>) ((Map<String, Object>) waitEvent).get("outputParameters");
            if (outputParameters != null) {
                outputParameters.stream().map((op) -> (HashMap<String, Object>) new Gson().fromJson(new Gson().toJson(op), HashMap.class)).forEachOrdered((opMap) -> {
                    elementReferences.add((String) opMap.get("assignToReference"));
                });
            }
            
        }
        addElementReferencesToResultSet(elementReferences, vars);
        
        //System.out.println("waits: ");
        addSetOfParsedStringValuesToResultSet(stringValues, vars);
    }
    
    /**
     * Chatter String Values support only: expression like {!SObject.Name} where SObject is key in map(vars) and {![Account].Name}
     * All other cases like {! [Account].Name} or {! SObject.Name} don't have any affects on expression it will be only strings.
    **/
    public static void setOfParsedChatterStringValues(FlowMetadata md, HashMap<String, String> vars) {
        //For chatter message
        List<Object> actionCalls = md.actionCalls;
        if (actionCalls.isEmpty()) { return; }
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
                                                (iValue != null)).map((iValue) -> {
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
                                    (iValue != null)).map((iValue) -> {
                                        if (!StringUtils.isBlank(iValue.stringValue)) {
                                            stringValues.add(iValue.stringValue);
                                        }
                                        return iValue;
                                    }).filter((iValue) -> (!StringUtils.isBlank(iValue.elementReference))).forEachOrdered((iValue) -> {
                                        elementReferences.add(iValue.elementReference);
                                    });
                                }
        }
        addElementReferencesToResultSet(elementReferences, vars);
        //System.out.println("chatterStringValues:\naddSetOfParsedStringValuesToResultSet: { ");
        String startOfExpression = "[{]!";
        
        String swlanmtoubl1 = "[a-zA-Z]" + "((?!\\w*__\\w*)\\w*)*"; //do not allow 2 underscores in a row. 
        String swlanmtoubl2 = "[a-zA-Z]" + "((?!\\w*___\\w*)\\w*)*"; //do not allow 3 underscores in a row. 
        
        String caseForField1 = "([.]" + swlanmtoubl1 + "){0,10}";
        String caseForField2 = "([.]" + swlanmtoubl2 + "){1,10}";
        
        String caseForObject2 = "(\\u005B" + swlanmtoubl2 + "\\u005D)";
        String exprssionForObjectCasses = "((" + swlanmtoubl1 + caseForField1 +  ")|(" + caseForObject2 + caseForField2 + "))";
        String endOfExpression = "[}]";
        String stringValueRegex =  startOfExpression + exprssionForObjectCasses + endOfExpression;
//        //System.out.println("stringChatterValueRegex: " + stringValueRegex);
        Pattern p = Pattern.compile(stringValueRegex);
        Set<String> allMatches = new HashSet<>();
        stringValues.stream().map((sValue) -> p.matcher(sValue)).forEachOrdered((m) -> {
            while (m.find()) {
                allMatches.add(m.group());
            }
        });
        
        allMatches.stream().map((m) -> m.replaceAll("[}{!]", "")).map((mKey) -> {
            //System.out.println("         s: " + mKey);
            return mKey; 
        }).forEachOrdered((mKey) -> {
            String keyMatch = mKey;
            String resultItem = "";
            if (mKey.contains("[")) {
                resultItem  = mKey.replaceAll("[\\u005B\\u005D]", "");
                result.add(resultItem);
            } else if (mKey.contains(".")) {
                keyMatch = mKey.substring(0, mKey.indexOf("."));
                if(vars.containsKey(keyMatch)) {
                    resultItem = mKey.replaceFirst(keyMatch, vars.get(keyMatch));
                    result.add(resultItem);
                }
            } else {
                if(vars.containsKey(keyMatch)) {
                    resultItem = mKey.replaceFirst(keyMatch, vars.get(keyMatch)) + ".Id";
                    result.add(resultItem);
                }
            }
            //System.out.println("resultItem: " + resultItem);
        });
        //System.out.println("}");
    }
    
    //Remove from expression all strings like: 'test', "test", and also remove all comments /* */ 
    public static String getStringWhichContainOnlyField(String str) {
    
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
//            //System.out.println("notBraceIndex: " + notBraceIndex);
            notBraceIndexes.add(notBraceIndex + 1);
            notBraceIndex = str.indexOf("\\'", notBraceIndex + 1);
        }
        
        while (notDoubleBraceIndex >= 0) {
//            //System.out.println("notDoubleBraceIndex: " + notDoubleBraceIndex);
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
//            //System.out.println("braceIndex: " + braceIndex);
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
//            //System.out.println("doubleBraceIndex: " + doubleBraceIndex);
            doubleBraceIndex = str.indexOf("\"", doubleBraceIndex + 1);
        }
                
        while (openCommentIndex >= 0) {
            IndexClass ic = new IndexClass();
            ic.setIndex(openCommentIndex);
            ic.setType("openCommentIndex");
            sortedIndexes.add(ic);
//            //System.out.println("openCommentIndex: " + openCommentIndex);
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
//            //System.out.println("closeCommentIndex: " + closeCommentIndex);
            closeCommentIndex = str.indexOf("*/", closeCommentIndex + 1);
        }
        
        Set<IndexClass> indexesForDelete = new TreeSet<>(new IndexComparator());
        boolean isStartIndexFound = false;
        boolean needCheck = true;
        String indexType = "";
        int firstIndex = -1;
        int lastIndex = -1;
        for (IndexClass ic : sortedIndexes) {
//            //System.out.println("ic: " + ic.index + ", type: " + ic.type);
            if (indexType == null ? ic.getType() == null : indexType.equals(ic.getType())) {
                lastIndex = ic.getIndex();
                IndexClass icDeleted = new IndexClass();
                icDeleted.setFirstIndex(firstIndex);
                icDeleted.setLastIndex(lastIndex);
                icDeleted.setType(indexType);
                indexesForDelete.add(icDeleted);
                indexType = "";
                firstIndex = -1;
                lastIndex = -1;
                isStartIndexFound = false;
                needCheck = false;
            }
            
            if (!isStartIndexFound && needCheck) {
                firstIndex = ic.getIndex();
                indexType = "openCommentIndex".equals(ic.getType()) ? "closeCommentIndex" : ic.getType() ;
                isStartIndexFound = true;
            }
            needCheck = true;

        }
        StringBuffer bodyStr = new StringBuffer(str);
        indexesForDelete.forEach((ic) -> {
            //            //System.out.println("index: " + ic.index + ", firstIndex: " + ic.firstIndex + ", lastIndex: " + ic.lastIndex + ", type: " + ic.type);
            if ("closeCommentIndex".equals(ic.getType())) {
//                //System.out.println("   token: " + str.substring(ic.firstIndex, ic.lastIndex+2));
                bodyStr.delete(ic.getFirstIndex(), ic.getLastIndex()+2);
            } else {
//                //System.out.println("   token: " + str.substring(ic.firstIndex, ic.lastIndex+1));
                bodyStr.delete(ic.getFirstIndex(), ic.getLastIndex()+1);
            }
        });
        //System.out.println("received field: " + str);
//        str = str.replaceAll("\\s", "");
        str = fastReplace(bodyStr.toString(), " ", "");
        //System.out.println("  result field: " + str);
        return str;
    
    }
    
    static String fastReplace(String str, String target, String replacement) {
        int targetLength = target.length();
        if (targetLength == 0) {
            return str;
        }
        int idx2 = str.indexOf(target);
        if (idx2 < 0) {
            return str;
        }
        StringBuilder buffer = new StringBuilder(targetLength > replacement.length() ? str.length() : str.length() * 2);
        int idx1 = 0;
        do {
            buffer.append(str, idx1, idx2);
            buffer.append( replacement );
            idx1 = idx2 + targetLength;
            idx2 = str.indexOf(target, idx1);
        } while(idx2 > 0);
        buffer.append(str, idx1, str.length());
        return buffer.toString();
    }
    
    public static void addElementReferencesToResultSet(Set<String> elementReferences, Map<String, String> vars) {
        if (elementReferences.isEmpty()) { return; }
        if (vars.isEmpty()) { return; }
        elementReferences.forEach((eR) -> {
            for (String key : vars.keySet()) {
                if (eR.startsWith(key + ".") || eR.equals(key)) {  
                    eR = eR.replace(key, vars.get(key)).replaceAll("Owner:User", "OwnerId") + (eR.contains(".") ? "" : ".Id");
                    result.add(eR);
                }
            }
        });
    }
    
    public static void main(String[] args) throws Exception {
        //        File f = new File("FlowJson.json");
//        File f = new File("volarisJson.json");
        File f = new File("HardIgorFlow_03_04.json");
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String bodyLines = null;
        StringBuffer bodyStr = new StringBuffer();
        while ((bodyLines = br.readLine())!=null) {
            bodyStr.append(bodyLines);
        }
//        //System.out.println("bodyStr" + bodyStr.toString());
        br.close();
        fr.close();
        List<String> objs = (ArrayList<String>) null;
        System.out.println(("A__c.action0").substring(0, ("A__c.action0").indexOf(".")));
//        System.out.println("str1: " + bodyStr.toString());
        parse(bodyStr.toString());
    }
        
    public class ResponseRawData {
        public ArrayList<RawData> body;
    }

    public class RawData {
        public FlowMetadata Metadata;
    }

    public class FlowMetadata {
        public ArrayList<Object> actionCalls;
        public ArrayList<Object> assignments;
        public ArrayList<Object> decisions;
        public ArrayList<Object> choices;
        public ArrayList<Object> dynamicChoiceSets;
        public ArrayList<Object> formulas;
        public ArrayList<Object> processMetadataValues;
        public ArrayList<Object> recordCreates;
        public ArrayList<Object> recordLookups;
        public ArrayList<Object> recordUpdates;
        public ArrayList<Object> waits;
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