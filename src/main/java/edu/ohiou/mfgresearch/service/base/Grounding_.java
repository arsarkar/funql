
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.Map;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class Grounding_ {

    /**
     * The Arg Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private Integer arg = 0;
    /**
     * The Dataproperty Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String dataProperty = "";
    /**
     * The Datatype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String dataType = "";
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Arg Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Integer getArg() {
        return arg;
    }

    /**
     * The Arg Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setArg(Integer arg) {
        this.arg = arg;
    }

    /**
     * The Dataproperty Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getDataProperty() {
        return dataProperty;
    }

    /**
     * The Dataproperty Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setDataProperty(String dataProperty) {
        this.dataProperty = dataProperty;
    }

    /**
     * The Datatype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * The Datatype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
