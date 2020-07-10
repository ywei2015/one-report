package com.nokia.export.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**  
 * @author ljh  E-mail: 
 * @version 创建时间：2017-6-26 上午11:01:47  
 * 类说明  
 */

public class ColorTransform {
	private static Logger logger = LoggerFactory.getLogger("ColorTransform");

	public static Color FromArgbStringToKmlColor(String argb) {
		String argbvalue = argb;
		if (argbvalue.contains("(") || argbvalue.contains(")")) {
			argbvalue=argbvalue.replaceAll("\\(", "").replaceAll("\\)", "").replace("rgba", "");
			logger.info("argbvalue="+argbvalue);
		}
		String[] argbStrings = argbvalue.split(",");
		double al = Double.valueOf(argbStrings[3]);
		int alpha = (int) (255*al);
		int red = Integer.valueOf(argbStrings[0]);
		int green = Integer.valueOf(argbStrings[1]);
		int blue = Integer.valueOf(argbStrings[2]);
		Color color = new Color(red, green, blue, alpha);//Color.FromArgb(alpha, blue, green, red);
		return color;
	}
	
	public static String FromArgbStringToHex(String argb) {
		Color color = FromArgbStringToKmlColor(argb);
		String hex = FromColorToHex(color);
		return hex;
	}
		
	public static String FromColorToHex(Color color) {
		String A, R, G, B;
		StringBuffer sb = new StringBuffer();
		A = Integer.toHexString(color.getAlpha());
		R = Integer.toHexString(color.getRed());
		G = Integer.toHexString(color.getGreen());
		B = Integer.toHexString(color.getBlue());
		A = A.length() == 1 ? "0" + A : A;
		R = R.length() == 1 ? "0" + R : R;
		G = G.length() == 1 ? "0" + G : G;
		B = B.length() == 1 ? "0" + B : B;
		sb.append(A.toUpperCase());
		sb.append(B.toUpperCase());
		sb.append(G.toUpperCase());
		sb.append(R.toUpperCase());
		return sb.toString();
	}

	public static String FromColorToHex2(Color color) {
		String A, R, G, B;
		StringBuffer sb = new StringBuffer();
		A = Integer.toHexString(color.getAlpha());
		R = Integer.toHexString(color.getRed());
		G = Integer.toHexString(color.getGreen());
		B = Integer.toHexString(color.getBlue());
		A = A.length() == 1 ? "0" + A : A;
		R = R.length() == 1 ? "0" + R : R;
		G = G.length() == 1 ? "0" + G : G;
		B = B.length() == 1 ? "0" + B : B;
		sb.append(A.toUpperCase());
		sb.append(R.toUpperCase());
		sb.append(G.toUpperCase());
		sb.append(B.toUpperCase());
		return sb.toString();
	}

	public static String transformToGeoStyleFill(String styler){
		String opacity = styler.substring(0,2);
		Color color = toColorFromString(styler);
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		Color color_res = new Color(b,g,r);
		String style_geo = FromColorToHex2(color_res);
		String res = opacity + style_geo.substring(2);
		return res == null ? null : res.toLowerCase();
	}

	public static Color toColorFromString(String colorStr){
		colorStr = colorStr.substring(2);
		Color color =  new Color(Integer.parseInt(colorStr, 16)) ;
		return color;
	}

	public static void main(String[] args) {
		String test = "ffe0ffd7";
		//String res = ColorTransform.transformToGeoStyleFill(test);
		Color color =  new Color(Integer.parseInt(test, 16)) ;
		System.out.println(color);
	}
}

