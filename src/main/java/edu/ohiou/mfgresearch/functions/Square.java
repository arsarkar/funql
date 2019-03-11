package edu.ohiou.mfgresearch.functions;

public class Square {

	public static double[] origin = new double[]{0.0, 0.0};
	
	/**
	 * returns vertices of a square in coordinate (x,y)
	 * @param lengthSide
	 * @return
	 */
	public static String[] getVertices(String lengthSide){
		String[] vertices = new String[4];
		double ls = Double.parseDouble(lengthSide);
		double[][] vertCords = new double[4][2];
		vertCords[0] = new double[]{origin[0], origin[1]};
		vertCords[1] = new double[]{origin[0]+ls, origin[1]};
		vertCords[2] = new double[]{origin[0], origin[1]+ls};
		vertCords[3] = new double[]{origin[0]+ls, origin[1]+ls};
		for(int i=0; i<4; i++){
			vertices[i] = String.valueOf(vertCords[i][0]) + "," + String.valueOf(vertCords[i][0]);
		}
		return vertices;
	}

}
