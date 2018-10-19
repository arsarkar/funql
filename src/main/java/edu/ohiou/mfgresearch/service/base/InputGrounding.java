
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class InputGrounding {

    /**
     * The Parameter Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String parameter = "";
    /**
     * The Grounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private List<Grounding> grounding = null;
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
     * The Grounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public List<Grounding> getGrounding() {
        return grounding;
    }

    /**
     * The Grounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setGrounding(List<Grounding> grounding) {
        this.grounding = grounding;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
