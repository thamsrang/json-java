package jsonToJavaParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * @author Thamaraiselvan Rangasami
 * */
public class JSONParser {
	private StringBuilder strCode=new StringBuilder();
	private boolean isFirst=true;
	private String currentKey;
	private String currentObjName;
	private String tabStr=" ";
	private boolean isFromArr=false;
	
	/**
	 * Parsing the JSON in format of String.
	 * 
	 * @param jStr
	 * @return code in the format of {@link String}
	 */
	public String parseString(String jStr) {
		jsonParser(toJSON(jStr));
		return strCode.toString();
	}
		
	/**
	 * Parsing the JSON in the file in the @param path
	 * 
	 * @param path
	 * @return code in the format of {@link String}
	 */
	public String parseFile(String path) {
		jsonParser(filePathToJSON(path));
		return strCode.toString();
	}
	
	/**
	 * Parsing JSON passes @param json
	 * 
	 * @param json
	 * @return code in the format of {@link String}
	 */
	public String parseJSON(JSON json) {
		jsonParser(json);
		return strCode.toString();
	}
	
	/**
	 * Reading a file formating to string  by using file path.
	 * 
	 * @param path
	 * @return formated string of file
	 */
	public JSON filePathToJSON(String path) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line =new String();
	    try {
	    	br = new BufferedReader(new FileReader(path));
	        while ((line = br.readLine()) != null) {
	            sb.append(line);
	        }
	        br.close();
	    }
	    catch (IOException e) {
	    	System.out.println("Error in reading file in path : "+path);
		}
	    return toJSON(sb.toString());
	}
		
	/**
	 * Parsing string to JSON
	 * 
	 * @param str
	 * @return JSON
	 */
	public JSON toJSON(String str) {
		JSON json=null;
		try {
			json=(JSON)JSONSerializer.toJSON(str);
		}
		catch (JSONException e) {
			System.out.println("<===== StringToJSON =====>"+"\n"+e);
		}
		return json;
	}

	/**
	 * Find JSON type initiates the parsing
	 * 
	 * @param json
	 */
	public void jsonParser(JSON json) {
		try{
			JSONObject jObj=new JSONObject();
			JSONArray jArr=new JSONArray();
			System.out.println("str=\""+json+"\"");
			if (json.isArray()) {
				if(isFirst){
					strCode.append(tabStr+"JSONArray jObj=(JSONArray) JSONSerializer.toJSON(str);\n");
					isFirst=false;
				}
				currentObjName="jArr";
				jArr=(JSONArray)json;
				jsonArrayParser(jArr);
			}
			else{
				if(isFirst){
					strCode.append(tabStr+"JSONObject jObj=(JSONObject) JSONSerializer.toJSON(str);\n");
					isFirst=false;
				}
				currentObjName="jObj";
				jObj=(JSONObject)json;
				jsonObjectParser(jObj);
			}
		}
		catch (JSONException e) {
			System.out.println("<===== JsonParser =====>"+"\n"+e);
		}
	}

	/**
	 * Parse JSONArray 
	 * 
	 * @param jArr
	 */
	public void jsonArrayParser(JSONArray jArr) {
		try{
			if (!isFromArr && isFirst) {
				strCode.append(tabStr+"JSONArray "+currentKey+"="+currentObjName+".getJSONArray(\""+currentKey+"\");\n");	
			}
			isFromArr=false;

			int sizeArr=jArr.size();
			if (sizeArr>0) {
				strCode.append(tabStr+"for (int i = 0; i < "+currentKey+".size(); i++) { \n");
				tabStr+="\t";
			}
			else{
				strCode.append(tabStr+"// Here JSONArray '"+currentKey+"' is empty.\n");
			}
			for (int i = 0; i < sizeArr; i++) {
				isFromArr=true;
				String dataType=getObjectType(jArr.get(i));
				if (dataType.equals("net.sf.json.JSONArray")) {
					strCode.append(tabStr+"JSONArray "+currentKey+"="+currentObjName+".get("+i+");\n");
	
					jsonArrayParser((JSONArray)jArr.get(i));
				}
				else if (dataType.equals("net.sf.json.JSONObject")) {
					strCode.append(tabStr+"JSONObject "+currentKey+"="+currentObjName+".get("+i+");\n");

					jsonObjectParser((JSONObject)jArr.get(i));
				}
				else if (dataType.equals("net.sf.json.JSONNull")) {
					strCode.append(tabStr+"// Here Object '"+currentKey+"' is 'null'.\n");
				}
				else if (dataType.equals("java.lang.Integer")) {
					strCode.append(tabStr+currentObjName+".getInt(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("java.lang.String")) {
					strCode.append(tabStr+currentObjName+".getString(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("java.lang.Boolean")) {
					strCode.append(tabStr+currentObjName+".getBoolean(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("long")) {
					strCode.append(tabStr+currentObjName+".getLong(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("double")) {
					strCode.append(tabStr+currentObjName+".getDouble(\""+currentKey+"\");\n");
				}
			}
			if (tabStr.length() > 0 && sizeArr>0) {
				tabStr=tabStr.substring(1);
			}
			if (sizeArr>0) {
				strCode.append(tabStr+"} \n");
			}
		}
		catch (Exception e) {
			System.out.println("<===== JSONArrayParser =====>"+"\n"+e);
		}
	}

	/**
	 * Parse the JSONObject
	 * 
	 * @param jObj
	 */
	public void jsonObjectParser(JSONObject jObj) {
		try{
			if(!isFromArr && isFirst){
				strCode.append(tabStr+"JSONObject "+currentKey+"="+currentObjName+".getJSONObject(\""+currentKey+"\");\n");
			}
			isFromArr=false;
			
			@SuppressWarnings("unchecked")
			Iterator<String> keysArr=jObj.keys();
	
			while (keysArr.hasNext()) {
				currentKey=keysArr.next();
				String dataType=getObjectType(jObj.get(currentKey));
				if (dataType.equals("net.sf.json.JSONArray")) {
					jsonArrayParser((JSONArray)jObj.get(currentKey));
				}
				else if (dataType.equals("net.sf.json.JSONObject")) {
					isFirst=true;
					jsonObjectParser((JSONObject)jObj.get(currentKey));
				}
				else if (dataType.equals("net.sf.json.JSONNull")) {
					strCode.append(tabStr+"// Here Object '"+currentKey+"' is 'null'.\n");
				}
				else if (dataType.equals("java.lang.Integer")) {
					strCode.append(tabStr+"int "+currentKey+"="+currentObjName+".getInt(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("java.lang.String")) {
					strCode.append(tabStr+"String "+currentKey+"="+currentObjName+".getString(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("java.lang.Boolean")) {
					strCode.append(tabStr+"boolean "+currentKey+"="+currentObjName+".getBoolean(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("long")) {
					strCode.append(tabStr+"long "+currentKey+"="+currentObjName+".getLong(\""+currentKey+"\");\n");
				}
				else if (dataType.equals("double")) {
					strCode.append(tabStr+"double "+currentKey+"="+currentObjName+".getDouble(\""+currentKey+"\");\n");
				}
			}
			if (tabStr.length() > 0) {
				tabStr=tabStr.substring(1);
			}
			
		}
		catch (Exception e) {
			System.out.println("<===== JSONObjectParser =====>"+"\n"+e);
		}
	}
	
	/**
	 * Getting the type of an object.
	 * 
	 * @param obj
	 * @return Data type of parameter object
	 */
	public String getObjectType(Object obj) {
		String className=null;
		try{
			if (obj!=null) {
				className=obj.getClass().getName();
			}
		}
		catch (Exception e) {
			System.out.println("<===== Getting data type =====>"+"\n"+e);
		}
		return className;
	}

}
