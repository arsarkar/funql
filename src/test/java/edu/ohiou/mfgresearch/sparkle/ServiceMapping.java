package edu.ohiou.mfgresearch.sparkle;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.service.ServiceFinder;
import edu.ohiou.mfgresearch.service.ServiceRegistry;
import edu.ohiou.mfgresearch.service.base.Service;
import edu.ohiou.mfgresearch.service.base.Services;

public class ServiceMapping {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void readServiceJson() {
		
		FileInputStream sr = null;
		try {
			sr = new FileInputStream(new File("C:/Users/sarkara1/git/sparkle/resources/META-INF/resources/service.json"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectMapper mapper = new ObjectMapper();
		Services services = null;
		try {
			services = mapper.readValue(sr, Services.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(services);
	}
	
	@Test
	public void loadServiceRegistry(){
		Uni.of(ServiceRegistry::new)
			.set(reg->reg.addService(new FileInputStream(new File("C:/Users/sarkara1/git/sparkle/resources/META-INF/services/calculateSurfaceAreaCone.json"))))
			.set(reg->reg.addService(new FileInputStream(new File("C:/Users/sarkara1/git/sparkle/resources/META-INF/services/calculateVolumeCone.json"))))
			.onSuccess(reg->System.out.println(reg.toString())); 
		;
	}
	
	@Test
	public void testServiceGraphGeneration(){
		Uni.of(ServiceRegistry::new)
		.set(reg->reg.addService(new FileInputStream(new File("C:/Users/sarkara1/git/sparkle/resources/META-INF/services/calculateSurfaceAreaCone.json"))))
		.set(reg->{
//			ServiceFinder service = new ServiceFinder();
		});
	}

}
