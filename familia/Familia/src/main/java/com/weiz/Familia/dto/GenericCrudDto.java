package com.weiz.Familia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericCrudDto {

    private String field;
    private String whereClause;
    private String orderClause;
    private String quey;

    @JsonProperty("db_type")
    private String dbType;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getQuey() {
        return quey;
    }

    public void setQuey(String quey) {
        this.quey = quey;
    }

    public String getOrderClause() {
        return orderClause;
    }

    public void setOrderClause(String orderClause) {
        this.orderClause = orderClause;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

}
