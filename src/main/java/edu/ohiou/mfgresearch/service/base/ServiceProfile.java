
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Serviceprofile Schema
 * <p>
 * 
 * 
 */
public class ServiceProfile {

    /**
     * The Servicename Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private String serviceName = "";
    /**
     * The Servicecategory Schema
     * <p>
     * 
     * 
     */
    private String serviceCategory = "";
    /**
     * The Textdescription Schema
     * <p>
     * 
     * 
     */
    private String textDescription = "";
    /**
     * The Contactinformation Schema
     * <p>
     * 
     * 
     */
    private List<String> contactInformation = null;
    /**
     * The Prefixnsmapping Schema
     * <p>
     * 
     * 
     */
    private List<PrefixNSMapping> prefixNSMapping = null;
    /**
     * The Actor Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private Actor actor;
    /**
     * The Input Schema
     * <p>
     * 
     * 
     */
    private List<Input> input = null;
    /**
     * The Output Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private Output output;
    /**
     * The Result Schema
     * <p>
     * 
     * 
     */
    private List<String> result = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Servicename Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * The Servicename Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * The Servicecategory Schema
     * <p>
     * 
     * 
     */
    public String getServiceCategory() {
        return serviceCategory;
    }

    /**
     * The Servicecategory Schema
     * <p>
     * 
     * 
     */
    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    /**
     * The Textdescription Schema
     * <p>
     * 
     * 
     */
    public String getTextDescription() {
        return textDescription;
    }

    /**
     * The Textdescription Schema
     * <p>
     * 
     * 
     */
    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    /**
     * The Contactinformation Schema
     * <p>
     * 
     * 
     */
    public List<String> getContactInformation() {
        return contactInformation;
    }

    /**
     * The Contactinformation Schema
     * <p>
     * 
     * 
     */
    public void setContactInformation(List<String> contactInformation) {
        this.contactInformation = contactInformation;
    }

    /**
     * The Prefixnsmapping Schema
     * <p>
     * 
     * 
     */
    public List<PrefixNSMapping> getPrefixNSMapping() {
        return prefixNSMapping;
    }

    /**
     * The Prefixnsmapping Schema
     * <p>
     * 
     * 
     */
    public void setPrefixNSMapping(List<PrefixNSMapping> prefixNSMapping) {
        this.prefixNSMapping = prefixNSMapping;
    }

    /**
     * The Actor Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Actor getActor() {
        return actor;
    }

    /**
     * The Actor Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    /**
     * The Input Schema
     * <p>
     * 
     * 
     */
    public List<Input> getInput() {
        return input;
    }

    /**
     * The Input Schema
     * <p>
     * 
     * 
     */
    public void setInput(List<Input> input) {
        this.input = input;
    }

    /**
     * The Output Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Output getOutput() {
        return output;
    }

    /**
     * The Output Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setOutput(Output output) {
        this.output = output;
    }

    /**
     * The Result Schema
     * <p>
     * 
     * 
     */
    public List<String> getResult() {
        return result;
    }

    /**
     * The Result Schema
     * <p>
     * 
     * 
     */
    public void setResult(List<String> result) {
        this.result = result;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
