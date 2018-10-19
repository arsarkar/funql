
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.Map;


/**
 * The Output Schema
 * <p>
 * 
 * 
 */
public class Output {

    /**
     * The Parameter Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String parameter = "";
    /**
     * The Parametertype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String parameterType = "";
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Parameter Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * The Parameter Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * The Parametertype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getParameterType() {
        return parameterType;
    }

    /**
     * The Parametertype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
