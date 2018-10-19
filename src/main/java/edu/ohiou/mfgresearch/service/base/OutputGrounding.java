
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Outputgrounding Schema
 * <p>
 * 
 * 
 */
public class OutputGrounding {

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
    private List<Grounding_> grounding = null;
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
    public List<Grounding_> getGrounding() {
        return grounding;
    }

    /**
     * The Grounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setGrounding(List<Grounding_> grounding) {
        this.grounding = grounding;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
