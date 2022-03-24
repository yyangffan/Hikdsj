package com.hikdsj.hikdsj.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.hikdsj.hikdsj.base.CarApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CarShareUtil {
    public static final String FILE_NAME = "APP_FILE";

    private static CarShareUtil mShareUtil;
    private static Context mContext;
    public static final String APP_AGREE = "app_agree";     //是否同意了用户协议
    public static final String APP_BASEURL ="APPBASEURL";   //项目的服务器地址
    public static final String APP_USERINFO = "APPUSERINFO";//存储LoginBean
    public static final String APP_USERID = "APPUSERID";    //接口访问的唯一标识
    public static final String APP_USERNAME = "APPUSERNAME";//userName
    public static final String CB_NAME ="CB_NAME";          //登录保存用户名
    public static final String CB_PWD = "CB_PWD";           //登录保存密码
    public static final String FWQ_IP = "FWQ_IP";                 //服务器ip地址
    public static final String FWQ_DKH = "FWQ_DKH";                //服务器端口号
    public static final String FWQ_INT = "FWQ_INT";                //服务器inter

    private CarShareUtil() {
    }

    public static CarShareUtil getInstance() {
        if (mShareUtil == null) {
            synchronized (CarShareUtil.class) {
                if (mShareUtil == null) {
                    mShareUtil = new CarShareUtil();
                }
            }
        }
        mContext = CarApplication.getInstance();
        return mShareUtil;
    }

    /**
     * 是否同意了协议
     *
     * @return true同意 false未同意
     */
    public boolean getAgree() {
        SharedPreferences sp = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(APP_AGREE, false);
    }

    public void put(String name, Object value) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (value instanceof String) {
            edit.putString(name, (String) value);
        } else if (value instanceof Integer) {
            edit.putInt(name, (Integer) value);
        } else if (value instanceof Boolean) {
            edit.putBoolean(name, (Boolean) value);
        } else if (value instanceof Float) {
            edit.putFloat(name, (Float) value);
        } else if (value instanceof Long) {
            edit.putLong(name, (Long) value);
        } else {
            edit.putString(name, new Gson().toJson(value));
        }
        SharedPreferencesCompat.apply(edit);
    }

    public Object get(String name, Object defaultValue) {
        SharedPreferences sp = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (defaultValue instanceof String) {
            return sp.getString(name, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sp.getInt(name, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sp.getBoolean(name, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sp.getFloat(name, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sp.getLong(name, (Long) defaultValue);
        }
//        else{edit.putString(name,new Gson().toJson(value));}
        return null;

    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public void remove(String key) {
        SharedPreferences sp = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        SharedPreferences sp = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        String app_url = (String)get(APP_BASEURL,"");
        String cb_name = (String)get(CB_NAME,"");
        String cb_pwd = (String)get(CB_PWD,"");


        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);

        if(!TextUtils.isEmpty(app_url)){
            put(APP_BASEURL,app_url);
        }
        if(!TextUtils.isEmpty(cb_name)){
            put(CB_NAME,cb_name);
            put(CB_PWD,cb_pwd);
        }
    }


    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }
}
