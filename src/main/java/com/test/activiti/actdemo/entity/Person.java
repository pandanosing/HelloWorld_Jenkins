package com.test.activiti.actdemo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by lijian on 2019-09-27.
 */
public class Person implements Serializable {
   private Integer id;
   private String name;
   private Date date;
   private String note;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Date getDate() {
      return date;
   }

   public void setDate(Date date) {
      this.date = date;
   }

   public String getNote() {
      return note;
   }

   public void setNote(String note) {
      this.note = note;
   }
}
