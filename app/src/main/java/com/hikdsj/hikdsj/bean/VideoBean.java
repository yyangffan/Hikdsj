package com.hikdsj.hikdsj.bean;

public class VideoBean {

   private String msg;
   private Integer code;
   private DataBean data;

   public String getMsg() {
      return msg;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   public Integer getCode() {
      return code;
   }

   public void setCode(Integer code) {
      this.code = code;
   }

   public DataBean getData() {
      return data;
   }

   public void setData(DataBean data) {
      this.data = data;
   }

   public static class DataBean {
      private String path;

      public String getPath() {
         return path;
      }

      public void setPath(String path) {
         this.path = path;
      }
   }
}
