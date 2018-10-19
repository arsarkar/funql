
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.Map;


/**
 * The Actor Schema
 * <p>
 * 
 * 
 */
public class Actor {

    /**
     * The Actortype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String actorType = "";
    /**
     * The Source Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String source = "";
    /**
     * The Endpoint Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String endPoint = "";
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Actortype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getActorType() {
        return actorType;
    }

    /**
     * The Actortype Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    /**
     * The Source Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getSource() {
        return source;
    }

    /**
     * The Source Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * The Endpoint Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getEndPoint() {
        return endPoint;
    }

    /**
     * The Endpoint Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
