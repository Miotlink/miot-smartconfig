package com.miot.android.smarthome.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.miot.android.smarthome.entity.FirstData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析mlcc smart-connected 和firstconfig
 *
 * @author Administrator
 */
public class Mlcc_ParseUtils {

    private static final String smartConnected = "CodeName=smart_connected";

    private static final String smartPlatformFinishAck = "CodeName=fc_complete_ack";

    private static final String smartSetWifiAck = "CodeName=SetWifiAck";

    private static final String MLCC_SPLIT_FLAG = "&";

    private static final String MLCC_SPLIT_KEY_VALUE_FLAG = "=";

    /**
     * 判断是否是smartconnected 返回的值
     *
     * @param value
     * @return
     */
    public static boolean isSmartConnected(String value) {
        if (value.split(MLCC_SPLIT_FLAG).length > 0) {
            if (value.split(MLCC_SPLIT_FLAG)[0]
                    .equals(smartConnected)
                    || value.startsWith(smartConnected)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSetWifiAck(String value) {
        if (value.split(MLCC_SPLIT_FLAG).length > 0) {
            if (value.split(MLCC_SPLIT_FLAG)[0]
                    .equals(smartSetWifiAck)
                    || value.startsWith(smartSetWifiAck)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlatformFinishAck(String value) {
        if (value.split(MLCC_SPLIT_FLAG).length > 0) {
            if (value.split(MLCC_SPLIT_FLAG)[0]
                    .equals(smartPlatformFinishAck)
                    || value.startsWith(smartPlatformFinishAck)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> getParse(String value) throws Exception {
        Map<String, String> parseMap = new HashMap<String, String>();
        for (String oneContent : value.split(MLCC_SPLIT_FLAG)) {
            String keyValueArray[] = oneContent
                    .split(MLCC_SPLIT_KEY_VALUE_FLAG);
            if (keyValueArray == null
                    || (keyValueArray.length != 2 && !oneContent
                    .contains(MLCC_SPLIT_KEY_VALUE_FLAG))) {
                throw new Exception("one keyValueArray length != 2, param=["
                        + oneContent + "]");
            } else {
                if (keyValueArray.length == 2) {
                    parseMap.put(keyValueArray[0], keyValueArray[1]);
                } else {
                    parseMap.put(keyValueArray[0], "");
                }
            }
        }
        return parseMap;
    }

    public static Map<String, Object> getParseObject(String value) throws Exception {
        Map<String, Object> parseMap = new HashMap<String, Object>();
        for (String oneContent : value.split(MLCC_SPLIT_FLAG)) {
            String keyValueArray[] = oneContent
                    .split(MLCC_SPLIT_KEY_VALUE_FLAG);
            if (keyValueArray == null
                    || (keyValueArray.length != 2 && !oneContent
                    .contains(MLCC_SPLIT_KEY_VALUE_FLAG))) {
                throw new Exception("one keyValueArray length != 2, param=["
                        + oneContent + "]");
            } else {
                if (keyValueArray.length == 2) {
                    parseMap.put(keyValueArray[0], keyValueArray[1]);
                } else {
                    parseMap.put(keyValueArray[0], "");
                }
            }
        }
        return parseMap;
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Object> getValue(Map<String, String> value) {
        Map<String, Object> mapValue = new HashMap<String, Object>();
        try {
            if (value == null) {
                return null;
            }
            for (Map.Entry MapString : value.entrySet()) {
                if (MapString.getKey().equals("mac")) {
                    mapValue.put("macCode", MapString.getValue());
                } else {
                    if (!MapString.getKey().equals("CodeName")) {
                        mapValue.put(MapString.getKey() + "", MapString.getValue());
                    }
                }
            }
        } catch (Exception e) {

        }
        return mapValue;

    }

    @SuppressWarnings("rawtypes")
    public static String getSuccessValue(Map<String, Object> map) throws Exception {
        String json = "";
        JSONObject jsonObjectValue = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        if (map != null) {
            for (Map.Entry mapValue : map.entrySet()) {
                jsonObject.put(mapValue.getKey() + "", mapValue.getValue().toString());
            }
        }
        jsonObjectValue.put("code", "smartconfigResult");
        jsonObjectValue.put("data", jsonObject);
        json = jsonObjectValue.toString();
        return json;
    }

    /**
     * 判断返回的MlCC
     *
     * @param s
     * @return
     */
    public static String getMlccConfigResult(String s) {
        if (s.isEmpty()) {
            return "";
        }
        String result = "";
        String[] results = s.split(MLCC_SPLIT_FLAG);
        if (results.length == 0) {
            return result;
        }
        for (String oneContent : results) {
            String keyValueArray[] = oneContent
                    .split(MLCC_SPLIT_KEY_VALUE_FLAG);
            if (keyValueArray == null
                    || (keyValueArray.length != 2 && !oneContent
                    .contains(MLCC_SPLIT_KEY_VALUE_FLAG))) {
                break;
            }
            if (keyValueArray[0].equals("CodeName")) {
                result = keyValueArray[1];
                break;
            }
        }
        return result;
    }

    /**
     * 判断上报上来的kind 和model 是否一致
     *
     * @param kind
     * @param model
     * @param mapValue
     * @return
     */
    public static int isReportKindOrModel(String validationModel, String kind, String model, Map<String, Object> mapValue) {
        try {
            if (mapValue == null) {
                return 20;
            }
            Log.e("code", "判断上报kind&modle=" + validationModel);
            if (mapValue.containsKey("kind") || mapValue.containsKey("model")) {
                String modelId = mapValue.get("model").toString();
                String kindId = mapValue.get("kind").toString();
                Log.e("code", "modelId:" + modelId + "==" + "kindId:" + kindId);
                if (validationModel.equals("0")) {
                    if (modelId.equals(model)) {
                        return 21;
                    } else if (modelId.equals("0")) {
                        return 21;
                    } else {
                        return 30;
                    }
                } else if (validationModel.equals("1")) {
                    if (modelId.equals(model)) {
                        return 21;
                    } else if (modelId.equals("0")) {
                        return 40;
                    } else {
                        return 50;
                    }
                } else if (validationModel.equals("2")) {
                    return 21;
                }
            } else {
                if (validationModel.equals("1")) {
                    return 60;
                } else {
                    return 21;
                }
            }

        } catch (Exception e) {
            return 20;
        }
        return 20;
    }





    public static List<FirstData> getFirstData(String data) {
        List<FirstData> firstDatas = new ArrayList<>();
        firstDatas.clear();
        try {
            if (!data.equals("")) {
                JSONArray array = new JSONArray(data);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = new JSONObject(array.get(i).toString());
                    FirstData firstData = new FirstData(json.getString("index"), json.getString("cmdData"), json.getString("ackCodeName"), json.getString("ackResult"));
                    firstDatas.add(firstData);
                }
            }
        } catch (JSONException e) {
            FirstData firstData = new FirstData("1", SmartConsts.PLATFORM_FC_PLATFORM, SmartConsts.PLATFORM_FC_PLATFORM_ACK, "0");
            firstDatas.add(firstData);
            e.printStackTrace();
        }
        return firstDatas;
    }

	/**
     *
     * @param
     * @return
     */
    public static String parseFeiBiValue(@NonNull String qrcode) {
        String value = "";
        try{
            if (qrcode.startsWith("FHA")){
                value = qrcode.substring(qrcode.indexOf("GT") + 2, qrcode.length());
                value = value.substring(0, value.indexOf("pass"));
            }
        }catch (Exception e){

        }

        return value;
    }


    public static String getSimParse(@NonNull String sim) throws Exception {
        String mac = "";
        try {
            if (sim.equals("")) {
                return "";
            }
            if (sim.length() != 15) {
                return "";
            }
            mac = sim.substring(2, sim.length());
            String imieString = mac.substring(0, 3);
            StringBuffer stringBuffer = new StringBuffer();
            if (Integer.parseInt(imieString) > 255) {
                String firstMacCode = Integer.toHexString(Integer.parseInt(imieString)).toUpperCase();
                if (firstMacCode.length() > 1) {
                    firstMacCode = firstMacCode.substring(1, firstMacCode.length());
                }
                stringBuffer.append(firstMacCode).append(":");
            } else {
                stringBuffer.append(Integer.toHexString(Integer.parseInt(imieString)).toUpperCase()).append(":");
            }
            String imieLast = mac.substring(3, mac.length());
            for (int i = 0; i < splitStrs(imieLast).length; i++) {
                if (Integer.parseInt(splitStrs(imieLast)[i]) < 16) {
                    stringBuffer.append("0" + Integer.toHexString(Integer.parseInt(splitStrs(imieLast)[i]))).append(":");
                } else {
                    stringBuffer.append(Integer.toHexString(Integer.parseInt(splitStrs(imieLast)[i]))).append(":");
                }
            }
            mac = stringBuffer.toString().substring(0, stringBuffer.toString().length() - 1);
            return mac.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }


    public static String[] splitStrs(@NonNull String str) {
        int m = str.length() / 2;
        if (m * 2 < str.length()) {
            m++;
        }
        String[] strs = new String[m];
        int j = 0;

        for (int i = 0; i < str.length(); i++) {
            if (i % 2 == 0) {
                strs[j] = "" + str.charAt(i);
            } else {
                strs[j] = strs[j] + "" + str.charAt(i);
                j++;
            }
        }
        return strs;
    }
}
