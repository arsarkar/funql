
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.Map;


/**
 * The Root Schema
 * <p>
 * 
 * 
 */
public class Services {

    /**
     * The Service Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private Service service;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Service Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Service getService() {
        return service;
    }

    /**
     * The Service Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setService(Service service) {
        this.service = service;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
