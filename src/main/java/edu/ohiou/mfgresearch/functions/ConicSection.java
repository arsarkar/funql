package edu.ohiou.mfgresearch.functions;

/**
 * a test class to be read by reflection
 * for service of actor type "java-method"
 * every such method should be static
 * @author sarkara1
 *
 */
public class ConicSection {	
	/**
	 * service calculateVolumeCone
	 * @param h
	 * @param d
	 * @return
	 */
	public static double calculateVolume(double h, double d){
		double vol = (1.0/3.0)*Math.PI*(d/2.0)*(d/2.0)*h;
		return vol;
	}
	
	/**
	 * service calculateSurfaceAreaCone
	 * @param h
	 * @param d
	 * @return
	 */
	public static double calculateSurfaceArea(double h, double d){
		return Math.PI*(d/2)*(d/2+Math.pow((h*h+(d/2)*(d/2)), 0.5));
	}
	
	public static double calculateInnerCubeSide(double h, double d){
		return (d/2)*h/(d+h*Math.pow(2, 0.5));
	}

}
