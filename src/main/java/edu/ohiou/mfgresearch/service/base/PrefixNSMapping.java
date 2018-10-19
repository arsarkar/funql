
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.Map;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class PrefixNSMapping {

    /**
     * The Prefix Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String prefix = "";
    /**
     * The Namespace Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String nameSpace = "";
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Prefix Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * The Prefix Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * The Namespace Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * The Namespace Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
