
package edu.ohiou.mfgresearch.service.base;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * The Service Schema
 * <p>
 * 
 * 
 */
public class Service {

    /**
     * The Serviceprofile Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private ServiceProfile serviceProfile;
    /**
     * The Servicegrounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    private ServiceGrounding serviceGrounding;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The Serviceprofile Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public ServiceProfile getServiceProfile() {
        return serviceProfile;
    }

    /**
     * The Serviceprofile Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setServiceProfile(ServiceProfile serviceProfile) {
        this.serviceProfile = serviceProfile;
    }

    /**
     * The Servicegrounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public ServiceGrounding getServiceGrounding() {
        return serviceGrounding;
    }

    /**
     * The Servicegrounding Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setServiceGrounding(ServiceGrounding serviceGrounding) {
        this.serviceGrounding = serviceGrounding;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

	@Override
	public String toString() {
		return 	"f_"+
				getServiceProfile().getServiceName()+
				"("+ 
				getServiceProfile().getInput().stream().map(i->i.getParameter()).collect(Collectors.joining(",")) +
				")->"+
				getServiceProfile().getOutput().getParameter();
	}

}
