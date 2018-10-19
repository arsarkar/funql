
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Servicegrounding Schema
 * <p>
 * 
 * 
 */
public class ServiceGrounding {

    /**
     * The Inputgrounding Schema
     * <p>
     * 
     * 
     */
    private List<InputGrounding> inputGrounding = null;
    /**
     * The Outputgrounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private OutputGrounding outputGrounding;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Inputgrounding Schema
     * <p>
     * 
     * 
     */
    public List<InputGrounding> getInputGrounding() {
        return inputGrounding;
    }

    /**
     * The Inputgrounding Schema
     * <p>
     * 
     * 
     */
    public void setInputGrounding(List<InputGrounding> inputGrounding) {
        this.inputGrounding = inputGrounding;
    }

    /**
     * The Outputgrounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public OutputGrounding getOutputGrounding() {
        return outputGrounding;
    }

    /**
     * The Outputgrounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setOutputGrounding(OutputGrounding outputGrounding) {
        this.outputGrounding = outputGrounding;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
