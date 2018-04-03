package com.run.action.impl;

import com.run.action.CommandExecutor;
import com.run.receiver.ActionReceiver;
import com.run.util.HttpUtil;
import com.run.util.JsonParser;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.whitelist.Constans;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.cordova.whitelist.HttpRequestPlugin.relInfo;
import static org.apache.cordova.whitelist.HttpRequestPlugin.token;
import static org.apache.cordova.whitelist.HttpRequestPlugin.userInnerId;

/**
 * Created by Xiao on 2018/4/3.
 */

public class ConsumtionExecutor implements CommandExecutor {
    private ActionReceiver actionReceiver;

    public ConsumtionExecutor(ActionReceiver actionReceiver) {
        this.actionReceiver = actionReceiver;
    }

    @Override
    public boolean execute() throws JSONException {
        CallbackContext callbackContext = actionReceiver.getCallbackContext();
        JSONArray args = actionReceiver.getParams();
        String url = actionReceiver.getUrl();
        JSONObject message = new JSONObject();
        if(relInfo == null){
            message.put("code", "404");
            message.put("msg","过期");
            callbackContext.error(message);
            return true;
        }
        userInnerId = relInfo.getUserInnerId();	// 用户id
        String startDate = ""; //开始时间
        String endDate = ""; //结束时间
        if(args.length() > 0 && args != null){
            startDate = args.getString(0);
            endDate = args.getString(1);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userInnerId", userInnerId);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("token", token);
        String rel = HttpUtil.sendRequest(url, data);
        Map<String, Object> relData = JsonParser.toObj(rel, Map.class);
        if(rel == null || relData.isEmpty()){
            message.put("code","-1");
            message.put("msg","服务器返回为空");
            callbackContext.error(message);
            return true;
        }
        String state = (String) relData.get(Constans.STATE);
        if(!state.equals(Constans.STATE_200)){
            if(state.equals(Constans.STATE_404)){
                message.put("code","404");
                message.put("msg",relData.get("msg"));
                callbackContext.error(message);
                return true;
            }
            else if(state.equals(Constans.STATE_405)){
                message.put("code","405");
                message.put("msg",relData.get("msg"));
                callbackContext.error(message);
                return true;
            }
            else {
                message.put("code","-1");
                message.put("msg",relData.get("msg"));
                callbackContext.error(message);
                return true;
            }
        }
        ArrayList<Map<String, Object>> result = (ArrayList<Map<String, Object>>)relData.get("result");
        callbackContext.success(JsonParser.toJson(result));//如果不调用success回调，则js中successCallback不会执行
        return true;
    }
}